package ronancamargo.firestore.v3.repositories.safe

import cats.effect.IO
import com.google.cloud.firestore.Firestore
import ronancamargo.firestore.v3.data.safe.DocumentDepthCoproduct.{DocumentDepth, DocumentDepthCoproduct}
import shapeless.ops.coproduct
import shapeless.ops.hlist.ToTraversable

import scala.annotation.implicitNotFound

abstract class FirestoreIORepository[A <: Product, D <: DocumentDepth](database: Firestore)(implicit
    @implicitNotFound("Check ${D} parameter, it must be a valid DocumentDepth type")
    coproductInject: coproduct.Inject[DocumentDepthCoproduct, D],
    toTraversable: ToTraversable.Aux[D, List, String]
) extends FirestoreRepository[IO, A, D](database)
