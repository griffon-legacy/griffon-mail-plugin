/*
 * Copyright 2009-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package griffon.plugins.mail

import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

import griffon.util.ApplicationHolder

/**
 * Sends email from a Griffon application.
 *
 * @author Josh A. Reed
 */
class DefaultMailProvider implements MailProvider {
    private static final DefaultMailProvider INSTANCE

    static {
        INSTANCE = new DefaultMailProvider()
    }

    static DefaultMailProvider getInstance() {
        INSTANCE
    }

    /**
     * Send an email using the arguments specified.  The following arguments are recognized:
     *  <ul>
     *      <li>transport - either 'smtp' for regular SMTP or 'smtps' for SMTP with SSL.  Defaults to 'smtp'</li>
     *      <li>mailhost  - (required) the address of the SMTP server, e.g. 'smtp.google.com'</li>
     *      <li>port      - the port of the SMTP server.  Defaults appropriately for the transport specified.</li>
     *      <li>auth      - true if authentication is required, false otherwise.  Defaults to false (no auth).</li>
     *      <li>user      - the username for authenticating with the SMTP server.</li>
     *      <li>password  - the password for authenticating with the SMTP server.</li>
     *      <li>from      - the message sender, e.g. 'foo@bar.com'</li>
     *      <li>to        - (required) the message recipients, e.g. 'foo@bar.com'.  Multiple addresses may be specified as a comma-separated list.</li>
     *      <li>cc        - the CC recipients, e.g. 'foo@bar.com'.  Multiple addresses may be specified as a comma-separated list.</li>
     *      <li>bcc       - the BCC recipients, e.g. 'foo@bar.com'.  Multiple addresses may be specified as a comma-separated list.</li>
     *      <li>subject   - the message subject.</li>
     *      <li>text      - the plain text message content.</li>
     *      <li>html      - the html message content.</li>
     *      <li>attachments - the list of file paths to attach to the message.</li>
     *  </ul>
     *
     * Future versions of this service will support HTML and file attachments.
     */
    void withMail(Map<String, Object> args) {
        ConfigObject config = new ConfigObject()
        ConfigObject mailConfig = ApplicationHolder.application.config.griffon.mail
        if (mailConfig) config.merge(mailConfig)
        config.putAll(args)
        println config

        if (config.disabled) return

        if (!config.mailhost) throw new RuntimeException('No mail host specified')
        if (!config.to)       throw new RuntimeException('No recipient specified')

        // default to smtp if no explicit transport set
        String transport = config.transport ?: 'smtp'

        // set system properties
        if (!config.props['mail.' + transport + '.host']) config.props['mail.' + transport + '.host'] = config.mailhost
        if (!config.props['mail.' + transport + '.port']) config.props['mail.' + transport + '.port'] = config.port
        if (config.auth) config.props['mail.' + transport + '.auth'] = 'true'

        Properties props = System.getProperties()
        props.putAll(config.props)

        // build our message
        Session session = Session.getInstance(props, null)
        Message message = new MimeMessage(session)
        if (config.from) {
            message.setFrom(new InternetAddress(config.from))
        } else if (config.default.from){
            message.setFrom(new InternetAddress(config.default.from))
        } else {
            message.setFrom()
        }
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(config.to, false))
        if (config.cc) message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(config.cc, false))
        if (config.bcc) message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(config.bcc, false))
        message.subject = args.subject ?: (config.default.subject ?: '')

        // sets the message content
        MimeMultipart content = new MimeMultipart()
        if (config.text) {
            // handle text
            MimeBodyPart textPart = new MimeBodyPart()
            textPart.setContent(config.text.toString(), 'text/plain')
            content.addBodyPart(textPart)
        }
        if (config.html) {
            // handle html
            MimeBodyPart htmlPart = new MimeBodyPart()
            htmlPart.setContent(config.html.toString(), 'text/html')
            content.addBodyPart(htmlPart)
        }
        if (config.attachments) {
            // add all attachments
            config.attachments.each { a ->
                MimeBodyPart attachmentPart = new MimeBodyPart()
                FileDataSource src = new FileDataSource(a)
                attachmentPart.dataHandler = new DataHandler(src)
                attachmentPart.fileName = new File(a).name
                content.addBodyPart(attachmentPart)
            }
        }
        message.content = content

        message.setHeader("X-Mailer", config.mailer ?: "Griffon Mail Service")
        message.sentDate = new Date()

        // send the mail
        def t = session.getTransport(transport)
        if (config.props['mail.' + transport + '.auth']) {
            t.connect(config.mailhost, config.username, config.password)
        } else {
            t.connect()
        }
        t.sendMessage(message, message.getAllRecipients())
    }
}
