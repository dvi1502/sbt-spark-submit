package ru.dvi.sbt.sparksubmit

class Jar(projectName: String, projetVesrion: String, scalaVersion: String) {

  val shortScalaVersion = scalaVersion.substring(0, scalaVersion.lastIndexOf("."))

  def fileFolder = s"target/scala-${shortScalaVersion}"

//  def fileName = s"${projectName}_${shortScalaVersion}-${projetVesrion}.jar"
  def fileName = s"${projectName}-assembly-${projetVesrion}.jar"

  def filePath = s"${fileFolder}/${fileName}"

  def file: java.io.File = new java.io.File(filePath)
}