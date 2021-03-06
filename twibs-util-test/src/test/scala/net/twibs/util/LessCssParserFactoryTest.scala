/*
 * Copyright (C) 2013-2014 by Michael Hombre Brinkmann
 */

package net.twibs.util

import com.google.common.base.Charsets
import com.google.common.io.Files
import java.io.{FileNotFoundException, File}

import net.twibs.testutil.TwibsTest

class LessCssParserFactoryTest extends TwibsTest {
  val dir = new File("src/test/webapp/www1")
  val parser = LessCssParserFactory.createParser(load)

  def load(path: String): Option[String] = try {
    Some(Files.toString(new File(dir, path), Charsets.UTF_8))
  } catch {
    case e: FileNotFoundException => None
  }

  test("Simple") {
    parser.parse("/simple.less", compress = false, 1) should be("div {\n  width: 3;\n}\n")
    parser.parse("/simple.less") should be("div{width:3}")
  }

  test("Does not exists") {
    intercept[LessCssParserException] {
      parser.parse("/does-not-exists.less")
    }.getMessage should be("java.io.FileNotFoundException: /does-not-exists.less")
  }

  test("Syntax error") {
    intercept[LessCssParserException] {
      parser.parse("/syntax-error.less")
    }.getMessage should be("Parse Error: missing closing `}` in '/syntax-error.less' (line 1, column 2) near\na {\n  color: red;")
    intercept[LessCssParserException] {
      parser.parse("/import-syntax-error.less")
    }.getMessage should be("Parse Error: missing closing `}` in '/syntax-error.less' (line 1, column 2) near\na {\n  color: red;")
  }

  test("Missing variable") {
    intercept[LessCssParserException] {
      parser.parse("/missing-variable.less")
    }.getMessage should be("Name Error: variable @color-red is undefined in '/missing-variable.less' (line 2, column 9) near\na {\n  color: @color-red; // Missing variable\n}")
    intercept[LessCssParserException] {
      parser.parse("/import-missing-variable.less")
    }.getMessage should be("Name Error: variable @color-red is undefined in '/missing-variable.less' (line 2, column 9) near\na {\n  color: @color-red; // Missing variable\n}")
  }

  test("Import simple") {
    parser.parse("/import-simple.less") should be("div{width:3}")
  }

  test("Imported file does not exists") {
    intercept[LessCssParserException] {
      parser.parse("/subdir/import-missing-file.less")
    }.getMessage should be("java.io.FileNotFoundException: /subdir/../../missing-file.less")
  }
}
