package ronancamargo.firestore.shapeless

import shapeless._
import shapeless.ops.hlist.Intersection

import scala.annotation.implicitNotFound

trait Projection[A, B]

object Projection {
  implicit def mkProjection[A, B, LA <: HList, LB <: HList](implicit
      genA: Lazy[LabelledGeneric.Aux[A, LA]],
      genB: Lazy[LabelledGeneric.Aux[B, LB]],
      @implicitNotFound("${B} must be a type projection of ${A}")
      intersection: Lazy[Intersection.Aux[LA, LB, LB]]
  ): Projection[A, B] = new Projection[A, B] {}
}
