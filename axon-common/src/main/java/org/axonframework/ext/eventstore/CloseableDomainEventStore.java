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
package org.axonframework.ext.eventstore;

import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;

import java.io.Closeable;

/**
 * @author lburgazzoli
 */
public interface CloseableDomainEventStore<T> extends Closeable {

    /**
     *
     */
    public void clear();

    /**
     *
     * @return the aggregate type
     */
    public String getAggregateType();

    /**
     *
     * @return the aggregate id
     */
    public String getAggregateId();

    /**
     *
     * @return the storage id
     */
    public String getStorageId();

    /**
     *
     * @return the number of items stored
     */
    public long getStorageSize();

    /**
     *
     * @param message
     */
    public void add(DomainEventMessage<T> message);

    /**
     *
     * @return the event stream
     */
    public DomainEventStream getEventStream();
}
