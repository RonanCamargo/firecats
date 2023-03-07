package ronancamargo.firestore.shapeless

import ronancamargo.firestore.data.safe.DocumentDepthCoproduct.DocumentDepthCoproduct
import shapeless.HList
import shapeless.ops._
import shapeless.ops.hlist.ToTraversable

import scala.annotation.implicitNotFound

trait TraversableCoproductInject[A, D] {
//  implicit val coproductInject: coproduct.Inject[DocumentDepthCoproduct, D]
//  implicit val toTraversable: ToTraversable.Aux[D, List, String]
}

object TraversableCoproductInject {
  implicit def mkTraversableCoproductInject[A, D <: HList](implicit
      @implicitNotFound("Check ${D} parameter, it must be a valid DocumentDepth type")
      coproductInject: coproduct.Inject[DocumentDepthCoproduct, D],
      toTraversable: ToTraversable.Aux[D, List, String]
  ): TraversableCoproductInject[A, D] = new TraversableCoproductInject[A, D] {}
}
