package ronancamargo.firestore.v3.codec

import magnolia1.{CaseClass, Magnolia, SealedTrait}
import ronancamargo.firestore.JavaMap
import ronancamargo.firestore.v3.codec.instances.FirestoreFieldDecoderInstances

import scala.jdk.CollectionConverters._
import scala.language.experimental.macros

trait FirestoreFieldDecoder[A] { self =>
  def decodeField(field: AnyRef): A

  def map[B](f: A => B): FirestoreFieldDecoder[B] =
    FirestoreFieldDecoder.instance[B](field => f(self.decodeField(field)))
}

object FirestoreFieldDecoder extends FirestoreFieldDecoderInstances {
  def instance[A](decode: AnyRef => A): FirestoreFieldDecoder[A] = new FirestoreFieldDecoder[A] {
    override def decodeField(field: AnyRef): A = decode(field)
  }

  type Typeclass[A] = FirestoreFieldDecoder[A]

  def join[A](ctx: CaseClass[FirestoreFieldDecoder, A]): FirestoreFieldDecoder[A] = new FirestoreFieldDecoder[A] {
    override def decodeField(field: AnyRef): A = field match {
      case field if field.isInstanceOf[JavaMap[_, _]] =>
        val keyValue = field.asInstanceOf[JavaMap[String, AnyRef]].asScala

        val classFields = ctx.parameters.map { p =>
          val value = keyValue(p.label)
          p.typeclass.decodeField(value)
        }

        ctx.rawConstruct(classFields)
    }
  }

  def split[A](ctx: SealedTrait[FirestoreFieldDecoder, A]): FirestoreFieldDecoder[A] =
    throw new RuntimeException("Not supported yet")

  implicit def gen[T]: FirestoreFieldDecoder[T] = macro Magnolia.gen[T]

}
