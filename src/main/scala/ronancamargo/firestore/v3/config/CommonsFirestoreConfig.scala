package ronancamargo.firestore.v3.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import mouse.any._

import java.io.InputStream

object CommonsFirestoreConfig {
  def createFirestore(configFileStream: InputStream): Firestore =
    FirebaseOptions
      .builder()
      .setCredentials(GoogleCredentials.fromStream(configFileStream))
      .build
      .|>(FirebaseApp.initializeApp)
      .|>(FirestoreClient.getFirestore)

  def freeUpResources(firestore: Firestore): Unit = firestore.close()
}
