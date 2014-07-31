package com.acelrtech.utils

/**
* General utilities for converting `Throwable` to `String`
*
*
*/
object Utils {
  /**
  * Return string representation of the `Throwable`
  *
  * @param exp Exception to be converted to string
  *
  */
  def stackTrace(exp: Throwable): String = {
    import java.io.PrintWriter
      import java.io.StringWriter

      val sw: StringWriter = new StringWriter();
    val pw: PrintWriter = new PrintWriter(sw)
      exp.printStackTrace(pw)
      sw.toString()
  }
}
