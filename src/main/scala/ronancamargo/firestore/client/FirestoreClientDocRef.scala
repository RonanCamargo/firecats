package ronancamargo.firestore.client

import cats.effect.Sync
import com.google.cloud.firestore.{DocumentReference, DocumentSnapshot, Firestore}
import mouse.any._
import mouse.feither._
import ronancamargo.firestore.codec
import ronancamargo.firestore.codec.{FirestoreDecoder, FirestoreEncoder}
import ronancamargo.firestore.errors.FirestoreError

case class FirestoreClientDocRef(private val firestore: Firestore) {

  def set[F[_], A](doc: A, docRef: DocumentReference)(implicit
      F: Sync[F],
      encoder: FirestoreEncoder[A]
  ): F[Either[FirestoreError, A]] = {
    F.blocking(docRef.set(encoder(doc).document).get())
      .|>(F.as(_, doc))
      .|>(F.attempt)
      .leftMapIn(FirestoreError.unexpectedFirestoreError)
  }

  def get[F[_], A](docRef: DocumentReference)(implicit
      F: Sync[F],
      decoder: FirestoreDecoder[A]
  ): F[Either[FirestoreError, A]] =
    F.blocking(docRef.get().get())
      .|>(F.map(_)(ds => decoder.decode(codec.FirestoreDocument(ds.getData))))
      .|>(F.attempt)
      .leftMapIn(FirestoreError.unexpectedFirestoreError)

  def del[F[_]](docRef: DocumentReference)(implicit F: Sync[F]): F[Either[FirestoreError, Unit]] =
    F.blocking(docRef.delete().get())
      .|>(F.void)
      .|>(F.attempt)
      .leftMapIn(FirestoreError.unexpectedFirestoreError)

  def create[F[_], A](doc: A, docRef: DocumentReference)(implicit
      F: Sync[F],
      encoder: FirestoreEncoder[A]
  ): F[Either[FirestoreError, A]] = {
    F.blocking(docRef.create(encoder(doc).document).get())
      .|>(F.as(_, doc))
      .|>(F.attempt)
      .leftMapIn(FirestoreError.unexpectedFirestoreError)
  }

  def create2[F[_], A](doc: A, docRef: DocumentReference)(implicit
      F: Sync[F],
      encoder: FirestoreEncoder[A]
  ): F[Either[FirestoreError, A]] = {
    attemptBlocking[F, A](F.delay(docRef.create(encoder(doc).document).get()).|>(F.as(_, doc)))
  }

  def update[F[_], A](docRef: DocumentReference)(f: A => A)(implicit
      F: Sync[F],
      decoder: FirestoreDecoder[A],
      encoder: FirestoreEncoder[A]
  ): F[Either[FirestoreError, A]] = findAndUpdate[F, A, A](docRef)(f)

  def findAndUpdate[F[_], A, B](docRef: DocumentReference)(f: A => B)(implicit
      F: Sync[F],
      decoder: FirestoreDecoder[A],
      encoder: FirestoreEncoder[B]
  ): F[Either[FirestoreError, B]] = {
    F.blocking(
      firestore
        .runTransaction { tx =>
          val data: DocumentSnapshot = tx.get(docRef).get()
          val doc: A                 = decoder.decode(codec.FirestoreDocument(data.getData))
          val updated: B             = f(doc)
          tx.update(docRef, encoder(updated).document)
          updated
        }
        .get()
    ).|>(F.attempt)
      .leftMapIn(FirestoreError.unexpectedFirestoreError)
  }

  def attemptBlocking[F[_], A](fa: F[A])(implicit F: Sync[F]): F[Either[FirestoreError, A]] =
    (F.blocking(fa) |> F.flatten |> F.attempt)
      .leftMapIn(FirestoreError.unexpectedFirestoreError)
}
