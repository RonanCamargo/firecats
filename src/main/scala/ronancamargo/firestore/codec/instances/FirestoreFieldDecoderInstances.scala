package ronancamargo.firestore.codec.instances

import cats.Functor
import ronancamargo.firestore.JavaList
import ronancamargo.firestore.codec.FirestoreFieldDecoder
import ronancamargo.firestore.codec.FirestoreFieldDecoder.instance

import scala.jdk.CollectionConverters._

private[codec] trait FirestoreFieldDecoderInstances {
  private def simpleDecoder[A]: FirestoreFieldDecoder[A] = instance[A](_.asInstanceOf[A])

  implicit val stringDecoder: FirestoreFieldDecoder[String]         = simpleDecoder[String]
  implicit val longDecoder: FirestoreFieldDecoder[Long]             = simpleDecoder[Long]
  implicit val intDecoder: FirestoreFieldDecoder[Int]               = longDecoder.map(_.toInt)
  implicit val doubleDecoder: FirestoreFieldDecoder[Double]         = instance[Double] { field =>
    if (field.isInstanceOf[Long]) longDecoder.decodeField(field).toDouble
    else simpleDecoder[Double].decodeField(field)
  }
  implicit val bigDecimalDecoder: FirestoreFieldDecoder[BigDecimal] = doubleDecoder.map(BigDecimal(_))
  implicit val booleanDecoder: FirestoreFieldDecoder[Boolean]       = simpleDecoder[Boolean]

  implicit def optionFieldDecoder[A](implicit
      fieldDecoder: FirestoreFieldDecoder[A]
  ): FirestoreFieldDecoder[Option[A]] =
    instance[Option[A]](Option(_).map(fieldDecoder.decodeField))

  implicit def seqFieldDecoder[A](implicit fieldDecoder: FirestoreFieldDecoder[A]): FirestoreFieldDecoder[Seq[A]] =
    instance[Seq[A]] { listOfFields =>
      Option(listOfFields)
        .map(_.asInstanceOf[JavaList[AnyRef]])
        .fold(Seq.empty[A])(_.asScala.map(fieldDecoder.decodeField).toSeq)
    }

  implicit def listFieldDecoder[A](implicit fieldDecoder: FirestoreFieldDecoder[A]): FirestoreFieldDecoder[List[A]] =
    seqFieldDecoder[A].map(_.toList)

  implicit val decoderFunctor: Functor[FirestoreFieldDecoder] = new Functor[FirestoreFieldDecoder] {
    override def map[A, B](fa: FirestoreFieldDecoder[A])(f: A => B): FirestoreFieldDecoder[B] = fa.map(f)
  }

}
