package ronancamargo.firestore.client

import cats.effect.Sync
import com.google.cloud.firestore.{DocumentReference, DocumentSnapshot, Firestore}
import mouse.any._
import mouse.feither._
import ronancamargo.firestore.codec
import ronancamargo.firestore.codec.{FirestoreDecoder, FirestoreEncoder}
import ronancamargo.firestore.errors.FirestoreError

trait FirestoreClientDocRefF[F[_]] {
  protected val database: Firestore

  def set[A](doc: A, docRef: DocumentReference)(implicit
      F: Sync[F],
      encoder: FirestoreEncoder[A]
  ): F[Either[FirestoreError, A]] = {
    F.blocking(docRef.set(encoder(doc).document).get())
      .|>(F.as(_, doc))
      .|>(F.attempt)
      .leftMapIn(FirestoreError.unexpectedFirestoreError)
  }

  def get[A](docRef: DocumentReference)(implicit
      F: Sync[F],
      decoder: FirestoreDecoder[A]
  ): F[Either[FirestoreError, Option[A]]] =
    F.blocking(docRef.get().get())
      .|>(F.map(_)(maybeDS => Option(maybeDS).map(ds => decoder.decode(codec.FirestoreDocument(ds.getData)))))
      .|>(F.attempt)
      .leftMapIn(FirestoreError.unexpectedFirestoreError)

  def del(docRef: DocumentReference)(implicit F: Sync[F]): F[Either[FirestoreError, Unit]] =
    F.blocking(docRef.delete().get())
      .|>(F.void)
      .|>(F.attempt)
      .leftMapIn(FirestoreError.unexpectedFirestoreError)

  def create[A](doc: A, docRef: DocumentReference)(implicit
      F: Sync[F],
      encoder: FirestoreEncoder[A]
  ): F[Either[FirestoreError, A]] = {
    F.blocking(docRef.create(encoder(doc).document).get())
      .|>(F.as(_, doc))
      .|>(F.attempt)
      .leftMapIn(FirestoreError.unexpectedFirestoreError)
  }

  def create2[A](doc: A, docRef: DocumentReference)(implicit
      F: Sync[F],
      encoder: FirestoreEncoder[A]
  ): F[Either[FirestoreError, A]] = {
    attemptBlock[A](F.delay(docRef.create(encoder(doc).document).get()).|>(F.as(_, doc)))
  }

  def update[A](docRef: DocumentReference)(f: A => A)(implicit
      F: Sync[F],
      decoder: FirestoreDecoder[A],
      encoder: FirestoreEncoder[A]
  ): F[Either[FirestoreError, A]] = findAndUpdate[A, A](docRef)(f)

  def findAndUpdate[A, B](docRef: DocumentReference)(f: A => B)(implicit
      F: Sync[F],
      decoder: FirestoreDecoder[A],
      encoder: FirestoreEncoder[B]
  ): F[Either[FirestoreError, B]] = {
    F.blocking(
      database
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

  def attemptBlock[A](fa: F[A])(implicit F: Sync[F]): F[Either[FirestoreError, A]] =
    (F.blocking(fa) |> F.flatten |> F.attempt)
      .leftMapIn(FirestoreError.unexpectedFirestoreError)
}
