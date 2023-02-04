package ronancamargo.firestore.v3.errors

import cats.Show
import cats.syntax.show._
import shapeless.HList

sealed abstract class FirestoreError(message: String, cause: Option[Throwable] = None)
    extends RuntimeException(message, cause.orNull)

final case class InvalidDocumentReference(message: String) extends FirestoreError(message)

final case class DocumentNotFoundError(message: String) extends FirestoreError(message)

final case class DocumentAlreadyExists(message: String) extends FirestoreError(message)

final case class UnexpectedFirestoreError(message: String, cause: Throwable)
    extends FirestoreError(message, Some(cause))

object FirestoreError {
  def documentNotFound[D <: HList : Show](key: D, collectionHierarchy: D): FirestoreError =
    DocumentNotFoundError(s"Document with key: ${key.show} was not found in collection: ${collectionHierarchy.show}")
  def documentAlreadyExists: FirestoreError                  = DocumentAlreadyExists("Document already exists")
  def unexpected(error: Throwable): UnexpectedFirestoreError =
    UnexpectedFirestoreError(s"Unexpected Firestore error. ${error.getMessage}", error)
}
