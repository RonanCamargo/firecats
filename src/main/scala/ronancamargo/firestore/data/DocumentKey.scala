package ronancamargo.firestore.data

final case class DocumentKey(keys: List[String])

object DocumentKey {
  def apply(keys: String*): DocumentKey = DocumentKey(keys.toList)
}
