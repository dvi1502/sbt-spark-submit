package ru.dvi.sbt.sparksubmit

import java.time.{Instant, ZoneId}


case class SftpFile(
                id: Int,
                fileName: String,
                createDate: Long,
                modifyDate: Long,
                size: Long,
              ) extends Serializable {


  override def toString() = {
    val sb = new StringBuffer()
    sb.append(s"[${this.id}]")
    sb.append(s"\t${Instant.ofEpochMilli(this.createDate).atZone(ZoneId.systemDefault()).toLocalDateTime()}")
    sb.append(s"\t${Instant.ofEpochMilli(this.modifyDate).atZone(ZoneId.systemDefault()).toLocalDateTime()}")
    sb.append(s"\t${this.size}")
    sb.append(s"\t${this.fileName}")
    sb.toString
  }

  override def hashCode(): Int = {
    fileName.trim.hashCode
  }

  override def equals(obj: Any): Boolean = {
    obj.hashCode() == this.hashCode()
  }

  def pretty(): String =
    classOf[SftpFile]
      .getDeclaredFields
      .map { f =>
        f.setAccessible(true)
        val res = (f.getName, f.get(this))
        f.setAccessible(false)
        s"${res._1.padTo(25, ' ')}\t:\t${res._2}"
      }.mkString("\n") + "\n---\n"

}

