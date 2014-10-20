/*
 * Copyright (C) 2013-2014 by Michael Hombre Brinkmann
 */

package net.twibs.form

import net.twibs.util.Parsers._
import net.twibs.util._
import org.owasp.html.PolicyFactory

trait Input extends TranslationSupport {
  type ValueType

  case class Entry(string: String, valueOption: Option[ValueType], title: String, messageOption: Option[Message], continue: Boolean = true, index: Int = 0) {
    def valid = messageOption.isEmpty

    def withIndex(newIndex: Int) = copy(index = newIndex)

    def invalid(message: Message) = copy(messageOption = Some(message), continue = false)

    def validate(valid: => Boolean, message: => Message) = if (valid) this else invalid(message)
  }

  private[this] var _entries: Option[Seq[Entry]] = None

  private[this] var _messageOption: Option[Message] = None

  private[this] final val cachedDefaultEntries = LazyCache(computeDefaultEntries)

  final def values_=(values: Seq[ValueType]): Unit = {
    _entries = Some(values.map(valueToEntry))
    _messageOption = None
  }

  final def strings_=(strings: Seq[String]): Unit = {
    _entries = Some(strings.map(stringToEntry))
    _messageOption =
      if (_entries.get.size < minimumNumberOfEntries)
        Some(warn"minimum-number-of-entries-message: Please enter at least {$minimumNumberOfEntries, plural, =1{one value}other{# values}}")
      else if (_entries.get.size > maximumNumberOfEntries)
        Some(warn"maximum-number-of-entries-message: Please enter no more than {$maximumNumberOfEntries, plural, =1{one value}other{# values}}")
      else None
  }

  protected def stringToEntry(string: String) =
    stringProcessors(Entry(string, None, string, None)) match {
      case e if e.continue => convertToValue(e.string) match {
        case None => e.invalid(warn"format-message: Invalid format for string ''${e.string}''")
        case valueOption => valueProcessors(valueToEntry(valueOption.get))
      }
      case e => e
    }

  def stringProcessors = processTrimmed andThen processRequired

  def valueProcessors = (entry: Entry) => entry

  private def processRequired = (entry: Entry) =>
    if (entry.continue && entry.string.isEmpty)
      if (required) entry.invalid(warn"required-message: Please enter a value")
      else entry.copy(continue = false)
    else entry

  private def processTrimmed = (entry: Entry) =>
    if (trim)
      entry.copy(string = trim(entry.string))
    else entry

  protected def trim(string: String) = string.trim

  protected def valueToEntry(value: ValueType) = {
    val string = convertToString(value)
    val title = titleFor(string)
    Entry(convertToString(value), Some(value), title, None)
  }

  protected def titleFor(string: String) = string

  // Implement
  def defaults: Seq[ValueType]

  def convertToString(value: ValueType): String

  def convertToValue(string: String): Option[ValueType]

  // Overridable
  def required = true

  def trim = true

  def minimumNumberOfEntries = 1

  def maximumNumberOfEntries = 1

  def computeDefaultEntries = {
    val ret = defaults.map(valueToEntry)
    val pad = minimumNumberOfEntries - ret.size
    if (pad > 0) ret ++ List.fill(pad)(stringToEntry("")) else ret
  }

  // Accessors
  final def defaultEntries = cachedDefaultEntries.value

  final def isModified = _entries.isDefined

  final def valid = messageOption.isEmpty && entries.forall(_.messageOption.isEmpty)

  final def messageOption = _messageOption

  final def entries: Seq[Entry] = _entries getOrElse defaultEntries

  final def values: Seq[ValueType] = entries.map(_.valueOption).flatten

  final def strings: Seq[String] = entries.map(_.string)

  // Convenience methods
  final def isChanged = isModified && values != defaults

  final def entry = entries.head

  final def string = strings.head

  final def stringOrEmpty = strings.headOption getOrElse ""

  final def string_=(string: String) = strings = string :: Nil

  final def value = values.head

  final def value_=(value: ValueType) = values = value :: Nil

  final def valueOption = values.headOption

  final def valueOption_=(valueOption: Option[ValueType]) = valueOption match {
    case Some(v) => values = v :: Nil
    case None => values = Nil
  }

