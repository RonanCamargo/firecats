package ronancamargo.firestore.data.safe

import ronancamargo.firestore.data.safe.DocumentDepthCoproduct.DocumentDepthCoproduct
import shapeless.ops.coproduct
import shapeless.ops.hlist.Length
import shapeless.{=:!=, HList, Nat}

case class DocumentKey[D <: HList, N <: Nat](keys: D)(implicit
    cop: coproduct.Inject[DocumentDepthCoproduct, D],
    length: Length.Aux[D, N],
    nonEmpty: N =:!= Nat._0
)
