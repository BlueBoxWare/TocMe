fun properties(key: String) = project.findProperty(key).toString()

buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    val kotlinVersion = project.findProperty("kotlinVersion").toString()
    classpath("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    classpath("org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion")
    classpath("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    classpath("org.jetbrains:annotations:13.0")
  }
}

plugins {
  id("java")
  kotlin("jvm") version "1.7.10"
  id("com.gradle.plugin-publish") version "1.1.0"
  id("com.github.blueboxware.tocme") version "1.6"
}

group = properties("group")
version = properties("pluginVersion")

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:" + properties("kotlinVersion"))
  implementation("org.jetbrains.kotlin:kotlin-reflect:" + properties("kotlinVersion"))

  implementation("com.vladsch.flexmark:flexmark:" + properties("flexmarkVersion"))
  implementation("com.vladsch.flexmark:flexmark-ext-toc:" + properties("flexmarkVersion"))

  testImplementation(gradleTestKit())
  testImplementation(kotlin("test"))

  testImplementation("io.kotest:kotest-runner-junit5:" + properties("kotestVersion"))
  testImplementation("io.kotest:kotest-assertions-core:" + properties("kotestVersion"))
  testImplementation("io.kotest:kotest-framework-datatest:" + properties("kotestVersion"))

}

tasks {

  withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
  }

  withType<Test> {
    useJUnitPlatform()
  }

  register("createReadme") {
    inputs.file("README.md.src")
    inputs.file("versions.gradle")

    outputs.file("README.md")

    file("README.md").writeText(
      file("README.md.src").readText().replace(
        "<releasedPluginVersion>", properties("releasedPluginVersion")
      )
    )

    dependsOn(project.tasks.getByName("insertTocs"))

  }

}

tocme {
  doc(file("README.md.src"))
}

gradlePlugin {
  website.set("https://github.com/BlueBoxWare/TocMe")
  vcsUrl.set("https://github.com/BlueBoxWare/TocMe.git")
  plugins {
    create("TocMe") {
      id = "com.github.blueboxware.tocme"
      implementationClass = "com.github.blueboxware.tocme.TocMePlugin"
      displayName = "TocMe"
      description = "Plugin to add Table of Contents to Markdown documents and keeping them up to date"
      tags.set(listOf("readme", "table of content", "table of contents", "toc", "markdown"))
    }
  }
}









