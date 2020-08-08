@file:Suppress("PackageDirectoryMismatch")

import com.github.blueboxware.tocme.util.TocMeOptionsImpl
import com.github.blueboxware.tocme.util.insertTocs
import java.io.File

internal fun main(ps: Array<String>) {
  val inputFile = ps.getOrNull(0)?.let { File(it) } ?: return

  val (result, warnings, error) = insertTocs(inputFile, null, TocMeOptionsImpl(null))

  for (warning in warnings) {
    System.err.println(warning)
  }

  error?.let {
    System.err.println(error)
  }

  result?.let {
    println(result)
  }
}