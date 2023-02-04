package ronancamargo.firestore.v3.syntax

import ronancamargo.firestore.{JavaList, JavaMap}
import ronancamargo.firestore.v3.codec._
import ronancamargo.firestore.v3.data.FirestoreDocument

import scala.jdk.CollectionConverters._

object codec {
  implicit final class DecodeAnyRef(private val anyRef: AnyRef) extends AnyVal {
    @deprecated("Use decodeField[Int]", "0.10.2")
    def decodeInt: Int               = anyRef.asInstanceOf[Long].toInt
    @deprecated("Use decodeField[BigDecimal]", "0.10.2")
    def decodeBigDecimal: BigDecimal = BigDecimal(anyRef.asInstanceOf[Double])
    @deprecated("Use decodeField[String]", "0.10.2")
    def decodeString: String         = anyRef.asInstanceOf[String]
    @deprecated("Use decodeField[Double]", "0.10.2")
    def decodeDouble: Double         = anyRef.asInstanceOf[Double]

    def decodeObject[A](implicit decoder: FirestoreDecoder[A]): A =
      decoder.decode(FirestoreDocument(anyRef.decodeJavaMap))
    def decodeJavaMap: JavaMap[String, AnyRef]                    = anyRef.asInstanceOf[JavaMap[String, AnyRef]]
    def decodeField[A](implicit fieldDecoder: FirestoreFieldDecoder[A]): A = fieldDecoder.decodeField(anyRef)
    def decodeList[A](implicit decoder: FirestoreDecoder[A]): List[A]      =
      Option(anyRef).map(_.asInstanceOf[JavaList[AnyRef]]).fold(List.empty[A])(_.asScala.map(_.decodeObject).toList)
  }

  implicit final class EncodeAnyRef[A](private val value: A) extends AnyVal {
    def encodeInt(implicit ev: A =:= Int): AnyRef                   = value.asInstanceOf[AnyRef]
    def encodeBigDecimal(implicit ev: A =:= BigDecimal): AnyRef     = value.toDouble.asInstanceOf[AnyRef]
    def encodeString(implicit ev: A =:= String): AnyRef             = value.asInstanceOf[AnyRef]
    def encodeDouble(implicit ev: A =:= Double): AnyRef             = value.asInstanceOf[AnyRef]
    def encodeObject(implicit encoder: FirestoreEncoder[A]): AnyRef =
      encoder(value).document.asInstanceOf[AnyRef]
  }
}
