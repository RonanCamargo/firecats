package ronancamargo.firestore.v3

import cats.effect.IO
import ronancamargo.firestore.errors.FirestoreError

package object aliases {
  type FirestoreOperation[F[_], A] = F[Either[FirestoreError, A]]
  type FirestoreIO[A]              = FirestoreOperation[IO, A]
}
