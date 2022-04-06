package ronancamargo.firestore.data

case class CollectionNames(collections: List[String])

object CollectionNames {
  def apply(collections: String*): CollectionNames = CollectionNames(collections.toList)
}
