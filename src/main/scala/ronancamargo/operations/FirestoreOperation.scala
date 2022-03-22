package ronancamargo.operations

sealed trait FirestoreOperation

case class GetById() extends FirestoreOperation
case class Set()     extends FirestoreOperation
case class Delete()  extends FirestoreOperation
case class Update()  extends FirestoreOperation
