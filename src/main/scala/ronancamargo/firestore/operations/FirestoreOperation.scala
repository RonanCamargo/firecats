package ronancamargo.firestore.operations

sealed trait FirestoreOperation

case class GetById(id: String, nested: Option[FirestoreCollectionReference] = None) extends FirestoreOperation
case class Set[A](id: String, document: A, nested: Option[FirestoreCollectionReference] = None)
    extends FirestoreOperation
case class Delete(id: String)                                                       extends FirestoreOperation
// case class Update()                                                                 extends FirestoreOperation

case class FirestoreCollectionReference(
    collectionName: String,
    documentReference: Option[String],
    collectionReference: Option[FirestoreCollectionReference] = None
)
