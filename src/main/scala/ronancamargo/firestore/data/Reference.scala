package ronancamargo.firestore.data
import shapeless.{::, HList, HNil}

object Reference {
  implicit class HListTuple[A](private val a: A) extends AnyVal {
    def @>[L <: HList](l: L): A :: L = a :: l
  }

  val Collection: HNil.type    = HNil
  val DocKey: HNil.type        = HNil
  val ManyDocs: String :: HNil = "dummy" :: HNil
}