package ru.dvi.sbt.sparksubmit

import com.jcraft.jsch.{ChannelSftp, SftpATTRS}
import sbt.Logger

import java.io.{BufferedInputStream, File, FileInputStream, InputStream}
import java.nio.file.{Files, Path, Paths}
import scala.util.{Failure, Success, Try}


object SFTP {


  def upload(connInfo: SSHSettings, filename: String, log: Logger): Unit = {

    SSHSessionFactory.getSSHSession(connInfo: SSHSettings) match {
      case Success(session) =>
        val channelSftp = session.openChannel("sftp").asInstanceOf[ChannelSftp]
        try {
          channelSftp.connect()
          channelSftp.cd(connInfo.SFTPDSTDIR)
          log.info(s"Transfering file ${filename} to ${connInfo.SFTPHOST}:${connInfo.SFTPDSTDIR}")
          val f: File = new File(filename)
          channelSftp.put(new FileInputStream(f), f.getName())
        } catch {
          case ex: Exception => log.error(ex.toString())
        } finally {
          channelSftp.exit()
          session.disconnect()
          log.info("   ... submitted successful!")
        }
      case Failure(exception) => log.error(s"${exception.getMessage}")
    }

  }


  def download(connInfo: SSHSettings, filename: String, log: Logger) = {

    SSHSessionFactory.getSSHSession(connInfo: SSHSettings) match {
      case Success(session) =>
        val channelSftp = session.openChannel("sftp").asInstanceOf[ChannelSftp]
        try {
          channelSftp.connect()

          log.info(s"Transfering file $filename to ${connInfo.SFTPHOST}:${connInfo.SFTPDSTDIR}")
          channelSftp.cd(connInfo.SFTPDSTDIR)
          channelSftp.get(filename, connInfo.LOCALDSTDIR)
          log.info("   ... submitted successful!")

        } catch {
          case ex: Exception =>
            log.error(ex.toString())

        } finally {
          channelSftp.exit()
          session.disconnect()
        }

      case Failure(exception) =>
        log.error(s"${exception.getMessage}")


    }

  }


  def delete(connInfo: SSHSettings, filename: String, log: Logger): Unit = {

    SSHSessionFactory.getSSHSession(connInfo: SSHSettings) match {
      case Success(session) =>
        val channelSftp = session.openChannel("sftp").asInstanceOf[ChannelSftp]
        try {
          channelSftp.connect()
          channelSftp.cd(connInfo.SFTPDSTDIR)

          log.info(s"Deleting file $filename")
          channelSftp.rm(filename)
          log.info("   ... submitted successful!")

        } catch {
          case ex: Exception => log.error(ex.toString())
        } finally {
          channelSftp.exit()
          session.disconnect()
        }
      case Failure(exception) => log.error(s"${exception.getMessage}")
    }

  }


  def stat(connInfo: SSHSettings, filename: String, log: Logger): Option[SftpATTRS] = {

    SSHSessionFactory.getSSHSession(connInfo: SSHSettings) match {
      case Success(session) =>
        val channelSftp = session.openChannel("sftp").asInstanceOf[ChannelSftp]
        try {
          channelSftp.connect()
          channelSftp.cd(connInfo.SFTPDSTDIR)
          //          channelSftp.lcd(connInfo.LOCALDSTDIR)
          log.info(s"Gei stat of file $filename")
          val stat: SftpATTRS = channelSftp.stat(filename)
          //          val lstat: SftpATTRS = channelSftp.lstat(filename)
          log.info("   ... submitted successful!")
          Some(stat)

        } catch {
          case ex: Exception =>
            log.error(ex.toString())
            None
        } finally {
          channelSftp.exit()
          session.disconnect()
        }
      case Failure(exception) =>
        log.error(s"${exception.getMessage}")
        None
    }

  }


  def ls(connInfo: SSHSettings, log: Logger): List[SftpFile] = {

    def listProcess(ls: java.util.Vector[_]): List[SftpFile] = {

      import scala.collection.JavaConverters.asScalaBufferConverter

      val x = ls.asScala.map(_.asInstanceOf[ChannelSftp#LsEntry]).toList

      x.filterNot(x => Set(".", "..").contains(x.getFilename) || x.getAttrs.isDir)
        .zipWithIndex
        .map { case (file, id) =>
          SftpFile(
            id,
            file.getFilename,
            file.getAttrs.getATime.toLong * 1000L,
            file.getAttrs.getMTime.toLong * 1000L,
            file.getAttrs.getSize,
          )
        }
    }


    SSHSessionFactory.getSSHSession(connInfo: SSHSettings) match {
      case Success(session) =>
        val channelSftp = session.openChannel("sftp").asInstanceOf[ChannelSftp]
        try {
          channelSftp.connect()

          log.info(s"Get list of files to $connInfo.SFTPDSTDIR")
          channelSftp.cd(connInfo.SFTPDSTDIR)
          channelSftp.ls(connInfo.SFTPDSTDIR)
          val filelist = channelSftp.ls(connInfo.SFTPDSTDIR)
          val ls: List[SftpFile] = listProcess(filelist)
          log.info("   ... submitted successful!")
          ls

        } catch {
          case ex: Exception =>
            log.error(ex.toString())
            List[SftpFile]()

        } finally {
          channelSftp.disconnect();
          session.disconnect();
        }
      case Failure(exception) =>
        log.error(s"${exception.getMessage}")
        List[SftpFile]()

    }

  }


}
