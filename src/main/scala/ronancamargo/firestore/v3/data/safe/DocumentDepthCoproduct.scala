package ronancamargo.firestore.v3.data.safe

import shapeless.{:+:, ::, CNil, HList, HNil}

object DocumentDepthCoproduct {
  type DocumentDepth = HList

  type DocumentDepth1 = String :: HNil
  type DocumentDepth2 = String :: DocumentDepth1
  type DocumentDepth3 = String :: DocumentDepth2
  type DocumentDepth4 = String :: DocumentDepth3
  type DocumentDepth5 = String :: DocumentDepth4

  type DocumentDepthCoproduct =
    DocumentDepth1 :+: DocumentDepth2 :+: DocumentDepth3 :+: DocumentDepth4 :+: DocumentDepth5 :+: CNil
}
