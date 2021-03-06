/*
 * Copyright (C) 2013-2015 by Michael Hombre Brinkmann
 */

package net.twibs.form.bootstrap3

import net.twibs.util.{Message, Session, XmlUtils}

trait FormUtils extends XmlUtils {
  implicit def wrapMessage(message: Message) = new {
    def showNotificationAfterReload(session: Session = Session.current) = session.addNotificationToSession(message.showNotification.toString + ";")
  }
}
