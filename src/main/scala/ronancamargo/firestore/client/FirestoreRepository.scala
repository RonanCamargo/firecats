package ronancamargo.firestore.client

import cats.effect.Sync
import cats.implicits._
import com.google.api.core.ApiFuture
import com.google.api.gax.rpc.AlreadyExistsException
import com.google.cloud.firestore.{DocumentReference, Firestore}
import mouse.all._
import ronancamargo.firestore.codec.{FirestoreDecoder, FirestoreDocument, FirestoreEncoder}
import ronancamargo.firestore.data.{CollectionHierarchy, DocumentKey}
import ronancamargo.firestore.errors.{DocumentNotFoundError, FirestoreError, InvalidReference}

import scala.concurrent.ExecutionException

abstract class FirestoreRepository[F[_] : Sync, A](
    database: Firestore,
    collectionHierarchy: CollectionHierarchy
) {

  private val syncF: Sync[F] = implicitly[Sync[F]]

  def set(doc: A, key: DocumentKey)(implicit
      encoder: FirestoreEncoder[A]
  ): F[Either[FirestoreError, A]] =
    syncF
      .fromEither(documentReference(collectionHierarchy, key, database))
      .tupleRight(encoder.encode(doc).document)
      .flatMap { case (ref, doc) => syncF.blocking(ref.set(doc).await) }
      .as(doc)
      .attempt
      .leftMapIn(errorHandler)

  def get(key: DocumentKey)(implicit
      decoder: FirestoreDecoder[A]
  ): F[Either[FirestoreError, A]] =
    getOption(key).flatMapIn(
      _.toRight(
        DocumentNotFoundError(s"Document with key: $key was not found in collection: $collectionHierarchy")
      )
    )

  def getOption(
      key: DocumentKey
  )(implicit decoder: FirestoreDecoder[A]): F[Either[FirestoreError, Option[A]]] =
    syncF
      .fromEither(documentReference(collectionHierarchy, key, database))
      .flatMap { ref => syncF.blocking(Option(ref.get().await.getData)) }
      .mapIn(FirestoreDocument(_))
      .mapIn(decoder.decode)
      .attempt
      .leftMapIn(errorHandler)

  def del(key: DocumentKey)(): F[Either[FirestoreError, Unit]] =
    syncF
      .fromEither(documentReference(collectionHierarchy, key, database))
      .flatMap { ref => syncF.blocking(ref.delete().get()) }
      .void
      .attempt
      .leftMapIn(errorHandler)

  def create(doc: A, key: DocumentKey)(implicit encoder: FirestoreEncoder[A]): F[Either[FirestoreError, A]] =
    syncF
      .fromEither(documentReference(collectionHierarchy, key, database))
      .tupleRight(encoder.encode(doc).document)
      .flatMap { case (ref, doc) => syncF.blocking(ref.create(doc).await) }
      .as(doc)
      .attempt
      .leftMapIn(errorHandler)

  def unsafeUpdate(doc: A, key: DocumentKey)(implicit
      encoder: FirestoreEncoder[A]
  ): F[Either[FirestoreError, A]] = {
    syncF
      .fromEither(documentReference(collectionHierarchy, key, database))
      .tupleRight(encoder.encode(doc).document)
      .flatMap { case (ref, doc) => syncF.blocking(ref.update(doc).await) }
      .as(doc)
      .attempt
      .leftMapIn(errorHandler)
  }

  def updateProjection[B](
      key: DocumentKey
  )(f: A => B)(implicit decoder: FirestoreDecoder[A], encoder: FirestoreEncoder[B]): F[Either[FirestoreError, B]] =
    syncF
      .blocking {
        database.runTransaction { tx =>
          val txResult = for {
            reference <- documentReference(collectionHierarchy, key, database)
            data      = reference.get().await.getData
            decoded   = decoder.decode(FirestoreDocument(data))
            projected = f(decoded)
            encoded   = encoder.encode(projected)
            _         = tx.update(reference, encoded.document)
          } yield projected
          txResult.leftWiden[FirestoreError]
        }.await
      }
      .attempt
      .leftMapIn(errorHandler)
      .map(_.flatten)

  def update(key: DocumentKey)(
      f: A => A
  )(implicit decoder: FirestoreDecoder[A], encoder: FirestoreEncoder[A]): F[Either[FirestoreError, A]] =
    updateProjection(key)(f)

  def errorHandler: Throwable => FirestoreError = {
    case ExecutionException(_: AlreadyExistsException) => FirestoreError.documentAlreadyExists
    case error: FirestoreError                         => error
    case error                                         => FirestoreError.unexpectedFirestoreError(error)
  }

  private def documentReference(
      collectionNames: CollectionHierarchy,
      key: DocumentKey,
      firestore: Firestore
  ): Either[InvalidReference, DocumentReference] =
    (collectionNames.collections, key.keys) match {
      case (collections, keys) if collections.size != keys.size =>
        InvalidReference(s"Keys: $key are invalid. Check keys size.").asLeft

      case (Nil, Nil) => InvalidReference("Collection names and keys are empty.").asLeft

      case (headCollection :: tailCollections, headKey :: tailKeys) =>
        val seedDocRef = firestore.collection(headCollection).document(headKey)
        (tailCollections zip tailKeys)
          .foldLeft(seedDocRef) { case (docRef, (col, key)) => docRef.collection(col).document(key) }
          .asRight
    }

  implicit class ApiFutureOps[OP](future: ApiFuture[OP]) {
    def await: OP = future.get()
  }
}

object ExecutionException {
  def unapply(error: ExecutionException): Option[Throwable] = Option(error.getCause)
}
