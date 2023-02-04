package ronancamargo.firestore.v3.codec

import ronancamargo.firestore.v3.codec.semiauto._
import ronancamargo.firestore.v3.data.FirestoreDocument
import shapeless.Generic.Aux
import shapeless.ops.hlist
import shapeless.{Generic, HList}

import scala.annotation.implicitNotFound

trait FirestoreEncoder[A] { self =>
  def apply(entity: A): FirestoreDocument

  def contramap[B](f: B => A): FirestoreEncoder[B] = new FirestoreEncoder[B] {
    override def apply(b: B): FirestoreDocument = self(f(b))
  }
}

object FirestoreEncoder {

  def instance[A](f: A => Map[String, AnyRef]): FirestoreEncoder[A] = new FirestoreEncoder[A] {
    override def apply(entity: A): FirestoreDocument = FirestoreDocument(f(entity))
  }

  def semiauto[A <: Product, I <: HList, O <: HList](generic: Generic.Aux[A, I])(implicit
      @implicitNotFound("Check whether all types of the Product are valid for Firestore")
      mapper: hlist.Mapper.Aux[polyEncoder.type, I, O],
      traverse: hlist.ToTraversable.Aux[O, List, AnyRef]
  ): FirestoreEncoder[A] =
    new FirestoreEncoder[A] {
      private implicit val gen: Aux[A, I]              = generic
      override def apply(entity: A): FirestoreDocument = FirestoreDocument(entity.encodeWith(polyEncoder))
    }
}
