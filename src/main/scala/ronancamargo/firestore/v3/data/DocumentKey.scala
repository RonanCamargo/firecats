package ronancamargo.firestore.v3.data

final case class DocumentKey(keys: List[String])

object DocumentKey {
  def apply(keys: String*): DocumentKey = DocumentKey(keys.toList)
}
