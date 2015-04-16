/*
 * Copyright (C) 2013-2015 by Michael Hombre Brinkmann
 */

package net.twibs.webtest

import net.twibs.form._
import net.twibs.util.{DefaultDisplayType, PrimaryDisplayType}

class TestForm extends Form("test") with Bs3Form {
  val openModal = new OpenModalLink() with PrimaryDisplayType

  new HorizontalLayout {
    >> {<h3>Buttons</h3>}
    >> {<h4>Four simple buttons (only two of them are shown)</h4>}

    new Button("enabled") with BooleanButton with PrimaryDisplayType
    new Button("disabled") with BooleanButton with PrimaryDisplayType {
      override protected def computeDisabled: Boolean = true
    }
    new Button("hidden") with BooleanButton with PrimaryDisplayType {
      override protected def computeHidden: Boolean = true
    }
    new Button("ignored") with BooleanButton with PrimaryDisplayType {
      override protected def computeIgnored: Boolean = true
    }

    >> {<h4>One button with three different values</h4>}

    new Button("three-values") with StringInput with Options with DefaultDisplayType {
      override def options: Seq[String] = "a" :: "b" :: "c" :: Nil

      override def execute(): Seq[Result] =
        if (validate()) AfterFormDisplay(info"pressed: Pressed $value".showNotification)
        else AfterFormDisplay(warn"invalid: Invalid Value selected ''$string''".showNotification)
    }

    >> {<h4>Clicking this button produces an Internal Server Error</h4>}

    new Button("internal-server-error") with BooleanButton with DefaultDisplayType {
      override def execute(): Seq[Result] = throw new RuntimeException("Internal Server Error")
    }

    >> {<h4>Clicking this button waits 5 seconds before returning. Transfer modal should show up.</h4>}

    new Button("wait-5-seconds") with BooleanButton with DefaultDisplayType {
      override def execute(): Seq[Result] = Thread.sleep(5000)
    }

    val floatingButton = new Button("floating") with StringInput with DefaultDisplayType with Floating {
      override def execute(): Seq[Result] = AfterFormDisplay(info"pressed: Pressed $value".showNotification)

      override def options: Seq[ValueType] = "1" :: "2" :: Nil
    }

    >> {<h4>Use floating buttons to display inside html</h4>}
    >> {<p>First Button with value 1: {floatingButton.withValue("1")(_.html)}</p>}
    >> {<p>Second {floatingButton.withValue("2")(_.html)} has value 2</p>}

    >> {<h3>Popover</h3>}
    >> {<h4>Clicking the next button shows a popover containing another button</h4>}


    // TODO: Enabled Popover again
//    new Popover("popover") with WarningDisplayType {
//      new Button("popover-button") with PrimaryDisplayType with StringInput {
//        override def execute(): Seq[Result] = AfterFormDisplay(info"pressed: Popover button pressed".showNotification)
//      }
//    }

    >> {<h3>Fields</h3>}
    >> {<h4>Four simple input fields (only two of them are shown) one is rendered hidden</h4>}

    val enabled = new SingleLineField("enabled") with StringInput
    enabled.strings = "" :: "" :: Nil
//    enabled.validate()

    >> {<h5>Even though the disabled field has values and is validated no validation information is displayed</h5>}
    val disabled = new SingleLineField("disabled") with StringInput {
      override protected def computeDisabled: Boolean = true
    }
    disabled.strings = "" :: "" :: Nil
    disabled.validate()

    new SingleLineField("hidden") with StringInput {
      override protected def computeHidden: Boolean = true
    }
    new SingleLineField("ignored") with StringInput {
      override protected def computeIgnored: Boolean = true
    }

    new SingleSelectField("single-select") with StringInput with Chosen with Optional {
      override def options: Seq[ValueType] = "Dear" :: "Bear" :: "Lion" :: Nil
    }

    new MultiSelectField("multi-select") with StringInput with Chosen {
      override def options: Seq[ValueType] = "Dear" :: "Bear" :: "Lion" :: Nil
    }

    >> {<h4>Multiline</h4>}
    new MultiLineField("multiline") with StringInput

    new HtmlField("html") with StringInput

    >> {<h4>Checkboxes &amp; Radiobuttons</h4>}
    new CheckboxField("boolean") with BooleanCheckboxField

    new CheckboxField("checkbox") with StringInput {
      override def options = "a" :: "b" :: Nil
    }

    new RadioField("radio") with StringInput with SubmitOnChange {
      override def options = "a" :: "b" :: Nil

      override def execute(): Seq[Result] =
        if (isSubmittedOnChange) AfterFormDisplay(info"pressed: Radio button changed: $string".showNotification)
        else Ignored
    }

    >> {<h3>Modal</h3>}
    >> {<h4>Open a copy of this form in a modal dialog</h4>}
    >> {new TestForm().openModal.html}
  }
}
