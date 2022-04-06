package ronancamargo.firestore.tryout

import ronancamargo.firestore.codec.{FirestoreDecoder, FirestoreEncoder}

case class Daruma(name: String, cuit: String)

object Daruma {
  implicit val darumaEncoder: FirestoreEncoder[Daruma] = FirestoreEncoder.instance[Daruma] { entity =>
    Map("name" -> entity.name, "cuit" -> entity.cuit)
  }

  implicit val darumaDecoder: FirestoreDecoder[Daruma] = FirestoreDecoder.instance { map =>
    Daruma(map("name").toString, map("cuit").toString)
  }
}
