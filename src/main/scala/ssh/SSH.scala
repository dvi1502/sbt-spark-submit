package ru.dvi.sbt.sparksubmit

import com.jcraft.jsch.{ChannelExec, Session}
import sbt.Logger

import java.io.{BufferedReader, InputStream, InputStreamReader, SequenceInputStream}
import java.util.concurrent.TimeUnit
import scala.io.Source
import scala.util.matching.Regex
import scala.util.{Failure, Success}


object SSH {

  private def extarctAppId(msg: String) = {
    val regexpPattern = new Regex("""INFO Client: Application report for (application_[0-9]+_[0-9]+) \(state: FINISHED\)""", "appId")
    regexpPattern.findFirstMatchIn(msg) match {
      case Some(value) if (value.group("appId").nonEmpty) => Some(value.group("appId"))
      case _ => None
    }
  }


  def exec(connInfo: SSHSettings, COMMAND: String, log: Logger): Option[String] = {

    var appid: Option[String] = None

    SSHSessionFactory.getSSHSession(connInfo: SSHSettings) match {
      case Success(session) =>

        val channelExec: ChannelExec = session.openChannel("exec").asInstanceOf[ChannelExec]
        try {

          log.info(s"""Execute command "${COMMAND}" to ${connInfo.SFTPHOST}:${connInfo.SFTPDSTDIR}""")

          channelExec.setCommand(COMMAND)
          val out: BufferedReader = new BufferedReader(new InputStreamReader(new SequenceInputStream(channelExec.getErrStream(), channelExec.getInputStream())))

          channelExec.setPty(false)
          channelExec.connect()

          while ( {
            val msg: String = out.readLine()
            if (msg != null) {
              log.info(msg)
              if (appid == None) appid = extarctAppId(msg)
            }
            msg != null
          }) {
          }

        }
        catch {
          case ex: Exception =>
            log.error(ex.toString())

        }
        finally {
          channelExec.disconnect()
          session.disconnect()
        }

      case Failure(exception) => throw exception

    }

    appid

  }


  private def useChannelExec(session: Session, command: String, log: Logger): Unit = {

    import java.io._

    val channelExec = session.openChannel("exec").asInstanceOf[ChannelExec]

    val stderr = new ByteArrayOutputStream()
    val stdout = new ByteArrayOutputStream()

    channelExec.setInputStream(null)
    channelExec.setErrStream(stderr, false);
    channelExec.setOutputStream(stdout, false);
    channelExec.connect(TimeUnit.SECONDS.toMillis(10L).asInstanceOf[Int])
    channelExec.setPty(false);

    channelExec.connect()
    channelExec.setCommand(command)

    Source.fromInputStream(new ByteArrayInputStream(stdout.toByteArray())).getLines.foreach { line => log.info(s"$line") }
    Source.fromInputStream(new ByteArrayInputStream(stderr.toByteArray())).getLines.foreach { line => log.error(s"$line") }

    channelExec.disconnect()

  }


}