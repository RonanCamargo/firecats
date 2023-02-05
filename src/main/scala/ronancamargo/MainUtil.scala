package ronancamargo

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import com.google.firebase.{FirebaseApp, FirebaseOptions}

import java.io.FileInputStream
import scala.util.chaining._

trait MainUtil {
  val serviceAccount   = new FileInputStream("src/main/resources/firebase-key.json")
  val options          = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).build
  val app: FirebaseApp = FirebaseApp.initializeApp(options)
  implicit val firestore: Firestore = FirestoreClient.getFirestore(app)

  implicit class Printer[E <: Throwable, A](private val printable: IO[Either[E, A]]) {
    def runTimedAndPrint: (Long, Either[E, A]) = printable.timed
      .map { case (duration, value) => (duration.toMillis, value) }
      .unsafeRunSync()
      .tap(println)
  }

}
