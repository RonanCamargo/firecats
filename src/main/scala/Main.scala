import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.{DocumentReference, Firestore}
import com.google.firebase.cloud.FirestoreClient
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import ronancamargo.firestore.client.FirestoreClientDocRefFAttemptCollection
import ronancamargo.firestore.data.FirestoreReference
import ronancamargo.firestore.errors.FirestoreError
import ronancamargo.firestore.syntax.runners._
import ronancamargo.firestore.tryout.{Daruma, Person}

import java.io.FileInputStream
import scala.util.chaining._

object Main extends App {

  val serviceAccount = new FileInputStream("src/main/resources/firebase-key.json")
  val options        = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).build
  val app: FirebaseApp   = FirebaseApp.initializeApp(options)
  implicit val firestore = FirestoreClient.getFirestore(app)

//  val client  = FirestoreClientDocRef(firestore)
//  val docRef  = firestore.collection("person").document("1")
//  val docRef2 = firestore.collection("person").document("2")

//  val faClient = new FirestoreClientDocRefFAttempt[IO] {
//    override protected val database: Firestore = firestore
//  }
//  val mapRef   = firestore.collection("person").document("3")
//  faClient
//    .getMap(mapRef)
//    .runSync
//    .map(_.asScala)
//    .map(dataMap =>
//      dataMap.map {
//        case (key, null)                                                           => println(s"Key: $key, Value: null")
//        case (key, value) if value.isInstanceOf[DocumentReference]                 =>
//          println(s"Document: ${faClient.getMap(value.asInstanceOf[DocumentReference]).runSync}")
//        case (key, value) if value.isInstanceOf[java.util.HashMap[String, AnyRef]] =>
//          println(s"Key: $key, Value: $value, Value type: ${value.getClass} ")
//          value.asInstanceOf[java.util.HashMap[String, AnyRef]].asScala.foreach { case (k, v) =>
//            println(s"\tKey: $k, Value: $v, Value type: ${v.getClass}")
//          }
//        case (key, value) => println(s"Key: $key, Value: $value, Value type: ${value.getClass}")
//      }
//    )
  val darumaDocRef: DocumentReference = firestore.collection("daruma").document("1")

  val nomadesClient = DarumaTeamNomadesFirestoreRepo(firestore)

  val ronan = Person("Ronan", 28)
  nomadesClient.set(ronan, "1", "123123").runSync.pipe(println)
  nomadesClient.get[Person]("1", "123123").runSync.pipe(p => println(s"Found: $p"))

  val darumaClient = DarumaFirestoreRepo(firestore)
  darumaClient.set(doc = Daruma("daruma", "30123123129"), "1").runSync.pipe(println)

  darumaClient.update[Daruma](darumaDocRef) { daruma => daruma.copy(cuit = "0000") }.runSync

//  import monocle.syntax.all._
//  darumaClient.update[Daruma](darumaDocRef)(_.focus(_.name).replace("Daruminho")).runSync
}

case class DarumaTeamNomadesFirestoreRepo(fs: Firestore) extends FirestoreClientDocRefFAttemptCollection[IO] {
  override protected val database: Firestore = fs
  override val collections                   = List("daruma", "nomades")
}

case class DarumaTeamBackAirFirestoreRepo(fs: Firestore) extends FirestoreClientDocRefFAttemptCollection[IO] {
  override protected val database: Firestore = fs
  override val collections                   = List("daruma", "backAir")
}

case class DarumaFirestoreRepo(fs: Firestore) extends FirestoreClientDocRefFAttemptCollection[IO] {
  override protected val database: Firestore = fs
  override val collections                   = List("daruma")
}
