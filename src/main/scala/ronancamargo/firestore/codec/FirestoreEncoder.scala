package ronancamargo.firestore.codec

trait FirestoreEncoder[A] {
  def encode(entity: A): FirestoreDocument
}

object FirestoreEncoder {
  def instance[A](f: A => Map[String, AnyRef]): FirestoreEncoder[A] = new FirestoreEncoder[A] {
    override def encode(entity: A): FirestoreDocument = FirestoreDocument(f(entity))
  }
}
