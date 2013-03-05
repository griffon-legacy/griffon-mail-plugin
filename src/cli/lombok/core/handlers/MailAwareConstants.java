/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lombok.core.handlers;

import lombok.core.BaseConstants;
import lombok.core.util.MethodDescriptor;

import static lombok.core.util.MethodDescriptor.args;
import static lombok.core.util.MethodDescriptor.type;

/**
 * @author Andres Almiray
 */
public interface MailAwareConstants extends BaseConstants {
    String MAIL_PROVIDER_TYPE = "griffon.plugins.mail.MailProvider";
    String DEFAULT_MAIL_PROVIDER_TYPE = "griffon.plugins.mail.DefaultMailProvider";
    String MAIL_CONTRIBUTION_HANDLER_TYPE = "griffon.plugins.mail.MailContributionHandler";
    String MAIL_PROVIDER_FIELD_NAME = "this$mailProvider";
    String METHOD_GET_MAIL_PROVIDER = "getMailProvider";
    String METHOD_SET_MAIL_PROVIDER = "setMailProvider";
    String METHOD_WITH_MAIL = "withMail";
    String PROVIDER = "provider";

    MethodDescriptor[] METHODS = new MethodDescriptor[]{
        MethodDescriptor.method(
            type(VOID),
            METHOD_WITH_MAIL,
            args(
                type(JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT))
        )
    };
}
