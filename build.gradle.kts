import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter

fun properties(key: String) = project.findProperty(key).toString()

plugins {
  `kotlin-dsl`
  id("com.gradle.plugin-publish") version "1.3.1"
  id("com.github.blueboxware.tocme") version "1.8"
}

group = properties("group")
version = properties("pluginVersion")

repositories {
  mavenCentral()
}

dependencies {

  implementation("com.vladsch.flexmark:flexmark:" + properties("flexmarkVersion"))
  implementation("com.vladsch.flexmark:flexmark-ext-toc:" + properties("flexmarkVersion"))

  testImplementation(gradleTestKit())
  testImplementation(kotlin("test"))

  testImplementation("io.kotest:kotest-runner-junit5:" + properties("kotestVersion"))
  testImplementation("io.kotest:kotest-assertions-core:" + properties("kotestVersion"))

}

@Suppress("UnstableApiUsage")
tasks.register<Test>("testInsertToc").configure {
  filter {
    includeTestsMatching("TestInsertToc")
  }
  group = "verification"
  val test by testing.suites.existing(JvmTestSuite::class)
  testClassesDirs = files(test.map { it.sources.output.classesDirs })
  classpath = files(test.map { it.sources.runtimeClasspath })

}

abstract class TestTask: Test() {
  @get:Input
  abstract val useBuildCache: Property<Boolean>

  @get:Input
  abstract val useConfigurationCache: Property<Boolean>

  @get:Input
  abstract val version: Property<String>

  override fun getGroup(): String = "verification"

  override fun getFilter(): TestFilter? {
    return DefaultTestFilter()
      .includeTestsMatching("TestGradle")
      .includeTestsMatching("TestExclusions")
      .includeTestsMatching("TestReadme")
  }

  @TaskAction
  override fun executeTests() {
    systemProperty("gdxplugin.cache.build", useBuildCache.get())
    systemProperty("gdxplugin.cache.configuration", useConfigurationCache.get())
    systemProperty("gdxplugin.gradle.version", version.get())

    super.executeTests()
  }

}

@Suppress("UnstableApiUsage")
for (useBuildCache in listOf(false, true)) {
  for (useConfCache in listOf(false, true)) {
    for (version in listOf("8.3", "8.14.3", "9.1.0")) {
      val name = buildString {
        append("test")
        if (useConfCache) append("ConfCache")
        if (useBuildCache) append("BuildCache")
        append("Version")
        append(version)
      }
      val test by testing.suites.existing(JvmTestSuite::class)
      val task = tasks.register<TestTask>(name) {
        this.useBuildCache = useBuildCache
        this.useConfigurationCache = useConfCache
        this.version = version
        testClassesDirs = files(test.map { it.sources.output.classesDirs })
        classpath = files(test.map { it.sources.runtimeClasspath })
      }
      tasks.test {
        dependsOn(task)
      }
    }
  }
}

tasks {

  test {
    exclude("*")
    dependsOn(
      project.tasks.named("testInsertToc")
    )
  }

  withType<Test>().configureEach {
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
      version = properties("releasedPluginVersion")
      tags.set(listOf("readme", "table of content", "table of contents", "toc", "markdown"))
    }
  }
}









