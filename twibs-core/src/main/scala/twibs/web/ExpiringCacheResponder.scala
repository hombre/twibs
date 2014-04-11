/*
 * Copyright (C) 2013-2014 by Michael Hombre Brinkmann
 */

package twibs.web

import com.google.common.cache.{CacheLoader, CacheBuilder, LoadingCache}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._

class ExpiringCacheResponder(delegate: Responder, duration: Duration = 1 second) extends CacheResponder {
  def respond(request: Request): Option[Response] =
    request.use {
      val requestCacheKey = request.cacheKey
      if (!Request.useCache) {
        cache.invalidate(requestCacheKey)
      }
      respond(requestCacheKey)
    }

  protected val cache: LoadingCache[RequestCacheKey, Option[Response]] =
    CacheBuilder.newBuilder().expireAfterWrite(duration.toMillis, TimeUnit.MILLISECONDS).build(loader)

  private def loader = new CacheLoader[RequestCacheKey, Option[Response]]() {
    def load(requestCacheKey: RequestCacheKey): Option[Response] = delegate.respond(Request)
  }
}
