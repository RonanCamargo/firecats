package ronancamargo.firestore.codec

trait FirestoreDecoder[A] {
  def decode(document: FirestoreDocument): A
}
