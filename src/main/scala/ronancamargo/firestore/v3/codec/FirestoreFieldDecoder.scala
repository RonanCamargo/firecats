package ronancamargo.firestore.v3.codec

import ronancamargo.firestore.v3.codec.instances.FirestoreFieldDecoderInstances

trait FirestoreFieldDecoder[A] { self =>
  def decodeField(field: AnyRef): A

  def map[B](f: A => B): FirestoreFieldDecoder[B] =
    FirestoreFieldDecoder.instance[B](field => f(self.decodeField(field)))
}

object FirestoreFieldDecoder extends FirestoreFieldDecoderInstances {
  def instance[A](decode: AnyRef => A): FirestoreFieldDecoder[A] = new FirestoreFieldDecoder[A] {
    override def decodeField(field: AnyRef): A = decode(field)
  }
}
