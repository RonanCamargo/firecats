import cats.effect.IO
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.{DocumentReference, Firestore, GeoPoint}
import com.google.firebase.cloud.FirestoreClient
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import ronancamargo.firestore.client.{FirestoreClientDocRef, FirestoreClientDocRefF, FirestoreClientDocRefFAttempt}
import ronancamargo.firestore.codec.{FirestoreDecoder, FirestoreDocument, FirestoreEncoder}
import ronancamargo.firestore.errors.FirestoreError
import ronancamargo.firestore.syntax.runners._
import shapeless.Poly1

import java.io.FileInputStream
import scala.collection.mutable
import scala.util.chaining._

object Main extends App {

  val serviceAccount = new FileInputStream("src/main/resources/firebase-key.json")
  val options        = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).build
  val app: FirebaseApp = FirebaseApp.initializeApp(options)
  val firestore        = FirestoreClient.getFirestore(app)
  val client           = FirestoreClientDocRef(firestore)

  case class Person(name: String, age: Int)
  implicit val personEncoder: FirestoreEncoder[Person] = new FirestoreEncoder[Person] {
    override def encode(entity: Person): FirestoreDocument =
      FirestoreDocument(
        Map("name" -> entity.name.asInstanceOf[AnyRef], "age" -> entity.age.asInstanceOf[AnyRef])
      )
  }

  implicit val personDecoder: FirestoreDecoder[Person] = new FirestoreDecoder[Person] {
    override def decode(document: FirestoreDocument): Person = {
      val doc = document.document
      Person(doc.get("name").asInstanceOf[String], doc.get("age").asInstanceOf[Long].toInt)
    }
  }

  val docRef                                  = firestore.collection("person").document("1")
  val docRef2                                 = firestore.collection("person").document("2")
  val set: IO[Either[FirestoreError, Person]] = client.set[IO, Person](Person("JIJI", 1), docRef2)

//  set.runSync
//  client.findAndUpdate[IO, Person, Person](docRef)(_ => Person("Marzovekio", 666)).runSync

  val fClient = new FirestoreClientDocRefF[IO] {
    override protected val database: Firestore = firestore
  }

  val r = for {
    gg    <- fClient.set(Person("GG", 666), docRef2)
    found <- fClient.get(docRef2)
  } yield found

//  r.runSync.pipe(println)

  val faClient = new FirestoreClientDocRefFAttempt[IO] {
    override protected val database: Firestore = firestore
  }
  import scala.jdk.CollectionConverters._

  val mapRef = firestore.collection("person").document("3")
  faClient
    .getMap(mapRef)
    .runSync
    .map(_.asScala)
    .map(dataMap =>
      dataMap.map {
        case (key, null)                                                           => println(s"Key: $key, Value: null")
        case (key, value) if value.isInstanceOf[DocumentReference]                 =>
          println(s"Document: ${faClient.getMap(value.asInstanceOf[DocumentReference]).runSync}")
        case (key, value) if value.isInstanceOf[java.util.HashMap[String, AnyRef]] =>
          println(s"Key: $key, Value: $value, Value type: ${value.getClass} ")
          value.asInstanceOf[java.util.HashMap[String, AnyRef]].asScala.foreach { case (k, v) =>
            println(s"\tKey: $k, Value: $v, Value type: ${v.getClass}")
          }
        case (key, value) => println(s"Key: $key, Value: $value, Value type: ${value.getClass}")
      }
    )
}
