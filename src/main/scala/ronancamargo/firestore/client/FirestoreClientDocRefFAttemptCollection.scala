package ronancamargo.firestore.client

import cats.effect.Sync
import com.google.cloud.firestore.{DocumentReference, DocumentSnapshot, Firestore}
import mouse.any._
import mouse.feither._
import ronancamargo.firestore.codec
import ronancamargo.firestore.codec.{FirestoreDecoder, FirestoreEncoder}
import ronancamargo.firestore.data.FirestoreReference
import ronancamargo.firestore.errors.FirestoreError

trait FirestoreClientDocRefFAttemptCollection[F[_]] {
  protected val database: Firestore

  protected val collections: List[String]

  private def createReference(collectionDocuments: List[(String, String)], fs: Firestore): DocumentReference = {
    val docRef: DocumentReference =
      collectionDocuments.tail.foldLeft(
        fs.collection(collectionDocuments.head._1).document(collectionDocuments.head._2)
      ) { case (accumDocRef, (k, v)) => accumDocRef.collection(k).document(v) }
    docRef
  }

  def set[A](doc: A, documentIds: String*)(implicit
      F: Sync[F],
      encoder: FirestoreEncoder[A]
  ): F[Either[FirestoreError, A]] = {
    println(s"Collections size: $collections, Docs: $documentIds")
    assert(collections.size == documentIds.size)
    val references: List[(String, String)] = (collections zip documentIds)
    val docRef                             = createReference(references, database)

    F.blocking(docRef.set(encoder.encode(doc).document).get())
      .|>(F.as(_, doc))
      .|>(F.attempt)
      .leftMapIn(FirestoreError.unexpectedFirestoreError)
  }

  def set[A](doc: A, docRef: FirestoreReference)(implicit
      F: Sync[F],
      encoder: FirestoreEncoder[A]
  ): F[Either[FirestoreError, A]] = {
    F.delay(docRef.docReference(database))
      .|>(F.map(_)(_.set(encoder.encode(doc).document).get()))
      .|>(F.as(_, doc))
      .|>(attemptBlocking)
  }

  def setDocRef[A](doc: A, docRef: DocumentReference)(implicit
      F: Sync[F],
      encoder: FirestoreEncoder[A]
  ): F[Either[FirestoreError, A]] =
    F.delay(docRef.set(encoder.encode(doc).document).get())
      .|>(F.as(_, doc))
      .|>(attemptBlocking)

  def get[A](docRef: DocumentReference)(implicit
      F: Sync[F],
      decoder: FirestoreDecoder[A]
  ): F[Either[FirestoreError, A]] =
    F.delay(docRef.get().get())
      .|>(F.map(_)(ds => decoder.decode(codec.FirestoreDocument(ds.getData))))
      .|>(attemptBlocking)

  def get[A](documentIds: String*)(implicit
      F: Sync[F],
      decoder: FirestoreDecoder[A]
  ): F[Either[FirestoreError, A]] = {
    assert(collections.size == documentIds.size)
    val references: List[(String, String)] = (collections zip documentIds)
    val docRef                             = createReference(references, database)

    F.delay(docRef.get().get())
      .|>(F.map(_)(ds => decoder.decode(codec.FirestoreDocument(ds.getData))))
      .|>(attemptBlocking)
  }

  def del(docRef: DocumentReference)(implicit F: Sync[F]): F[Either[FirestoreError, Unit]] =
    attemptBlocking(F.delay(docRef.delete().get()).|>(F.void))

  def create[A](doc: A, docRef: DocumentReference)(implicit
      F: Sync[F],
      encoder: FirestoreEncoder[A]
  ): F[Either[FirestoreError, A]] =
    attemptBlocking(F.delay(docRef.create(encoder.encode(doc).document).get()).|>(F.as(_, doc)))

  def update[A](docRef: DocumentReference)(f: A => A)(implicit
      F: Sync[F],
      decoder: FirestoreDecoder[A],
      encoder: FirestoreEncoder[A]
  ): F[Either[FirestoreError, A]] = findAndUpdate[A, A](docRef)(f)

  def findAndUpdate[A, B](docRef: DocumentReference)(f: A => B)(implicit
      F: Sync[F],
      decoder: FirestoreDecoder[A],
      encoder: FirestoreEncoder[B]
  ): F[Either[FirestoreError, B]] =
    attemptBlocking(
      F.delay(
        database
          .runTransaction { tx =>
            val data: DocumentSnapshot = tx.get(docRef).get()
            val doc: A                 = decoder.decode(codec.FirestoreDocument(data.getData))
            val updated: B             = f(doc)
            tx.update(docRef, encoder.encode(updated).document)
            updated
          }
          .get()
      )
    )

  def attemptBlocking[A](fa: F[A])(implicit F: Sync[F]): F[Either[FirestoreError, A]] =
    (F.blocking(fa) |> F.flatten |> F.attempt)
      .leftMapIn(FirestoreError.unexpectedFirestoreError)
}
