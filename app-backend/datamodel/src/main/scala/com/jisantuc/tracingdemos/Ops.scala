package com.jisantuc.tracingdemos.datamodel

import io.circe._
import io.circe.syntax._

import scala.util.Random

sealed trait Op

case class Lit(x: Double)           extends Op
case class Add3(op: Op)             extends Op
case class Subtract1(op: Op)        extends Op
case class DivideBy4(op: Op)        extends Op
case class MultiplyBy5(op: Op)      extends Op
case class Divide1MillionBy(op: Op) extends Op

object Op {

  val opConstructors = List[Op => Op](
    Add3.apply _,
    Subtract1.apply _,
    DivideBy4.apply _,
    MultiplyBy5.apply _,
    Divide1MillionBy.apply _
  )

  def shuffledOpsConstructors = Random.shuffle(opConstructors)

  implicit val encodeOp: Encoder[Op] = new Encoder[Op] {

    def apply(op: Op) =
      (op match {
        case Lit(x)                   => Map("lit"         -> x.asJson)
        case Add3(op)                 => Map("add3"        -> op.asJson)
        case Subtract1(op)            => Map("subtract1"   -> op.asJson)
        case DivideBy4(op)            => Map("divideBy4"   -> op.asJson)
        case MultiplyBy5(op)          => Map("multiplyBy5" -> op.asJson)
        case Divide1MillionBy(op: Op) => Map("divide1e6By" -> op.asJson)
      }).asJson
  }
}
