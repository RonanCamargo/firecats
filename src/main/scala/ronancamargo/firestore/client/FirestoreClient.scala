package ronancamargo.firestore.client

import cats.effect.Async
import com.google.cloud.firestore.{Firestore, WriteBatch}
import mouse.any._
import ronancamargo.firestore.codec.{FirestoreDecoder, FirestoreDocument, FirestoreEncoder}

case class FirestoreClient(firestore: Firestore) {

  def set[F[_], A](docId: String, doc: A, collection: String)(implicit
      F: Async[F],
      encoder: FirestoreEncoder[A]
  ): F[Either[Throwable, A]] = {
    val docRef = firestore.collection(collection).document(docId)
    F.blocking(docRef.set(encoder.encode(doc)).get())
      .|>(F.as(_, doc))
      .|>(F.attempt)
  }

  def get[F[_], A](docId: String, collection: String)(implicit
      F: Async[F],
      decoder: FirestoreDecoder[A]
  ): F[Either[Throwable, A]] = {
    val docRef = firestore.collection(collection).document(docId)
    F.blocking(docRef.get().get())
      .|>(F.map(_)(ds => decoder.decode(FirestoreDocument(ds.getData))))
      .|>(F.attempt)
  }

  def del[F[_]](docId: String, collection: String)(implicit F: Async[F]): F[Either[Throwable, Unit]] = {
    val docRef = firestore.collection(collection).document(docId)
    F.blocking(docRef.delete().get())
      .|>(F.void)
      .|>(F.attempt)
  }

  def create[F[_], A](docId: String, doc: A, collection: String)(implicit
      F: Async[F],
      encoder: FirestoreEncoder[A]
  ): F[Either[Throwable, A]] = {
    val docRef = firestore.collection(collection).document(docId)
    F.blocking(docRef.create(encoder.encode(doc)).get())
      .|>(F.as(_, doc))
      .|>(F.attempt)
  }

  def update[F[_], A](docId: String, f: A => A, collection: String)(implicit
      F: Async[F],
      decoder: FirestoreDecoder[A],
      encoder: FirestoreEncoder[A]
  ): F[Either[Throwable, A]] = {
    val docRef = firestore.collection(collection).document(docId)
    F.blocking(
      firestore
        .runTransaction { tx =>
          val doc        = decoder.decode(FirestoreDocument(tx.get(docRef).get().getData))
          val updated: A = f(doc)
          tx.update(docRef, encoder.encode(updated).document)
          updated
        }
        .get()
    ).|>(F.attempt)
  }

  def txSet[A](docId: String, doc: A, collection: String)(implicit encoder: FirestoreEncoder[A]): WriteBatch = {
    val docRef        = firestore.collection(collection).document(docId)
    firestore.runTransaction { tx =>
      tx.set(docRef, encoder.encode(doc))
      doc
    }
    val b: WriteBatch = firestore.batch()
    b.set(docRef, encoder.encode(doc))
  }

  def setNested2[F[_], A](
      docId: String,
      doc: A,
      collection: String,
      parentId: String,
      parentCollection: String
  )(implicit
      F: Async[F],
      encoder: FirestoreEncoder[A]
  ): F[Either[Throwable, A]] = {
    val document = encoder.encode(doc)
    val docRef   = firestore.collection(parentCollection).document(parentId).collection(collection).document(docId)
    F.blocking(docRef.set(document).get())
      .|>(F.as(_, doc))
      .|>(F.attempt)
  }
}
