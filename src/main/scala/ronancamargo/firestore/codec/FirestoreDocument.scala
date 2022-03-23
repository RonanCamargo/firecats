package ronancamargo.firestore.codec

import ronancamargo.firestore.JavaMap

case class FirestoreDocument(document: JavaMap[String, AnyRef])
