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
package org.axonframework.ext.hazelcast.eventhandling.sub;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.domain.EventMessage;
import org.axonframework.ext.hazelcast.eventhandling.HzEventBusTerminal;
import org.axonframework.ext.hazelcast.eventhandling.IHzTopicSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class StaticSubscriber implements IHzTopicSubscriber {
    private static final Logger LOGGER = LoggerFactory.getLogger(StaticSubscriber.class);

    private final Set<String> m_topicNames;
    private final Map<String,String> m_subKeys;

    /**
     *
     */
    public StaticSubscriber() {
        m_topicNames = Sets.newHashSet();
        m_subKeys    = Maps.newHashMap();
    }

    /**
     * @param topicNames the topic names
     */
    public StaticSubscriber(String... topicNames) {
        m_topicNames = Sets.newHashSet(topicNames);
        m_subKeys    = Maps.newHashMap();
    }

    /**
     * @param topicNames the topic names
     */
    public StaticSubscriber(List<String> topicNames) {
        m_topicNames = Sets.newHashSet(topicNames);
        m_subKeys    = Maps.newHashMap();
    }

    /**
     *
     * @param topics the topics
     */
    public void setTopicNames(List<String> topics) {
        m_topicNames.clear();
        m_topicNames.addAll(topics);
    }

    @Override
    public void subscribe(final HazelcastInstance hzInstance, final HzEventBusTerminal terminal) {
        for(String topicName : m_topicNames) {
            LOGGER.debug("Subscribing to <{}>",topicName);
            ITopic<EventMessage> topic = hzInstance.getTopic(topicName);
            m_subKeys.put(topicName, topic.addMessageListener(terminal));
        }
    }

    @Override
    public void unsubscribe(final HazelcastInstance hzInstance, final HzEventBusTerminal terminal) {
        for(String topicName : m_topicNames) {
            LOGGER.debug("Unsubscribing from <{}>",topicName);
            ITopic<EventMessage> topic = hzInstance.getTopic(topicName);

            String key = m_subKeys.remove(topicName);
            if(StringUtils.isNotEmpty(key)) {
                topic.removeMessageListener(key);
            }
        }
    }
}
