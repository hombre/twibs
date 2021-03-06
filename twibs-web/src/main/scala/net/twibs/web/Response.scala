/*
 * Copyright (C) 2013-2015 by Michael Hombre Brinkmann
 */

package net.twibs.web

import com.google.common.base.Charsets
import com.google.common.io.{CharStreams, ByteStreams}
import concurrent.duration._
import java.io._
import java.util.zip.GZIPOutputStream
import net.twibs.util.Predef._
import net.twibs.util.{ApplicationSettings, RunMode}

import scala.language.postfixOps

trait Response extends Serializable {
  def asInputStream: InputStream

  def asBytes: Array[Byte]

  def asString: String

  def length: Long

  def lastModified: Long

  def mimeType: String

  def isModified: Boolean

  def isCacheable: Boolean

  def expiresOnClientAfter: Duration

  def isContentFinal: Boolean = false

  def isInMemory: Boolean

  lazy val gzippedOption: Option[Array[Byte]] = {
    val bytes = asInputStream useAndClose compressWithGzip
    if (bytes.length < length) Some(bytes) else None
  }

  private def compressWithGzip(uncompressed: InputStream) = {
    val baos = new ByteArrayOutputStream()
    new GZIPOutputStream(baos) useAndClose {ByteStreams.copy(uncompressed, _)}
    baos.toByteArray
  }

}

trait InputStreamResponse extends Response {
  def asBytes: Array[Byte] = ByteStreams.toByteArray(asInputStream)

  def asString: String = CharStreams.toString(new InputStreamReader(asInputStream, Charsets.UTF_8))

  def isInMemory: Boolean = false
}

trait FileResponse extends InputStreamResponse {
  def file: File

  def asInputStream = new FileInputStream(file)

  val length = file.length()

  val lastModified = file.lastModified()

  def isModified = !file.exists || file.lastModified() != lastModified

  lazy val mimeType = ApplicationSettings.tika.detect(file)
}

trait StringResponse extends Response {
  def asBytes: Array[Byte] = asString.getBytes(Charsets.UTF_8)

  def asInputStream: InputStream = new ByteArrayInputStream(asBytes)

  def length: Long = asBytes.length

  def isInMemory: Boolean = true
}

trait ByteArrayResponse extends Response {
  def asString = new String(asBytes, Charsets.UTF_8)

  def asInputStream: InputStream = new ByteArrayInputStream(asBytes)

  def length: Long = asBytes.length

  def isInMemory: Boolean = true
}

class RedirectResponse(val asString: String) extends StringResponse {
  def lastModified: Long = 0

  def mimeType: String = ""

  def isModified: Boolean = true

  def isCacheable: Boolean = false

  def expiresOnClientAfter = if (RunMode.isDevelopment) 1 seconds else 8 hours

  override def isContentFinal: Boolean = true
}

trait ErrorResponse extends Response

trait NotFoundResponse extends Response

trait CacheableResponse extends Response {
  def isCacheable = true

  def expiresOnClientAfter = if (RunMode.isDevelopment) 1 seconds else 8 hours
}

trait NotCacheableResponse extends Response {
  def isCacheable: Boolean = false

  def expiresOnClientAfter = 0 seconds
}

trait VolatileResponse extends NotCacheableResponse {
  def isModified: Boolean = true

  val lastModified: Long = System.currentTimeMillis()
}

trait CalculatedLastModifiedResponse extends CacheableResponse {
  val lastModified: Long = calculateModified

  def isModified: Boolean = lastModified != calculateModified

  def calculateModified: Long
}

trait CompilationTimeResponse extends CalculatedLastModifiedResponse {
  def calculateModified: Long = compilationTime // if (RunMode.isDevelopment) System.currentTimeMillis() else compilationTime

  def compilationTime: Long
}

trait SingleResponseWrapper extends Response {
  protected def delegatee: Response

  def lastModified: Long = delegatee.lastModified

  def isModified: Boolean = delegatee.isModified

  def expiresOnClientAfter: Duration = delegatee.expiresOnClientAfter

  def isCacheable: Boolean = delegatee.isCacheable

  override def isContentFinal = delegatee.isContentFinal
}

trait MultiResponseWrapper extends Response {
  protected def delegatees: List[Response]

  lazy val lastModified: Long = delegatees.map(_.lastModified).max

  def isModified = delegatees.exists(_.isModified)

  lazy val expiresOnClientAfter = delegatees.map(_.expiresOnClientAfter).min

  lazy val isCacheable = delegatees.forall(_.isCacheable)

  override lazy val isContentFinal = delegatees.forall(_.isContentFinal)
}

class DecoratableResponseWrapper(val delegatee: Response) extends SingleResponseWrapper {
  def asInputStream: InputStream = delegatee.asInputStream

  def asBytes: Array[Byte] = delegatee.asBytes

  def asString: String = delegatee.asString

  def length: Long = delegatee.length

  def mimeType: String = delegatee.mimeType

  def isInMemory = delegatee.isInMemory
}

trait AsAttachment extends Response {
  def attachmentFileName: String
}
