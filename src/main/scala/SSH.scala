package ru.dvi.sbt.sparksubmit

import com.jcraft.jsch.*
import sbt.Logger
import java.io.{BufferedReader, File, FileInputStream, InputStreamReader}
import java.util.Properties


object SSH {

  def command(
               settings: SSHSettings,
               COMMAND: String, log: Logger) = {

    var channel: Channel = null
    var session: Session = null

    try {
      val jsch: JSch = new JSch()
      jsch.addIdentity(settings.SFTPKEY, "passphrase")

//      log.debug(s"SFTPKEY=${settings.SFTPKEY}")
//      log.debug(s"SFTPHOST=${settings.SFTPHOST}:${settings.SFTPPORT}")
//      log.debug(s"SFTPUSER=${settings.SFTPUSER}")

      log.debug("*****************************")
      log.debug(s"SSH.command")
      log.debug(s"COMMAND=$COMMAND")
      log.debug("*****************************")

      val config: Properties = new Properties()
      config.put("StrictHostKeyChecking", "no")

      session = jsch.getSession(settings.SFTPUSER, settings.SFTPHOST, settings.SFTPPORT)
      session.setConfig(config)
      session.connect()

      channel = session.openChannel("exec")

      log.info(s"Submitting command: $$> $COMMAND")
      val channelExec: ChannelExec = channel.asInstanceOf[ChannelExec]
      channelExec.setCommand(COMMAND);

      val in: BufferedReader = new BufferedReader(new InputStreamReader(channel.getInputStream()));

      channel.connect();

      var msg: String = null;
      while ( {
        msg = in.readLine();
        msg != null
      }) {
        log.info(msg);
      }

    } catch {
      case ex: Exception => log.error(ex.toString())
    } finally {

      channel.disconnect();
      session.disconnect();

      log.info("   ... submitted successful!")
    }
  }


  def upload(
              settings: SSHSettings,
              FILETOTRANSFER: String,
              log: Logger) = {
    var fis: FileInputStream = null
    var channel: Channel = null
    var session: Session = null

    try {

      val jsch: JSch = new JSch()

      jsch.addIdentity(settings.SFTPKEY, "passphrase")

//      log.debug(s"SFTPKEY=${settings.SFTPKEY}")
//      log.debug(s"SFTPHOST=${settings.SFTPHOST}:${settings.SFTPPORT}")
//      log.debug(s"SFTPUSER=${settings.SFTPUSER}")
      log.debug("*****************************")
      log.debug(s"SSH.upload")
      log.debug(s"FILETOTRANSFER=$FILETOTRANSFER")
      log.debug(s"SFTPDSTDIR=${settings.SFTPDSTDIR}")
      log.debug("*****************************")

      val config: Properties = new Properties()
      config.put("StrictHostKeyChecking", "no")

      session = jsch.getSession(settings.SFTPUSER, settings.SFTPHOST, settings.SFTPPORT)
      session.setConfig(config)
      session.connect()

      channel = session.openChannel("sftp")
      channel.connect()

      val channelSftp: ChannelSftp = channel.asInstanceOf[ChannelSftp]

      channelSftp.cd(settings.SFTPDSTDIR)

      log.info(s"Transfering file $FILETOTRANSFER to ${settings.SFTPHOST}:${settings.SFTPDSTDIR}")
      val f: File = new File(FILETOTRANSFER)
      fis = new FileInputStream(f)

      channelSftp.put(fis, f.getName())

    } catch {
      case ex: Exception => log.error(ex.toString())
    } finally {
      if (fis != null) {
        fis.close()
      }

      channel.disconnect();
      session.disconnect();

      log.info("   ... transfer successful!")
    }
  }

}

