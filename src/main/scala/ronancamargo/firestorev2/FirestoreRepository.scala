package ronancamargo.firestorev2

import com.google.cloud.firestore.Firestore
import ronancamargo.firestorev2.Depth.{Depth, Depth0, DepthCoproduct}
import shapeless.ops.coproduct
import shapeless.{:+:, =:!=, CNil, Nat}

abstract class FirestoreRepository[A <: Product, D <: Depth](firestore: Firestore)(implicit
    coproductInject: coproduct.Inject[DepthCoproduct, Depth],
    nonEmpty: D =:!= Depth0
)

object Depth {
  type Depth = Nat

  type Depth0 = Nat._0
  type Depth1 = Nat._1
  type Depth2 = Nat._2
  type Depth3 = Nat._3

  type DepthCoproduct = Depth0 :+: Depth1 :+: Depth2 :+: Depth3 :+: CNil
}

object Keys {
  type Key1 = String
  type Key2 = (Key1, Key1)
  type Key3 = (Key2, Key1)
  type Key4 = (Key3, Key1)
  type Key5 = (Key4, Key1)

  type KeyCoproduct = Key1 :+: Key2 :+: Key3 :+: Key4 :+: Key5 :+: CNil
}
