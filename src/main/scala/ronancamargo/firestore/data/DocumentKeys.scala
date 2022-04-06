package ronancamargo.firestore.data

case class DocumentKeys(keys: List[String])

object DocumentKeys {
  def apply(keys: String*): DocumentKeys = DocumentKeys(keys.toList)
}
