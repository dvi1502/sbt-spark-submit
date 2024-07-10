
resolvers +=  Resolver.file("local", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

addSbtPlugin("ru.dvi" % "sbt-spark-submit" % "0.1.0-SNAPSHOT")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")
