package ronancamargo.firestore.v3.repositories.safe

import cats.Show
import cats.effect.Sync
import cats.free.Yoneda
import cats.implicits._
import com.google.api.core.ApiFuture
import com.google.api.gax.rpc.AlreadyExistsException
import com.google.cloud.firestore._
import fs2.{Chunk, Pull}
import mouse.all._
import ronancamargo.firestore.v3.codec.{FirestoreDecoder, FirestoreEncoder}
import ronancamargo.firestore.v3.data.FirestoreDocument
import ronancamargo.firestore.v3.data.safe.DocumentDepthCoproduct.DocumentDepthCoproduct
import ronancamargo.firestore.v3.errors.FirestoreError
import ronancamargo.firestore.v3.repositories.safe.Ops._
import ronancamargo.firestore.v3.shapeless.Projection
import shapeless.HList
import shapeless.ops.coproduct
import shapeless.ops.hlist.ToTraversable

import scala.annotation.implicitNotFound
import scala.concurrent.ExecutionException
import scala.jdk.CollectionConverters._

abstract class FirestoreRepository[F[_] : Sync, A <: Product, D <: HList](
    database: Firestore
)(implicit
    @implicitNotFound("Check ${D} parameter, it must be a valid DocumentDepth type")
    coproductInject: coproduct.Inject[DocumentDepthCoproduct, D],
    toTraversable: ToTraversable.Aux[D, List, String]
) {

  protected val collectionHierarchy: D
  protected def keyFromDoc(doc: A): D

  private implicit val showD: Show[D] = (t: D) => t.toList.mkString(" -> ")

  protected def parentCollectionReference(collections: D, keys: D, firestore: Firestore): CollectionReference =
    documentReference(collections, keys, firestore).getParent

  protected def documentReference(
      collections: D,
      keys: D,
      firestore: Firestore
  ): DocumentReference =
    ((collections.toList, keys.toList): @unchecked) match {
      case (collectionsHead :: collectionTail, keysHead :: keysTail) =>
        val seedDocRef = firestore.collection(collectionsHead).document(keysHead)
        (collectionTail zip keysTail)
          .foldLeft(seedDocRef) { case (docRef, (col, key)) => docRef.collection(col).document(key) }
    }

  def getAllAsStream(keys: D)(implicit firestoreDecoder: FirestoreDecoder[A]): fs2.Stream[F, A] = {
    val pageSize                                              = 500
    def go(offset: Int): Pull[F, QueryDocumentSnapshot, Unit] =
      Pull
        .eval(
          Sync[F].blocking(
            parentCollectionReference(collectionHierarchy, keys, database)
              .offset(offset)
              .limit(pageSize)
              .get()
              .awaitFirestore
          )
        )
        .map(_.getDocuments.asScala.iterator)
        .flatMap {
          case iterator if iterator.nonEmpty => Pull.output(Chunk.iterator(iterator)) >> go(offset + pageSize)
          case _                             => Pull.done
        }

    go(0).stream.map(_.getData).map(FirestoreDocument(_)).map(firestoreDecoder.decode)
  }

  def getAll(keys: D)(implicit firestoreDecoder: FirestoreDecoder[A]): F[Either[FirestoreError, List[A]]] =
    Yoneda(Sync[F].blocking(parentCollectionReference(collectionHierarchy, keys, database).get().awaitFirestore))
      .map(_.getDocuments.asScala.toList)
      .mapNested2(_.getData)
      .mapNested2(FirestoreDocument(_))
      .mapNested2(firestoreDecoder.decode)
      .run
      .attempt
      .leftMapIn(errorHandler)

  def getByQuery(keys: D)(
      query: CollectionReference => Query
  )(implicit firestoreDecoder: FirestoreDecoder[A]): F[Either[FirestoreError, List[A]]] =
    Yoneda(Sync[F].blocking(query(parentCollectionReference(collectionHierarchy, keys, database)).get().awaitFirestore))
      .map(_.getDocuments.asScala.toList)
      .mapNested2(_.getData)
      .mapNested2(FirestoreDocument(_))
      .mapNested2(firestoreDecoder.decode)
      .run
      .attempt
      .leftMapIn(errorHandler)

  def set(doc: A)(implicit
      encoder: FirestoreEncoder[A]
  ): F[Either[FirestoreError, A]] =
    Sync[F]
      .pure(documentReference(collectionHierarchy, keyFromDoc(doc), database))
      .tupleRight(encoder(doc).document)
      .flatMap { case (ref, doc) => Sync[F].blocking(ref.set(doc).awaitFirestore) }
      .as(doc)
      .attempt
      .leftMapIn(errorHandler)

  def get(key: D)(implicit
      decoder: FirestoreDecoder[A]
  ): F[Either[FirestoreError, A]] =
    getOption(key).flatMapIn(_.toRight(FirestoreError.documentNotFound(key, collectionHierarchy)))

  def getOption(
      key: D
  )(implicit decoder: FirestoreDecoder[A]): F[Either[FirestoreError, Option[A]]] =
    Sync[F]
      .pure(documentReference(collectionHierarchy, key, database))
      .flatMap { ref => Sync[F].blocking(Option(ref.get().awaitFirestore.getData)) }
      .mapIn(FirestoreDocument(_))
      .mapIn(decoder.decode)
      .attempt
      .leftMapIn(errorHandler)

  def delete(key: D)(): F[Either[FirestoreError, Unit]] =
    Sync[F]
      .pure(documentReference(collectionHierarchy, key, database))
      .flatMap { ref => Sync[F].blocking(ref.delete().get()) }
      .void
      .attempt
      .leftMapIn(errorHandler)

  def create(doc: A)(implicit encoder: FirestoreEncoder[A]): F[Either[FirestoreError, A]] =
    Sync[F]
      .pure(documentReference(collectionHierarchy, keyFromDoc(doc), database))
      .tupleRight(encoder(doc).document)
      .flatMap { case (ref, doc) => Sync[F].blocking(ref.create(doc).awaitFirestore) }
      .as(doc)
      .attempt
      .leftMapIn(errorHandler)

  def unsafeUpdate(doc: A)(implicit
      encoder: FirestoreEncoder[A]
  ): F[Either[FirestoreError, A]] = {
    Sync[F]
      .pure(documentReference(collectionHierarchy, keyFromDoc(doc), database))
      .tupleRight(encoder(doc).document)
      .flatMap { case (ref, doc) => Sync[F].blocking(ref.update(doc).awaitFirestore) }
      .as(doc)
      .attempt
      .leftMapIn(errorHandler)
  }

  def updateProjection[B <: Product, LA <: HList, LB <: HList](
      key: D
  )(f: A => B)(implicit
      decoder: FirestoreDecoder[A],
      encoder: FirestoreEncoder[B],
      projectable: Projection[A, B]
  ): F[Either[FirestoreError, B]] =
    Sync[F]
      .blocking {
        database.runTransaction { tx =>
          val reference = documentReference(collectionHierarchy, key, database)
          val data      = reference.get().awaitFirestore.getData
          val decoded   = decoder.decode(FirestoreDocument(data))
          val projected = f(decoded)
          val encoded   = encoder(projected)
          tx.update(reference, encoded.document)

          projected
        }.awaitFirestore
      }
      .attempt
      .leftMapIn(errorHandler)

  def update[LA <: HList](key: D)(
      f: A => A
  )(implicit
      decoder: FirestoreDecoder[A],
      encoder: FirestoreEncoder[A],
      projection: Projection[A, A]
  ): F[Either[FirestoreError, A]] =
    updateProjection(key)(f)

  private def errorHandler: Throwable => FirestoreError = {
    case ExecutionException(_: AlreadyExistsException) => FirestoreError.documentAlreadyExists
    case error: FirestoreError                         => error
    case error                                         => FirestoreError.unexpected(error)
  }
}

private object Ops {
  implicit final class ApiFutureOps[OP](private val future: ApiFuture[OP]) extends AnyVal {
    def awaitFirestore: OP = future.get()

//    def awaitFirestoreAsync[F[_] : Async]: F[OP] = Async[F].async_ { (cb: Either[Throwable, OP] => Unit) =>
//      ApiFutures.addCallback(
//        future,
//        new ApiFutureCallback[OP] {
//          override def onFailure(t: Throwable): Unit = cb(Left(t))
//
//          override def onSuccess(result: OP): Unit = cb(Right(result))
//        }
//      )
//    }
  }
}

private object ExecutionException {
  def unapply(error: ExecutionException): Option[Throwable] = Option(error.getCause)
}
