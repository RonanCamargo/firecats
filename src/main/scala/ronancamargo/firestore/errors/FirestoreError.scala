package ronancamargo.firestore.errors

sealed abstract class FirestoreError(message: String, cause: Option[Throwable] = None)
    extends RuntimeException(message, cause.orNull)

final case class InvalidReference(message: String) extends FirestoreError(message)

final case class DocumentDecodingError(message: String) extends FirestoreError(message)

final case class DocumentNotFoundError(message: String) extends FirestoreError(message)

final case class DocumentAlreadyExists(message: String) extends FirestoreError(message)

final case class UnexpectedFirestoreError(message: String, cause: Throwable)
    extends FirestoreError(message, Some(cause))

object FirestoreError {
  def documentAlreadyExists: FirestoreError = DocumentAlreadyExists("Document already exists.")
  def unexpectedFirestoreError(error: Throwable): UnexpectedFirestoreError =
    UnexpectedFirestoreError(s"Unexpected Firestore error. ${error.getMessage}", error)
}
