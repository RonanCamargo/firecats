import cats.effect.IO
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.{DocumentReference, Firestore}
import com.google.firebase.cloud.FirestoreClient
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import ronancamargo.firestore.client.FirestoreIORepository
import ronancamargo.firestore.data.{CollectionHierarchy, DocumentKey}
import ronancamargo.firestore.errors.FirestoreError
import ronancamargo.firestore.syntax.runners._
import ronancamargo.firestore.tryout.{Person, PersonDocument}

import java.io.FileInputStream
import scala.util.chaining._

object Main extends App {

  val serviceAccount = new FileInputStream("src/main/resources/firebase-key.json")
  val options        = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).build
  val app: FirebaseApp              = FirebaseApp.initializeApp(options)
  implicit val firestore: Firestore = FirestoreClient.getFirestore(app)
  implicit class Printer[A](printable: IO[Either[FirestoreError, A]]) {
    def runAndPrint: Either[FirestoreError, A] = printable.runSync.tap(println)
  }

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
  val ronan                           = PersonDocument("123", "1", "Ronan", 28)
  val myTeamKey                       = DocumentKey("1", "123")

  val teamRepo = TeamFirestoreRepo(firestore)
  teamRepo.get(DocumentKey("1", "1")).runAndPrint
  teamRepo.set(ronan).runAndPrint
  teamRepo.getOption(myTeamKey).runAndPrint
  teamRepo.create(ronan).runAndPrint

  val capitalized = ronan.copy(name = ronan.name.toUpperCase)

  teamRepo.unsafeUpdate(capitalized).runAndPrint
  teamRepo.update(myTeamKey)(_.copy(age = 20)).runAndPrint
  teamRepo.updateProjection(myTeamKey)(_ => Person("", 100)).runAndPrint
}

case class TeamFirestoreRepo(fs: Firestore)
    extends FirestoreIORepository[PersonDocument](fs, CollectionHierarchy("team", "nomades")) {
  override def keyFromDoc: PersonDocument => DocumentKey = person => DocumentKey(person.id)
}
