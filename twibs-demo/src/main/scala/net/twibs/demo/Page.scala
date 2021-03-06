/*
 * Copyright (C) 2013-2015 by Michael Hombre Brinkmann
 */

package net.twibs.demo

import net.twibs.util.{Request, GetMethod, ClassUtils}
import net.twibs.web._
import net.twibs.util.Predef._
import net.twibs.util.Formatters._

import org.threeten.bp.Instant

class Page extends Responder {
  def respond(request: Request): Option[Response] =
    if (request.path.string == "/index.html" && request.method == GetMethod) Some(indexResponse)
    else None

  def indexResponse = new StringResponse with VolatileResponse with HtmlMimeType {
    val asString = plainString
  }

  def plainString = doctype + html.toString

  def doctype = "<!DOCTYPE html>"

  val modified = Instant.ofEpochMilli(ClassUtils.getCompilationTime(getClass)).toZonedDateTime

  def yearString = modified.getYear.toString

  def dateString = modified.formatAsIso

  def html =
    <html lang="de" class="no-js">
      <head>
        <title>Twibs Demo</title>
        <meta charset="UTF-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <meta name="copyright" content={s"$yearString Twibs"}/>
        <meta name="author" content="Twibs Demo"/>
        <meta name="date" content={dateString}/>
        <meta name="description" content="Twibs Demo"/>
        <meta name="keywords" content="Java, Scala, CMS, Software"/>
        <link rel="stylesheet" href="/inc/foreign.css"/>
        <link rel="stylesheet" href="/inc/default.css"/>
        <link rel="shortcut icon" href="/favicon.ico"/>
        <script src="/inc/foreign.js"></script>
        <script src="/inc/default.js"></script>
      </head>
      <body>
        <div class="container">
          {new DemoForm().inlineHtml}
          {new DemoForm() {
          override def accessAllowed: Boolean = false
        }.inlineHtml}
        </div>
      </body>
    </html>
}
