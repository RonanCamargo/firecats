package ronancamargo.firestore.codec

trait FirestoreDecoder[A] {
  def decode(document: FirestoreDocument): A
}

object FirestoreDecoder {
  import scala.jdk.CollectionConverters._
  def instance[A](f: Map[String, AnyRef] => A): FirestoreDecoder[A] = new FirestoreDecoder[A] {
    override def decode(document: FirestoreDocument): A = f(document.document.asScala.toMap)
  }
}
