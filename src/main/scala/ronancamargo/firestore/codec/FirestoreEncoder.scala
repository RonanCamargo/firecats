package ronancamargo.firestore.codec

trait FirestoreEncoder[A] {
  def encode(entity: A): FirestoreDocument
}
