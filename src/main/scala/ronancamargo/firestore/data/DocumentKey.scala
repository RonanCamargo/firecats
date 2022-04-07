package ronancamargo.firestore.data

case class DocumentKey(keys: List[String])

object DocumentKey {
  def apply(keys: String*): DocumentKey = DocumentKey(keys.toList)
}
