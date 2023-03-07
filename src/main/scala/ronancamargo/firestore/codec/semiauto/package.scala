package ronancamargo.firestore.codec

import com.google.cloud.firestore.{DocumentReference, GeoPoint}
import ronancamargo.firestore.JavaMap
import shapeless.ops.hlist
import shapeless.{<:!<, Generic, HList, Poly, Poly1}

import scala.jdk.CollectionConverters._

package object semiauto {
  implicit private[codec] object polyEncoder extends Poly1 {
    implicit val intCase: polyEncoder.Case.Aux[Int, AnyRef]               = at[Int](_.toLong.asInstanceOf[AnyRef])
    implicit val booleanCase: polyEncoder.Case.Aux[Boolean, AnyRef]       = at[Boolean](_.asInstanceOf[AnyRef])
    implicit val stringCase: polyEncoder.Case.Aux[String, AnyRef]         = at[String](_.asInstanceOf[AnyRef])
    implicit val bigDecimalCase: polyEncoder.Case.Aux[BigDecimal, AnyRef] =
      at[BigDecimal](_.toDouble.asInstanceOf[AnyRef])
    implicit val doubleCase: polyEncoder.Case.Aux[Double, AnyRef]         = at[Double](_.asInstanceOf[AnyRef])
    implicit val longCase: polyEncoder.Case.Aux[Long, AnyRef]             = at[Long](_.asInstanceOf[AnyRef])

    implicit val geoCase: polyEncoder.Case.Aux[GeoPoint, AnyRef]             = at[GeoPoint](_.asInstanceOf[AnyRef])
    implicit val docRefCase: polyEncoder.Case.Aux[DocumentReference, AnyRef] =
      at[DocumentReference](_.asInstanceOf[AnyRef])

    implicit def optionCase[A](implicit
        caseAux: polyEncoder.Case.Aux[A, AnyRef]
    ): polyEncoder.Case.Aux[Option[A], AnyRef] =
      at[Option[A]](_.map(caseAux).orNull)

    implicit def listCase[A](implicit
        caseAux: polyEncoder.Case.Aux[A, AnyRef],
        ev: A <:!< Product
    ): polyEncoder.Case.Aux[List[A], AnyRef] =
      at[List[A]](_.map(caseAux).asJava.asInstanceOf[AnyRef])

    implicit def productCase[P <: Product, I <: HList, O <: HList](implicit
        generic: Generic.Aux[P, I],
        mapper: hlist.Mapper.Aux[polyEncoder.type, I, O],
        traverse: hlist.ToTraversable.Aux[O, List, AnyRef]
    ): polyEncoder.Case.Aux[P, AnyRef] =
      at[P](product => FirestoreEncoder.semiauto(generic).apply(product).document.asInstanceOf[AnyRef])
  }

  implicit final private[codec] class ProductToMap[A <: Product](private val product: A) extends AnyVal {
    def encodeWith[I <: HList, O <: HList](f: Poly)(implicit
        generic: Generic.Aux[A, I],
        mapper: hlist.Mapper.Aux[f.type, I, O],
        traversable: hlist.ToTraversable.Aux[O, List, AnyRef]
    ): JavaMap[String, AnyRef] = {
      val hlist  = generic.to(product).map(f)
      val zipped = product.productElementNames zip hlist.toList
      zipped.toMap.asJava
    }
  }

}
