package ru.dvi.sbt.sparksubmit


case class SSHSettings(
                        SFTPHOST: String,
                        SFTPPORT: Int,
                        SFTPUSER: String,
                        SFTPKEY: String,
                        SFTPDSTDIR: String,
                      )
{
  def pretty(): String =
    classOf[SSHSettings]
      .getDeclaredFields
      .map { f =>
        f.setAccessible(true)
        val res = (f.getName, f.get(this))
        f.setAccessible(false)
        s"${res._1.padTo(25, ' ')}\t:\t${res._2}"
      }.mkString("\n") + "\n---\n"
}