package ronancamargo.firestore.codec

trait FirestoreEncoder[A] { self =>
  def apply(entity: A): FirestoreDocument

  def contramap[B](f: B => A): FirestoreEncoder[B] = new FirestoreEncoder[B] {
    override def apply(a: B): FirestoreDocument = self(f(a))
  }
}

object FirestoreEncoder {
  def instance[A](f: A => Map[String, AnyRef]): FirestoreEncoder[A] = new FirestoreEncoder[A] {
    override def apply(entity: A): FirestoreDocument = FirestoreDocument(f(entity))
  }
}
