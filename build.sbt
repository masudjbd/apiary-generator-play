sbtPlugin := true

organization := "com.thed.zephyr.api.play"

name := "sbt-api"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  ("com.atlassian.connect" % "ac-play-java_2.10" % "0.10.1" withSources())
    .exclude("org.bouncycastle", "bcprov-jdk15on"),
  "com.atlassian.fugue" % "fugue" % "2.0.0",
  "com.atlassian.security" % "atlassian-secure-random" % "3.2.4",
  "com.basho.riak" % "riak-client" % "2.0.0",
  "com.google.inject" % "guice" % "3.0",
  "com.hazelcast" % "hazelcast" % "3.5.2",
  "com.hazelcast" % "hazelcast-cloud" % "3.5.2",
  "joda-time" % "joda-time" % "2.5",
  "org.kamranzafar" % "jtar" % "2.3",
  ("com.clever-age" % "play2-elasticsearch" % "1.4.1-SNAPSHOT"),
  //    .exclude("org.elasticsearch", "elasticsearch"),
  //  "org.elasticsearch" % "elasticsearch" % "1.3.2",
  "com.atlassian.jira" % "jira-rest-java-client-core" % "2.0.0-m30" withSources(),
  "com.eaio.uuid" % "uuid" % "3.2",
  "org.julienrf" %% "play-jsmessages" % "1.6.1",
  "commons-beanutils" % "commons-beanutils" % "1.9.2",
  "com.amazonaws" % "aws-java-sdk" % "1.10.27",
  "org.imgscalr" % "imgscalr-lib" % "4.2",
  "org.apache.tika" % "tika-parsers" % "1.4",
  "org.apache.commons" % "commons-exec" % "1.2",
  "org.xerial.snappy" % "snappy-java" % "1.1.1.6",
  //Metrics
  "io.dropwizard.metrics" % "metrics-core" % "3.1.0" withSources(),
  "io.dropwizard.metrics" % "metrics-json" % "3.1.0" withSources(),
  //For Reporting, for configuration: see https://github.com/sdb/play2-metrics
  "io.dropwizard.metrics" % "metrics-graphite" % "3.1.0",
  "com.palominolabs.metrics" % "metrics-guice" % "3.1.3" withSources(),
  "com.googlecode.jatl" % "jatl" % "0.2.2",
  //TEST Dependencies
  "org.mockito" % "mockito-all" % "1.9.5" % "test",
  "com.wordnik" %% "swagger-play2" % "1.3.6" exclude("org.reflections", "reflections"),
  "org.reflections" % "reflections" % "0.9.8" notTransitive ()
)


resolvers +=  "Atlassian's Maven Public Repository" at "https://maven.atlassian.com/content/groups/public"

resolvers += "pk11" at "http://pk11-scratch.googlecode.com/svn/trunk"

//resolvers += "inhouse repo" at "http://192.168.100.200:9999/repository"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Typesafe Releases" at "http://typesafe.artifactoryonline.com/typesafe/repo"

resolvers += "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository"