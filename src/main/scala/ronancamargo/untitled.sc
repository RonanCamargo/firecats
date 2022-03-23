import cats.effect.IO
import cats.effect.kernel.Async
import cats.effect.unsafe.implicits.global
import com.google.cloud.firestore.Firestore
import mouse.any._
import ronancamargo.firestore.codec.FirestoreEncoder

def blocking[F[_], A](block: => A)(implicit F: Async[F]): F[Either[Throwable, A]] =
  F.blocking(block) |> F.attempt

blocking[IO, Int](1).unsafeRunSync()

val firestore: Firestore                    = ???
implicit val encoder: FirestoreEncoder[Int] = ???
val docRef                                  = firestore.collection("").document("")
