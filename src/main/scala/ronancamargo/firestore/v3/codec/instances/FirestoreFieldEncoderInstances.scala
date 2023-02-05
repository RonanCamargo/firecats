package ronancamargo.firestore.v3.codec.instances

import ronancamargo.firestore.v3.codec.FirestoreFieldEncoder
import ronancamargo.firestore.v3.codec.FirestoreFieldEncoder.instance

import scala.jdk.CollectionConverters._

private[codec] trait FirestoreFieldEncoderInstances {
  implicit def simpleEncoder[A]: FirestoreFieldEncoder[A] = instance[A](_.asInstanceOf[AnyRef])

  implicit val intEncoder        = simpleEncoder[Int]
  implicit val longEncoder       = simpleEncoder[Long]
  implicit val doubleEncoder     = simpleEncoder[Double]
  implicit val booleanEncoder    = simpleEncoder[Boolean]
  implicit val stringEncoder     = simpleEncoder[String]
  implicit val bigDecimalEncoder = doubleEncoder.contramap[BigDecimal](_.toDouble)

  implicit def optionFieldEncoder[A](implicit
      fieldEncoder: FirestoreFieldEncoder[A]
  ): FirestoreFieldEncoder[Option[A]] =
    instance[Option[A]](_.map(fieldEncoder.encodeField).orNull)

  implicit def seqFieldEncoder[A](implicit fieldEncoder: FirestoreFieldEncoder[A]): FirestoreFieldEncoder[Seq[A]] =
    instance[Seq[A]](_.map(fieldEncoder.encodeField).asJava.asInstanceOf[AnyRef])

  implicit def listFieldEncoder[A](implicit fieldEncoder: FirestoreFieldEncoder[A]): FirestoreFieldEncoder[List[A]] =
    seqFieldEncoder[A].contramap[List[A]](_.toSeq)

}
