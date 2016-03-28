package com.thed.zephyr.api.play

/**
  * Created by masudurrahman on 2/12/16.
  */
import sbt._

trait ApiKeys {
  lazy val packageName = SettingKey[String]("package-name")
  lazy val basePath = SettingKey[String]("base-path")
  lazy val generate = SettingKey[Boolean]("generate", "Should this project's generate apiary blueprint ?")
}

object ApiKeys extends ApiKeys
