package ronancamargo.firestore.tryout

import ronancamargo.firestore.codec.{FirestoreDecoder, FirestoreDocument, FirestoreEncoder}

case class Person(name: String, age: Int)

object Person {
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

}
