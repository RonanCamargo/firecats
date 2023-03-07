package ronancamargo.firestore.syntax

import ronancamargo.firestore.codec._
import ronancamargo.firestore.data.FirestoreDocument
import ronancamargo.firestore.{JavaList, JavaMap}

import scala.jdk.CollectionConverters._

object codec {
  implicit final class DecodeAnyRef(private val anyRef: AnyRef) extends AnyVal {
    def decodeObject[A](implicit decoder: FirestoreDecoder[A]): A =
      decoder.decode(FirestoreDocument(anyRef.decodeJavaMap))

    def decodeField[A](implicit fieldDecoder: FirestoreFieldDecoder[A]): A = fieldDecoder.decodeField(anyRef)

    def decodeJavaMap: JavaMap[String, AnyRef]                        = anyRef.asInstanceOf[JavaMap[String, AnyRef]]
    def decodeList[A](implicit decoder: FirestoreDecoder[A]): List[A] =
      Option(anyRef).map(_.asInstanceOf[JavaList[AnyRef]]).fold(List.empty[A])(_.asScala.map(_.decodeObject).toList)

  }

  implicit final class EncodeAnyRef[A](private val value: A) extends AnyVal {
    def encodeObject(implicit encoder: FirestoreEncoder[A]): AnyRef =
      encoder(value).document.asInstanceOf[AnyRef]

    def encodeField[B](implicit ev: A =:= B, fieldEncoder: FirestoreFieldEncoder[B]): AnyRef =
      fieldEncoder.encodeField(ev(value))
  }
}
