package ronancamargo.firestore.data

import com.google.cloud.firestore.DocumentSnapshot
import ronancamargo.firestore.JavaMap

import scala.jdk.CollectionConverters._

  final case class FirestoreDocument(document: JavaMap[String, AnyRef])

object FirestoreDocument {
  def apply(map: Map[String, AnyRef]): FirestoreDocument = FirestoreDocument(map.asJava)

  def apply(documentSnapshot: DocumentSnapshot): FirestoreDocument = FirestoreDocument(documentSnapshot.getData)
}
