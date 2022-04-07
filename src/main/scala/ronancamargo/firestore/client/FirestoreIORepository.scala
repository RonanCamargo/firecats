package ronancamargo.firestore.client

import cats.effect.IO
import com.google.cloud.firestore.Firestore
import ronancamargo.firestore.data.CollectionHierarchy

abstract class FirestoreIORepository[A](database: Firestore, collectionHierarchy: CollectionHierarchy)
    extends FirestoreRepository[IO, A](database, collectionHierarchy) {}
