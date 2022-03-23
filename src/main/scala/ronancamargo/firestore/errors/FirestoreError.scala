package ronancamargo.firestore.errors

sealed abstract class FirestoreError(message: String, cause: Option[Throwable] = None)
    extends RuntimeException(message, cause.orNull)

final case class UnexpectedFirestoreError(message: String, cause: Throwable)
    extends FirestoreError(message, Some(cause))

object FirestoreError {
  def unexpectedFirestoreError(error: Throwable): UnexpectedFirestoreError =
    UnexpectedFirestoreError(s"Unexpected Firestore error. ${error.getMessage}", error)
}
