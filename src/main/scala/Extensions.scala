package ru.dvi.sbt.sparksubmit

object Extensions {

  class RichSeq(seq: Seq[(String, Option[Any])]) {
    def toArgs: Seq[String] = seq.collect { case (arg, Some(value)) => Seq(arg, value.toString) }.flatten
  }

  implicit def toRichSeq(seq: Seq[(String, Option[Any])]): RichSeq = new RichSeq(seq)
}
