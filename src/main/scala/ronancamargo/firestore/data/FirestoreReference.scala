package ronancamargo.firestore.data

import com.google.cloud.firestore.{DocumentReference, Firestore}

import scala.annotation.tailrec

case class FirestoreReference(
    private val collectionName: String,
    private val documentReference: String,
    private val collectionReference: Option[FirestoreReference] = None
) {
  def docReference(implicit firestore: Firestore): DocumentReference = {
    println(s"Firestore: $firestore")
    val result = firestore
      .collection(collectionName)
      .document(documentReference)

    val n = nestedReference(result, collectionReference)
    println(n)
    n
  }

  @tailrec
  private def nestedReference(
      documentReference: DocumentReference,
      maybeReference: Option[FirestoreReference]
  ): DocumentReference = {
    maybeReference match {
      case Some(value) =>
        nestedReference(
          documentReference.collection(value.collectionName).document(value.documentReference),
          value.collectionReference
        )
      case None        => documentReference
    }
  }
}
