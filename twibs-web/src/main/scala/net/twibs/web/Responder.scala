/*
 * Copyright (C) 2013-2015 by Michael Hombre Brinkmann
 */

package net.twibs.web

import net.twibs.util.Request

trait Responder {
  def respond(request: Request): Option[Response]
}

class ResponderChain(list: Seq[Responder]) extends Responder {
  def respond(request: Request): Option[Response] = list.view.flatMap(_.respond(request)).headOption
}

object Responder {
  implicit def apply(list: Seq[Responder]) = new ResponderChain(list)
}
