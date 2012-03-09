/*
 * Copyright 2009-2012 the original author or authors.
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
 * See the License for the specific language governing pemailssions and
 * limitations under the License.
 */

/**
 * @author Josh A. Reed
 * @author Andres Almiray
 */
class MailGriffonPlugin {
    // the plugin version
    String version = '0.4'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '0.9.5 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [:]
    // resources that are included in plugin packaging
    List pluginIncludes = []
    // the plugin license
    String license = 'Apache Software License 2.0'
    // Toolkit compatibility. No value means compatible with all
    // Valid values are: swing, javafx, swt, pivot, gtk
    List toolkits = []
    // Platform compatibility. No value means compatible with all
    // Valid values are:
    // linux, linux64, windows, windows64, macosx, macosx64, solaris
    List platforms = []
    // URL where documentation can be found
    String documentation = ''
    // URL where source can be found
    String source = 'https://github.com/griffon/griffon-mail-plugin'

    List authors = [
        [
            name: 'Josh Reed',
            email: 'jareed@andrill.org'
        ],
        [
            name: 'Andres Almiray',
            email: 'aalmiray@yahoo.com'
        ]
    ]
    String title = 'Send email from your Griffon app'

    String description = '''
The Mail plugin adds the ability to send email from your Griffon application.

Usage
-----
The plugin will inject the following dynamic methods:

* `sendMail(Map params)`

Where params may contain

| Property    | Type         | Required | Notes                                                                                                                                        |
| ----------- | ------------ | -------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| transport   | String       | no       | either 'smtp' for regular SMTP or 'smtps' for SMTP with SSL. Defaults to 'smtp'                                                              |
| mailhost    | String       | yes      | the address of the SMTP server, e.g. 'smtp.google.com'                                                                                       |
| port        | String       | no       | the port of the SMTP server. Defaults appropriately for the transport specified                                                              |
| auth        | boolean      | no       | true if authentication is required, false otherwise. Defaults to false                                                                       |
| user        | String       | no       | the username for authenticating with the SMTP server. Only used if auth=true                                                                 |
| password    | String       | no       | the password for authenticating with the SMTP server. Only used if auth=true                                                                 |
| from        | String       | no       | the message sender, e.g. 'foo@bar.com'                                                                                                       |
| to          | String       | yes      | the message recipient(s), e.g. 'foo@bar.com'. Multiple addresses may be specified as a comma-separated list, e.g. 'foo@bar.com, bar@bar.com' |
| cc          | String       | no       | the CC recipients(s), e.g. 'foo@bar.com'. Multiple addresses may be specified as a comma-separated list, e.g. 'foo@bar.com, bar@bar.com'     |
| bcc         | String       | no       | the BCC recipients(s), e.g. 'foo@bar.com'. Multiple addresses may be specified as a comma-separated list, e.g. 'foo@bar.com, bar@bar.com'    |
| subject     | String       | no       | the message subject                                                                                                                          |
| text        | String       | no       | the message content                                                                                                                          |
| html        | String       | no       | the message content in HTML                                                                                                                  |
| attachments | List&lt;String&gt; | no       | the list of file paths (as Strings) to attach to the email.                                                                                  |

These methods are also accessible to any component through the singleton `griffon.plugins.mail.MailEnhancer`.
You can inject these methods to non-artifacts via metaclasses. Simply grab hold of a particular metaclass and call
`MailEnhancer.enhance(metaClassInstance)`.

Configuration
-------------
### Dynamic method injection

Dynamic methods will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.mail.injectInto = ['controller', 'service']

### Examples

Sending a regular email:

        sendMail(mailhost: 'smtp.company.com',
            to: 'jareed@andrill.org',
            from: 'joeblow@company.com',
            subject: 'Test Mail',
            text:"""
                Hi Josh,
 
                This is a test email.
 
                Cheers,
                Joe Blow""".stripIndent(8))

Sending an email via Google's SMTP server:

        sendMail(transport: 'smtps',
            auth: true,
            mailhost: 'smtp.gmail.com',
            user: 'user@gmail.com',
            password: 'password',
            to: 'jareed@andrill.org',
            from: 'user@gmail.com',
            subject: 'Test Mail',
            text:"""
                Hi Josh,
 
                This is a test email sent via Gmail.
 
                Cheers,
                Joe Blow""".stripIndent(8))

Sending an HTML email with an attachment via Google's SMTP server:

        sendMail(
            transport: 'smtps',
            auth: true,
            mailhost: 'smtp.gmail.com',
            user: 'user@gmail.com',
            password: 'password',
            to: 'jareed@andrill.org',
            from: 'user@gmail.com',
            subject: 'HTML and Attachments',
            attachments: ['/Users/jareed/Desktop/Screenshot1.png'],
            html: new groovy.xml.StreamingMarkupBuilder().bind {
                html {
                    body {
                        p {
                            mkp.yield 'This is an HTML message with a '
                            strong 'bold'
                            mkp.yield ' element!'
                        }
                    }
                }
            }
        )

Notes
-----

As of version 0.2 this plugin supports plain text, HTML, attachments, or some combination thereof.

`sendMail` blocks until the mail is sent or until the request times out. You are responsible for making sure it is called
off of the EDT so it doesn't affect your application if the SMTP server is not available.

Testing
-------
Dynamic methods will not be automatically injected during unit testing, because addons are simply not initialized
for this kind of tests. However you can use `MailEnhancer.enhance(metaClassInstance, mailProviderInstance)` where 
`mailProviderInstance` is of type `griffon.plugins.mail.MailProvider`. The contract for this interface looks like this

    public interface MailProvider {
        void sendMail(Map params);
    }

It's up to you define how these methods need to be implemented for your tests. For example, here's an implementation that never
fails regardless of the arguments it receives

    class MyMailProvider implements MailProvider {
        void sendMail(Map params) { }
    }
    
This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            MailEnhancer.enhance(service.metaClass, new MyMailProvider())
            // exercise service methods
        }
    }
'''
}
