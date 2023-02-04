package ronancamargo.firestore.v3.data.safe

import ronancamargo.firestore.v3.data.safe.DocumentDepthCoproduct.DocumentDepthCoproduct
import shapeless.{=:!=, HList, Nat}
import shapeless.ops.coproduct
import shapeless.ops.hlist.Length

case class CollectionHierarchy[D <: HList, N <: Nat](collections: D)(implicit
    cop: coproduct.Inject[DocumentDepthCoproduct, D],
    length: Length.Aux[D, N],
    nonEmpty: N =:!= Nat._0
)
