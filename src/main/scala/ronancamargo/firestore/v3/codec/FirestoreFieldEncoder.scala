package ronancamargo.firestore.v3.codec

import ronancamargo.firestore.v3.codec.instances.FirestoreFieldEncoderInstances
import shapeless._

trait FirestoreFieldEncoder[A] { self =>
  def encodeField(field: A): AnyRef

  def contramap[B](f: B => A): FirestoreFieldEncoder[B] = new FirestoreFieldEncoder[B] {
    override def encodeField(field: B): AnyRef = self.encodeField(f(field))
  }
}

object FirestoreFieldEncoder extends FirestoreFieldEncoderInstances {
  def instance[A](f: A => AnyRef): FirestoreFieldEncoder[A] = new FirestoreFieldEncoder[A] {
    override def encodeField(field: A): AnyRef = f(field)
  }
  implicit object polyEncode extends Poly1 {}

//  implicit def mkFirestoreFieldEncoder[A, LA <: HList, H, LT <: HList](implicit
//      gen: LabelledGeneric.Aux[A, LA],
//      isHcons: IsHCons.Aux[LA, H, LT]
//  ) = new FirestoreFieldEncoder[A] {
//    override def encodeField(field: A): AnyRef = {
//      val a = gen.to(field).head
//      val head = encodeOne(a)
//      gen.to(field).map()
//
//    }
//  }

  def encodeOne[H, K <: Symbol](h: H)(implicit witness: Witness.Aux[K]): (String, H) =
    (witness.value.name, h)

//  import magnolia1._
//
//  import language.experimental.macros
//
//  type Typeclass[A] = FirestoreFieldEncoder[A]
//
//  import scala.jdk.CollectionConverters._
//  def join[T](ctx: CaseClass[FirestoreFieldEncoder, T]): FirestoreFieldEncoder[T] = new FirestoreFieldEncoder[T] {
//    override def encodeField(field: T): AnyRef =
//      ctx.parameters
//        .map { p: Param[Typeclass, T] => p.label -> p.typeclass.encodeField(p.dereference(field)) }
//        .toMap
//        .asJava
//        .asInstanceOf[AnyRef]
//  }
//
//  def split[T](ctx: SealedTrait[FirestoreFieldEncoder, T]): FirestoreFieldEncoder[T] = new FirestoreFieldEncoder[T] {
//    override def encodeField(field: T): AnyRef = ctx.split(field) { sub => sub.typeclass.encodeField(sub.cast(field)) }
//  }
//
//  implicit def gen[A]: FirestoreFieldEncoder[A] = macro Magnolia.gen[A]
}
