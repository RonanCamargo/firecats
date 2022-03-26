package ronancamargo.firestore.codec

import ronancamargo.firestore.JavaMap

import scala.jdk.CollectionConverters._

case class FirestoreDocument(document: JavaMap[String, AnyRef])

object FirestoreDocument {
  def apply(map: Map[String, AnyRef]): FirestoreDocument = FirestoreDocument(map.asJava)
}
