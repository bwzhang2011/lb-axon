/*
 * Copyright (c) 2010-2013. Axon Framework
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
package org.axonframework.hazelcast.eventhandling.pub;

import com.hazelcast.core.ITopic;
import org.axonframework.domain.EventMessage;
import org.axonframework.hazelcast.IHzProxy;
import org.axonframework.hazelcast.eventhandling.IHzTopicPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class AbstractPublisher implements IHzTopicPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPublisher.class);

    /**
     * c-tor
     */
    public AbstractPublisher() {
    }

    /**
     *
     * @param event
     */
    public void publish(IHzProxy prox,EventMessage event) {
        String               topicName = resolve(event);
        ITopic<EventMessage> topic     = prox.getTopic(topicName);

        LOGGER.debug("Publish event <{}> to {}",event.getIdentifier(),topic.getName());
        topic.publish(event);
    }

    protected abstract String resolve(EventMessage event);
}
