package ronancamargo

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ronancamargo.firestore.codec.FirestoreFieldDecoder

import scala.jdk.CollectionConverters._

class AutoDecoderTest extends AnyFlatSpec with Matchers {

  case class Person(name: String, age: Int, children: List[Person])

  "a" must "" in {

    val children = List(
      Map(
        "name"     -> "child".asInstanceOf[AnyRef],
        "children" -> List.empty[Person].asJava,
        "age"      -> 5L.asInstanceOf[AnyRef]
      ).asJava.asInstanceOf[AnyRef]
    ).asJava.asInstanceOf[AnyRef]

    val input =
      Map("name" -> "Ronan".asInstanceOf[AnyRef], "age" -> 29L.asInstanceOf[AnyRef], "children" -> children).asJava
        .asInstanceOf[AnyRef]

    implicitly[FirestoreFieldDecoder[Person]]
      .decodeField(input) mustBe Person("Ronan", 29, List(Person("child", 5, Nil)))
  }

  sealed trait Shape
  object Shape {
    case class Circle(radius: Double) extends Shape
    case class Square(side: Double)   extends Shape
  }

//  it must "asd" in {
//    val circle = Map("radius" -> 2.22.asInstanceOf[AnyRef]).asJava.asInstanceOf[AnyRef]
//    implicitly[FirestoreFieldDecoder[Shape]].decodeField(circle) mustBe Shape.Circle(2.22)
//  }
}
