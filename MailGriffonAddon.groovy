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
 * See the License for the specific language governing pemailssions and
 * limitations under the License.
 */

import griffon.core.GriffonApplication
import griffon.core.GriffonClass
import griffon.plugins.mail.MailEnhancer
import griffon.plugins.mail.MailContributionHandler

/**
 * @author Josh A. Reed
 * @author Andres Almiray
 */
class MailGriffonAddon {
    void addonPostInit(GriffonApplication app) {
        def types = app.config.griffon?.mail?.injectInto ?: ['controller']
        for(String type : types) {
            for(GriffonClass gc : app.artifactManager.getClassesOfType(type)) {
                if (MailContributionHandler.isAssignableFrom(gc.clazz)) continue
                MailEnhancer.enhance(gc.metaClass)
            }
        }
    }
}