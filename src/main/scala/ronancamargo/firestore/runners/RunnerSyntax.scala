package ronancamargo.firestore.runners

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import ronancamargo.firestore.errors.FirestoreError

object RunnerSyntax {
  implicit class IORunner[A](firestoreIO: IO[Either[FirestoreError, A]]) {
    def runSync: Either[FirestoreError, A] = firestoreIO.unsafeRunSync()
  }

//  implicit class FRunner[F[_], A](firestoreIO: F[Either[FirestoreError, A]]) {
//    def run[G[_]](implicit
//        ev: G[_] =:= F[_],
//        ev2: F[Either[FirestoreError, A]] =:= IO[Either[FirestoreError, A]]
//    ): Either[FirestoreError, A] = {
//      val io: IO[Either[FirestoreError, A]] = firestoreIO
//      io.unsafeRunSync()
//    }
//  }
}
