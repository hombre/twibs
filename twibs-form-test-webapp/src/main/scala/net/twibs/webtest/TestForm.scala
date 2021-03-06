/*
 * Copyright (C) 2013-2015 by Michael Hombre Brinkmann
 */

package net.twibs.webtest

import com.ibm.icu.util.ULocale
import net.twibs.form._
import net.twibs.util._
import net.twibs.util.XmlUtils._

class TestForm extends Form("test") with HorizontalForm {
  override def formTitleHtml = <h1>{formTitleString}</h1>

  val mode = new RadioField("mode") with StringInput with SubmitOnChange with RadioInlineLayout {
    override def options = "enabled" :: "disabled" :: "hidden" :: "ignored" :: Nil

    override def defaults: Seq[ValueType] = "enabled" :: Nil
  }

  new Button("submit") with SimpleButton with PrimaryDisplayType with ExecuteValidated with DefaultButton

  val hl = new HorizontalLayout {
    override protected def selfIsDisabled: Boolean = mode.string == "disabled"

    override protected def selfIsHidden: Boolean = mode.string == "hidden"

    override protected def selfIsIgnored: Boolean = mode.string == "ignored"

    >> {<h3>Language Select List</h3>}

    new SingleSelectField("countries") with StringInput with Chosen {
      override def options: Seq[String] = ULocale.getISOCountries

      override protected def titleFor(string: String): String =
        ULocale.getDisplayCountry(Request.locale.getLanguage + "_" + string, Request.locale)

      override def optionHtmlFor(entry: Entry, option: Entry) =
        super.optionHtmlFor(entry, option).set("data-text", <span><i class={s"flag flag-${option.string.toLowerCase}"}></i> {option.title}</span>)
    }

    >> {<h3>Dynamic Container</h3>}
    new ChildContainer("dynamic") with DynamicParent {
      override type T = DynamicChild

      override def minimumNumberOfDynamics: Int = 2

      override def createChild(): T = new HorizontalLayout with DynamicChild {
        new SingleLineField("first-name") with StringInput
        new SingleLineField("last-name") with StringInput
      }
    }

    >> {<h3>Multiselect Fields</h3>}

    new MultiSelectField("multi-select-chosen") with StringInput with Chosen {
      override def options: Seq[ValueType] = "Dear" :: "Bear" :: "Lion" :: Nil
    }

    new MultiSelectField("multi-select") with StringInput {
      override def options: Seq[ValueType] = "Dear" :: "Bear" :: "Lion" :: Nil
    }

    new MultiSelectField("multi-select-optional-chosen") with StringInput with Chosen with Optional {
      override def options: Seq[ValueType] = "Dear" :: "Bear" :: "Lion" :: Nil
    }

    new MultiSelectField("multi-select-optional") with StringInput with Optional {
      override def options: Seq[ValueType] = "Dear" :: "Bear" :: "Lion" :: Nil
    }

    >> {<h3>Integer Field</h3>}
    new IntField("int-values") with SubmitOnChange {
      override def execute(): Seq[Result] =
        if (isSubmittedOnChange) AfterFormDisplay(info"pressed: Date time changed: $string".showNotification)
        else Ignored

      override def minimumNumberOfEntries: Int = 1

      override def maximumNumberOfEntries: Int = 3

      override def minimum: Option[Int] = Some(3)

      override def maximum: Option[Int] = Some(8)
    }

    >> {<h3>Date Time Field</h3>}
    new DateTimeField("date-time-values") with SubmitOnChange {
      override def execute(): Seq[Result] =
        if (isSubmittedOnChange) AfterFormDisplay(info"pressed: Date time changed: $string".showNotification)
        else Ignored

      override def minimumNumberOfEntries: Int = 1

      override def maximumNumberOfEntries: Int = 3
    }

    >> {<h3>Date Field</h3>}
    new DateField("date-values") with SubmitOnChange {
      override def execute(): Seq[Result] =
        if (isSubmittedOnChange) AfterFormDisplay(info"pressed: Date changed: $string".showNotification)
        else Ignored

      override def minimumNumberOfEntries: Int = 1

      override def maximumNumberOfEntries: Int = 3
    }

    >> {<h3>Single Select Fields</h3>}

    new SingleSelectField("single-select-multiple-values") with StringInput with SubmitOnChange {
      override def options = "Dear" :: "Bear" :: "Lion" :: Nil

      override def execute(): Seq[Result] =
        if (isSubmittedOnChange) AfterFormDisplay(info"pressed: Single select changed: $string".showNotification)
        else Ignored

      override def defaults: Seq[ValueType] = "" :: Nil

      override def minimumNumberOfEntries: Int = 1

      override def maximumNumberOfEntries: Int = 3
    }

    new SingleSelectField("chosen-single-select-multiple-values") with StringInput with Chosen with SubmitOnChange {
      override def options = "Dear" :: "Bear" :: "Lion" :: Nil

      override def execute(): Seq[Result] =
        if (isSubmittedOnChange) AfterFormDisplay(info"pressed: Single select changed: $string".showNotification)
        else Ignored

      override def defaults: Seq[ValueType] = "" :: Nil

      override def minimumNumberOfEntries: Int = 1

      override def maximumNumberOfEntries: Int = 3
    }

    new SingleSelectField("single-select-multiple-values") with StringInput with SubmitOnChange with Optional {
      override def options = "Dear" :: "Bear" :: "Lion" :: Nil
    }

    new SingleSelectField("chosen-single-select-multiple-values") with StringInput with Chosen with SubmitOnChange with Optional {
      override def options = "Dear" :: "Bear" :: "Lion" :: Nil
    }

    >> {<h3>Single Line Fields</h3>}
    new SingleLineField("single-line-multiple-values") with StringInput with SubmitOnChange {
      override def execute(): Seq[Result] =
        if (isSubmittedOnChange) AfterFormDisplay(info"pressed: Single select changed: $string".showNotification)
        else Ignored

      override def defaults: Seq[ValueType] = "" :: Nil

      override def minimumNumberOfEntries: Int = 1

      override def maximumNumberOfEntries: Int = 3
    }

    >> {<h3>Multi Line Fields</h3>}
    new MultiLineField("multi-line-multiple-values") with StringInput with SubmitOnChange {
      override def maximumNumberOfEntries: Int = 3
    }

    >> {<h3>Html Fields</h3>}
    new HtmlField("html-multiple-values") with StringInput with SubmitOnChange {
      override def defaults: Seq[ValueType] = "" :: "" :: Nil

      override def maximumNumberOfEntries: Int = 3
    }

    >> {<h3>Radio Buttons</h3>}
    val multipleValues = new RadioField("radio-multiple-values") with StringInput with SubmitOnChange {
      override def options = "Dear" :: "Bear" :: "Lion" :: Nil

      override def execute(): Seq[Result] =
        if (isSubmittedOnChange) AfterFormDisplay(info"pressed: Radio button changed: $string".showNotification)
        else Ignored

      override def defaults: Seq[ValueType] = "a" :: Nil

      override def minimumNumberOfEntries: Int = 1

      override def maximumNumberOfEntries: Int = 3
    }

    val singleValue = new RadioField("radio-single-value") with StringInput with SubmitOnChange {
      override def options = "Dear" :: "Bear" :: "Lion" :: Nil

      override def execute(): Seq[Result] =
        if (isSubmittedOnChange) AfterFormDisplay(info"pressed: Radio button changed: $string".showNotification)
        else Ignored

      override def defaults: Seq[ValueType] = "a" :: "" :: Nil

      override def minimumNumberOfEntries: Int = 1

      override def maximumNumberOfEntries: Int = 1
    }

    val inlineLayout = new RadioField("radio-single-value-inline") with StringInput with SubmitOnChange with RadioInlineLayout {
      override def options = "Dear" :: "Bear" :: "Lion" :: Nil

      override def execute(): Seq[Result] =
        if (isSubmittedOnChange) AfterFormDisplay(info"pressed: Radio button changed: $string".showNotification)
        else Ignored

      override def defaults: Seq[ValueType] = "a" :: "" :: Nil

      override def minimumNumberOfEntries: Int = 1

      override def maximumNumberOfEntries: Int = 1
    }

    >> {<h3>Checkboxes</h3>}
    >> {<p>Simple boolean checkbox with submit on change</p>}
    new CheckboxField("boolean-checkbox") with BooleanCheckboxField with SubmitOnChange

    >> {<p>With two options. First label configured in application.conf</p>}
    new CheckboxField("checkbox-enabled") with StringInput {
      override def options = "a" :: "b" :: Nil
    }


    //  >> {<h3>Popover</h3>}
    //  >> {<h4>Clicking the next button shows a popover containing another button</h4>}
    // TODO: Enabled Popover again
    //    new Popover("popover") with WarningDisplayType {
    //      new Button("popover-button") with PrimaryDisplayType with StringInput {
    //        override def execute(): Seq[Result] = AfterFormDisplay(info"pressed: Popover button pressed".showNotification)
    //      }
    //    }

    >> {<h3>Buttons</h3>}
    new Button("button") with SimpleButton with PrimaryDisplayType

    >> {<h4>One button with three different values</h4>}
    >> {<p>There is a control label, the first button takes its label, icon and display-type from the values of application.conf,
     the second and third take the defaults.</p>}
    new Button("button-with-three-values") with StringInput with Options with DefaultDisplayType {
      override def options: Seq[String] = "a" :: "b" :: "c" :: Nil

      override def execute(): Seq[Result] =
        if (validate()) AfterFormDisplay(info"pressed: Pressed ''$value''".showNotification)
        else AfterFormDisplay(warn"invalid: Invalid Value selected ''$string''".showNotification)
    }

    >> {<h4>A button row containing two buttons.</h4>}
    >> {<p>Clicking the first produces an Internal Server Error, clicking the second
      waits 2 seconds before returning. Transfer modal should show up.</p>}
    new ButtonRow {
      new Button("internal-server-error") with SimpleButton with DangerDisplayType {
        override def execute(): Seq[Result] = throw new RuntimeException("Internal Server Error")
      }
      new Button("wait-2-seconds") with SimpleButton with WarningDisplayType {
        override def execute(): Seq[Result] = Thread.sleep(2000)
      }
    }

    val floatingButton = new Button("floating") with StringInput with DefaultDisplayType with Floating with DynamicOptions {
      override def execute(): Seq[Result] = AfterFormDisplay(info"pressed: Pressed $string".showNotification)
    }

    >> {<h4>Use floating buttons to display inside html</h4>}
    >> {<p>First Button with value 1 {floatingButton.withOption("1")(_.html)}. Second {floatingButton.withOption("2")(_.html)} has value 2</p>}

  }

  new Button("submit") with SimpleButton with PrimaryDisplayType with ExecuteValidated with DefaultButton

  val openModal = new Button("open-modal") with OpenModalLinkButton with PrimaryDisplayType

  >> {<h3>Modal</h3>}
  >> {<h4>Open a copy of this form in a modal dialog</h4>}
  >> {
    Request.copy(parameters = Request.parameters.copy(parameterMap = Request.parameters.parameterMap)).use {
      new TestForm().openModal.html
    }
  }
}
