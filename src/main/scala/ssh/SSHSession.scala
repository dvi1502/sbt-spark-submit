package ru.dvi.sbt.sparksubmit


import scala.util.Try

sealed abstract class Auth()
class PasswordAuth (val SFTPPASSWORD: String) extends Auth {
  override def toString: String = SFTPPASSWORD
}
class KeyAuth (val SFTPKEY: String) extends Auth {
  override def toString: String = SFTPKEY
}


case class SSHSettings(
                           SFTPHOST: String,
                           SFTPPORT: Int,
                           SFTPUSER: String,
                           SFTPAUTH: Auth,
                           SFTPDSTDIR: String,
                           LOCALDSTDIR: String,

                         ) {

  def isPasswordAuth() = SFTPAUTH.isInstanceOf[PasswordAuth]

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

object SSHSessionFactory {

  def getSSHSession(connInfo: SSHSettings) = {
    connInfo.isPasswordAuth() match {
      case true => getSessionWithPasswordAuth(connInfo)
      case _ => getSessionWithKeyAuth(connInfo)
    }
  }


  private def getSessionWithKeyAuth(connInfo: SSHSettings) = {
    import com.jcraft.jsch.JSch
    Try({
      val jsch = new JSch()
      jsch.addIdentity(connInfo.SFTPAUTH.toString, "passphrase")
      val session = jsch.getSession(connInfo.SFTPUSER, connInfo.SFTPHOST, connInfo.SFTPPORT)

      val props: java.util.Properties = new java.util.Properties()
      props.put("StrictHostKeyChecking", "no")

      session.setConfig(props)
      session.setTimeout(30 * 1000)
      session.connect()
      session
    })

  }

  private def getSessionWithPasswordAuth(connInfo: SSHSettings) = {
    import com.jcraft.jsch.JSch
    Try({
      val jsch = new JSch()

      val session = jsch.getSession(connInfo.SFTPUSER, connInfo.SFTPHOST, connInfo.SFTPPORT)

      val props: java.util.Properties = new java.util.Properties();
      props.put("StrictHostKeyChecking", "no")
      props.put("PreferredAuthentications", "password")

      session.setConfig(props)
      session.setTimeout(30 * 1000)
      session.connect()
      session
    })

  }


}