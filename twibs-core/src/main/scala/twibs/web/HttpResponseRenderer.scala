package twibs.web

import com.google.common.base.Charsets
import com.google.common.io.ByteStreams
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.threeten.bp.ZoneOffset
import twibs.util.Predef._

private[web] class HttpResponseRenderer(request: Request, response: Response, httpRequest: HttpServletRequest, httpResponse: HttpServletResponse) {
  private val currentDateTime = Request.now()

  private def currentDateTimeInMillis = currentDateTime.toInstant(ZoneOffset.UTC).toEpochMilli

  private def expiresInMillis = currentDateTimeInMillis + expiresAfterInMillis

  private def expiresAfterInMillis = response.expiresOnClientAfter.toMillis

  def render(): Unit =
    response match {
      case r: RedirectResponse => redirectTo(r.asString)
      case r: NotFoundResponse => transfer()
      case r: ErrorResponse => transfer()
      case _ => respond()
    }

  private def redirectTo(target: String): Unit =
    httpResponse.sendRedirect(target)

  private def respond(): Unit =
    if (response.lastModified == ifModifiedSince) {
      httpResponse.setDateHeader("Expires", expiresInMillis)
      httpResponse.sendError(HttpServletResponse.SC_NOT_MODIFIED)
    } else
      transfer()

  private def transfer(): Unit = {
    httpResponse.setStatus(status(response))
    //httpResponse.setHeader("Cache-Control", "private, max-age=0")
    httpResponse.setHeader("Vary", "Accept-Encoding")
    httpResponse.setDateHeader("Date", currentDateTimeInMillis)
    httpResponse.setDateHeader("Expires", expiresInMillis)
    response.useFileName match {
      case "" => ""
      case s => httpResponse.setHeader("Content-Disposition", """attachment; filename="""" + s + '"')
    }

    if (response.mimeType.startsWith("text/"))
      httpResponse.setCharacterEncoding(Charsets.UTF_8.name)

    httpResponse.setContentType(response.mimeType)
    httpResponse.setDateHeader("Last-Modified", response.lastModified)
    transferContent()
  }

  private def status(response: Response) =
    response match {
      case r: RedirectResponse => HttpServletResponse.SC_MOVED_PERMANENTLY
      case r: NotFoundResponse => HttpServletResponse.SC_NOT_FOUND
      case r: ErrorResponse => HttpServletResponse.SC_INTERNAL_SERVER_ERROR
      case _ => HttpServletResponse.SC_OK
    }

  private def ifModifiedSince = httpRequest.getDateHeader("If-Modified-Since")

  private def transferContent(): Unit = {
    response.gzippedOption.filter(x => request.doesClientSupportGzipEncoding) match {
      case Some(bytes) =>
        httpResponse.setHeader("Content-Encoding", "gzip")
        httpResponse.setContentLength(bytes.length)
        httpResponse.getOutputStream.write(bytes)
      case None =>
        httpResponse.setContentLength(response.length.toInt)
        response.asInputStream useAndClose {is => ByteStreams.copy(is, httpResponse.getOutputStream)}
    }
    httpResponse.getOutputStream.close()
  }
}
