package ronancamargo.firestore.data

final case class CollectionHierarchy(collections: List[String])

object CollectionHierarchy {
  def apply(collections: String*): CollectionHierarchy = CollectionHierarchy(collections.toList)
}
