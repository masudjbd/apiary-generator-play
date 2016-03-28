package com.thed.zephyr.api.play

import sbt._
import sbt.Keys._

/**
  * Created by masudurrahman on 2/12/16.
  */
object ApiGeneratePlugin extends AutoPlugin{

  var packageName: String = "com.thed.zephyr.connect.controllers"
  var BASE_PATH: String = "target/scala-2.10/classes/"
  var outputFileName: String = "target/apiary.txt"
  var generateApi: Boolean = true
  val apiCommand = taskKey[Unit]("Prints 'Starting Task'")

   apiCommand := {
      println("Plugin Started ")
//      val routeParser = new RouteParser(packageName,BASE_PATH)
//      routeParser.generateApiaryDoc()
      println("Plugin work done")
     }




}