  final def default = defaults.head

  final def defaultOption = defaults.headOption

  final def valueOrDefault = valueOption getOrElse default

  final def withValue[R](valueArg: ValueType)(f: this.type => R): R = withValues(valueArg :: Nil)(f)

  final def withValues[R](valuesArg: Seq[ValueType])(f: this.type => R): R = {
    val was = values
    values = valuesArg
    try {
      f(this)
    } finally {
      values = was
    }
  }

}

trait Untrimmed extends Input {
  override def trim = false
}

trait Optional extends Input {
  override def required = false
}

trait TranslatedTitles extends Input {
  override protected def titleFor(string: String): String = translator.usage("value-title").translate(string, super.titleFor(string))
}

trait StringInput extends Input {
  type ValueType = String

  override def defaults: Seq[String] = "" :: Nil

  override def convertToString(value: ValueType): String = value

  override def convertToValue(string: String): Option[ValueType] = Some(string)

  override def stringProcessors: (Entry) => Entry = super.stringProcessors andThen minimumLengthProcessor andThen maximumLengthProcessor andThen regexProcessor

  private def minimumLengthProcessor = (entry: Entry) => entry.validate(entry.string.length >= minimumLength, warn"minimum-length-message: Please enter at least $minimumLength characters")

  private def maximumLengthProcessor = (entry: Entry) => entry.validate(entry.string.length <= maximumLength, warn"maxiumum-length-message: Please enter no more than $maximumLength characters")

  private def regexProcessor = (entry: Entry) => entry.validate(regex.isEmpty || entry.string.matches(regex), warn"regex-message: Please enter a string that matches ''$regex''")

  // Overideable
  def minimumLength = 0

  def maximumLength = Int.MaxValue

  def regex = ""
}

trait SingleLineInput extends StringInput {
  override def stringProcessors: (Entry) => Entry = super.stringProcessors andThen checkLineBreaks

  override protected def trim(string: String): String = super.trim(super.trim(string).stripLineEnd)

  private def checkLineBreaks = (entry: Entry) =>
    if (entry.continue && (entry.string.contains("\n") || entry.string.contains("\r"))) entry.invalid(warn"line-breaks-message: Value must not contain line breaks")
    else entry
}

trait EmailAddressInput extends SingleLineInput {
  override def stringProcessors = super.stringProcessors andThen emailAddressProcessor

  private def emailAddressProcessor = (entry: Entry) => entry.validate(EmailUtils.isValidEmailAddress(entry.string), warn"format-message: ''${entry.string}'' is not a valid email address")
}

trait WebAddressInput extends SingleLineInput {
  override def stringProcessors = super.stringProcessors andThen webAddressProcessor

  private def webAddressProcessor = (entry: Entry) => entry.validate(UrlUtils.isValidWebAddress(entry.string), warn"format-message: ''${entry.string}'' is not a valid web address")
}

trait HtmlInput extends StringInput {
  def policyFactory: PolicyFactory

  override def stringProcessors = super.stringProcessors andThen cleanupHtml

  private def cleanupHtml = (entry: Entry) => entry.copy(string = policyFactory.sanitize(string))
}

trait LongInput extends Input {
  override type ValueType = Long

  override def defaults = 0L :: Nil

  override def convertToString(l: Long) = l.toString

  override def convertToValue(string: String) = string.toLongOption
}

trait IntInput extends Input {
  override type ValueType = Int

  override def defaults = 0 :: Nil

  override def convertToString(i: Int) = i.toString

  override def convertToValue(string: String) = string.toIntOption
}

trait Options extends Input {
  def options: Seq[ValueType]

  def optionEntries: Seq[Entry] = options.map(super.valueToEntry)

  override protected def valueToEntry(value: ValueType): Entry =
    optionEntries.find(_.valueOption == Some(value)) getOrElse (throw new FormException("Value must be in options"))

  override protected def stringToEntry(string: String): Entry =
    optionEntries.find(_.string == string) getOrElse Entry(string, None, string, Some(warn"not-an-option-message: ''$string'' is not an option"))
}