package ronancamargo.firestore.codec

import ronancamargo.firestore.data.FirestoreDocument

import scala.jdk.CollectionConverters._

  trait FirestoreDecoder[A] {
  def decode(document: FirestoreDocument): A
}

object FirestoreDecoder {
  def instance[A](f: Map[String, AnyRef] => A): FirestoreDecoder[A] = new FirestoreDecoder[A] {
    override def decode(document: FirestoreDocument): A = f(document.document.asScala.toMap)
  }
}
