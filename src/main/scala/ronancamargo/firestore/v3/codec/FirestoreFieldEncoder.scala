package ronancamargo.firestore.v3.codec

import ronancamargo.firestore.v3.codec.instances.FirestoreFieldEncoderInstances

trait FirestoreFieldEncoder[A] { self =>
  def encodeField(field: A): AnyRef

  def contramap[B](f: B => A): FirestoreFieldEncoder[B] = new FirestoreFieldEncoder[B] {
    override def encodeField(field: B): AnyRef = self.encodeField(f(field))
  }
}

object FirestoreFieldEncoder extends FirestoreFieldEncoderInstances {
  def instance[A](f: A => AnyRef): FirestoreFieldEncoder[A] = new FirestoreFieldEncoder[A] {
    override def encodeField(field: A): AnyRef = f(field)
  }
}
