import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
  kotlin("jvm") version "1.5.20"
  id("java")
  id("java-gradle-plugin")
  id("maven-publish")
  id("com.gradle.plugin-publish") version "0.12.0"
  id("com.github.blueboxware.tocme") version "1.1"
}

group = properties("group")
version = properties("pluginVersion")

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.31")
  implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.31")

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
  withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "1.8"
      freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
  }

  withType<Test> {
    useJUnitPlatform()
  }

  register("createReadme") {
    inputs.file("README.md.src")
    inputs.file("versions.gradle")

    outputs.file("README.md")

    file("README.md").writeText(
      file("README.md.src").readText().replace("<releasedPluginVersion>", properties("releasedPluginVersion"))
    )

    dependsOn(project.tasks.getByName("insertTocs"))

  }

}

gradlePlugin {
  plugins {
    register("TocMe") {
      id = "com.github.blueboxware.tocme"
      implementationClass = "com.github.blueboxware.tocme.TocMePlugin"
      displayName = "TocMe"
      version = properties("version")
    }
  }
}

pluginBundle {
  website = "https://github.com/BlueBoxWare/TocMe"
  vcsUrl = "https://github.com/BlueBoxWare/TocMe.git"
  description = "Plugin to add Table of Contents to Markdown documents and keeping them up to date"
  tags = listOf("readme", "table of content", "table of contents", "toc", "markdown")
}

tocme {
  doc(file("README.md.src"))
}







