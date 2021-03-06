/*
 * Copyright (c) 2010-2014. Axon Framework
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
package org.axonframework.ext.chronicle.store;

import org.apache.commons.lang3.CharEncoding;
import org.axonframework.domain.DomainEventMessage;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 *
 */
public class ChronicleEventStoreUtil {

    /**
     *
     * @param type        the type
     * @param identifier  the identifier
     * @return            the id
     */
    public static String getStorageIdentifier(String type,String identifier) {
        return String.format("%s-%s",
            type,
            safeIdentifier(identifier));
    }

    /**
     *
     * @param type    the type
     * @param message the message
     * @return        the id
     */
    public static String getStorageIdentifier(String type, DomainEventMessage message) {
        return getStorageIdentifier(type, message.getAggregateIdentifier().toString());
    }

    /**
     *
     * @param id the id
     * @return   the id
     */
    public static String safeIdentifier(String id) {
        try {
            return URLEncoder.encode(id, CharEncoding.UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("System doesnt support UTF-8?", e);
        }
    }

    /**
     * Delete IndexedChronicle data at JVM exit
     *
     * @param path the path
     */
    public static void deleteOnExit(String path) {
        new File(path + ".data" ).deleteOnExit();
        new File(path + ".index").deleteOnExit();
    }
}
