package ronancamargo.firestore.client

import cats.effect.Sync
import com.google.cloud.firestore.{DocumentReference, DocumentSnapshot, Firestore}
import mouse.any._
import mouse.feither._
import ronancamargo.firestore.codec.{FirestoreDecoder, FirestoreEncoder}
import ronancamargo.firestore.errors.FirestoreError
import ronancamargo.firestore.{codec, JavaMap}

trait FirestoreClientDocRefFAttempt[F[_]] {
  protected val database: Firestore

  def getMap(docRef: DocumentReference)(implicit F: Sync[F]): F[Either[FirestoreError, JavaMap[String, AnyRef]]] =
    F.blocking(docRef.get().get().getData)
      .|>(F.attempt)
      .leftMapIn(FirestoreError.unexpectedFirestoreError)

  def set[A](doc: A, docRef: DocumentReference)(implicit
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
