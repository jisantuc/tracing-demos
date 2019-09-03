package com.jisantuc.tracingdemos.datamodel

object Interpreter {

  def apply(op: Op): Double = op match {
    case Lit(x)               => x
    case Add3(op)             => apply(op) + 3
    case Subtract1(op)        => apply(op) - 1
    case DivideBy4(op)        => apply(op) / 4
    case MultiplyBy5(op)      => apply(op) * 5
    case Divide1MillionBy(op) => 1e6 / apply(op)
  }
}
