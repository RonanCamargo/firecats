package ronancamargo.firestore.operations

import ronancamargo.firestore.data.FirestoreReference

sealed trait FirestoreOperation

case class GetById(id: String, nested: Option[FirestoreReference] = None)             extends FirestoreOperation
case class Set[A](id: String, document: A, nested: Option[FirestoreReference] = None) extends FirestoreOperation
case class Delete(id: String)                                                         extends FirestoreOperation
// case class Update()                                                                 extends FirestoreOperation
