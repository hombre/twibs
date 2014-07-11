/*
 * Copyright (C) 2013-2014 by Michael Hombre Brinkmann
 */

package twibs.util

import twibs.TwibsTest

class HtmlUtilsTest extends TwibsTest {
  test("Clean html") {
    HtmlUtils.convertHtmlToPlain("<a>B <c>D</e> </a>X") should be("B D X")
    HtmlUtils.convertHtmlToPlain("<a>B <c>D</e> </a> ") should be("B D")
  }

  test("Clean paragraphs") {
    HtmlUtils.convertHtmlToPlain("<p>A <br  />\n\nB\n\n<br>C<p>D</p>") should be("A\nB\nC\n\nD")
  }

  test("Empty p") {
    HtmlUtils.convertHtmlToPlain(
      """<p></p>
        |    <p><strong>Frohes Fest</strong></p>
        |    <p>Das beste Rezept für kalte Tage: Dick einpacken, gemütlich zusammenrücken und einander warme Gedanken senden.</p>
      """.stripMargin) should be(
      """Frohes Fest
        |
        |Das beste Rezept für kalte Tage: Dick einpacken, gemütlich zusammenrücken und einander warme Gedanken senden.
        | """.stripMargin.trim)
  }

  test("Anything goes") {
    HtmlUtils.cleanup("<font color='red'>50% RABATT (bereits im") should be("50% RABATT (bereits im")
    HtmlUtils.cleanup("""<span class="red"><strong><b>50</b></strong>""") should be("""<span class="red"><strong><b>50</b></strong></span>""")
  }
}
