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
package org.axonframework.ext.eventstore.chronicle;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptTailer;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.serializer.Serializer;
import org.axonframework.serializer.SimpleSerializedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ChronicleDomainEventReader<T> implements DomainEventStream {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronicleDomainEventReader.class);

    private final Serializer m_serializer;
    private ExcerptTailer m_excerpt;

    /**
     * c-tor
     *
     * @param chronicle   the Chronicle
     * @param serializer  the DomainEventStream serializer
     */
    public ChronicleDomainEventReader(Chronicle chronicle, Serializer serializer) {
        m_serializer = serializer;

        try {
            m_excerpt = chronicle.createTailer();
        } catch (Exception e) {
            m_excerpt = null;
            LOGGER.warn("CreateTailer - Exception",e);
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public boolean hasNext() {
        return m_excerpt != null ? m_excerpt.nextIndex() : false;
    }

    @Override
    public DomainEventMessage<T> next() {
        return m_excerpt != null ? eventAt(m_excerpt.index()) : null;
    }

    @Override
    public DomainEventMessage<T> peek() {
        return m_excerpt != null ? eventAt(m_excerpt.index()) : null;
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * TODO: check de-serialization
     *
     * @param index the Chronicle index to read
     * @return the event at the given index
     */
    private DomainEventMessage<T> eventAt(long index) {
        DomainEventMessage<T> dem = null;

        if(m_excerpt.index(index)) {
            int len = m_excerpt.readInt();

            byte[] buffer = new byte[len];
            m_excerpt.read(buffer);

            //TODO: check revision
            dem = m_serializer.deserialize(
                new SimpleSerializedObject<>(
                    buffer,
                    byte[].class,
                    DomainEventMessage.class.getName(),
                    StringUtils.EMPTY)
            );
        }

        return dem;
    }
}