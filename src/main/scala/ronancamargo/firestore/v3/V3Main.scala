package ronancamargo.firestore.v3

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Query.Direction
import ronancamargo.MainUtil
import ronancamargo.firestore.data.Reference._
import ronancamargo.firestore.v3.codec.{FirestoreDecoder, FirestoreEncoder}
import ronancamargo.firestore.v3.data.safe.DocumentDepthCoproduct.DocumentDepth1
import ronancamargo.firestore.v3.repositories.safe.FirestoreIORepository
import ronancamargo.firestore.v3.syntax.codec._

import scala.jdk.CollectionConverters._

object V3Main extends App with MainUtil {

  val repo = PersonRepository(firestore)

//  (1 to 1000).map(n => repo.set(Person(n.toString, 100))).foreach(_.runTimedAndPrint)

  val set              = repo.set(Person("Messi", 35))
  val set2             = repo.set(Person("El Diego", 60))
  val get              = repo.get("Messi" @> DocKey)
  val update           = repo.update("Messi" @> DocKey)(_.copy(age = 36))
  val updateProjection = repo.updateProjection("Messi" @> DocKey)(p => Age(p.age + 1))
  val all              = repo.getAll(ManyDocs)
  val greaterThan30    = repo.getByQuery(ManyDocs)(_.whereGreaterThan("age", 30))
  val nameIn           = repo.getByQuery(ManyDocs)(_.whereIn("name", List("Messi").asJava))
  val orderByAge       = repo.getByQuery(ManyDocs)(
    _.whereNotEqualTo("age", 0)
      .orderBy("age", Direction.ASCENDING)
      .offset(10)
      .limit(2)
  )
  val getStream        = repo.getAllAsStream(ManyDocs)

//  set.runTimedAndPrint
//  set2.runTimedAndPrint
  get.runTimedAndPrint
//  update.runTimedAndPrint
//  updateProjection.runTimedAndPrint
//  getStream.compile.toList.attempt.runTimedAndPrint
  all.runTimedAndPrint
//  greaterThan30.runTimedAndPrint
//  nameIn.runTimedAndPrint
//  orderByAge.runTimedAndPrint
}

case class Person(name: String, age: Int)

object Person {
  implicit val personEncoder: FirestoreEncoder[Person] =
    FirestoreEncoder.instance[Person](p => Map("name" -> p.name.encodeString, "age" -> p.age.encodeInt))

  implicit val personDecoder: FirestoreDecoder[Person] =
    FirestoreDecoder.instance[Person](map => Person(map("name").decodeField[String], map("age").decodeField[Int]))
}

case class PersonRepository(firestore: Firestore) extends FirestoreIORepository[Person, DocumentDepth1](firestore) {
  override protected val collectionHierarchy: DocumentDepth1 = "people" @> Collection

  override protected def keyFromDoc(doc: Person): DocumentDepth1 = doc.name @> DocKey

}

case class Age(age: Int)
object Age {
  implicit val ageEncoder: FirestoreEncoder[Age] =
    FirestoreEncoder.instance[Age](age => Map("age" -> age.age.encodeInt))
}
