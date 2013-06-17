package org.axonframework.hazelcast.eventhandling.impl;

import com.google.common.collect.Sets;
import com.hazelcast.core.ITopic;
import org.axonframework.domain.EventMessage;
import org.axonframework.hazelcast.IHazelcastInstanceProxy;
import org.axonframework.hazelcast.eventhandling.HazelcastEventBusTerminal;
import org.axonframework.hazelcast.eventhandling.IHazelcastTopicSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 *
 */
public class StaticSubscriber implements IHazelcastTopicSubscriber {
    private static final Logger LOGGER = LoggerFactory.getLogger(StaticSubscriber.class);

    private final Set<String> m_topicNames;

    /**
     *
     */
    public StaticSubscriber() {
        m_topicNames = Sets.newHashSet();
    }

    /**
     * @param topicNames
     */
    public StaticSubscriber(String... topicNames) {
        m_topicNames = Sets.newHashSet(topicNames);
    }

    /**
     * @param topicNames
     */
    public StaticSubscriber(List<String> topicNames) {
        m_topicNames = Sets.newHashSet(topicNames);
    }

    /**
     *
     * @param topics
     */
    public void setTopicNames(List<String> topics) {
        m_topicNames.clear();
        m_topicNames.addAll(topics);
    }

    @Override
    public void subscribe(IHazelcastInstanceProxy proxy,HazelcastEventBusTerminal terminal) {
        for(String topicName : m_topicNames) {
            LOGGER.debug("Subscribing to <{}>",topicName);
            ITopic<EventMessage> topic = proxy.getTopic(topicName);
            topic.addMessageListener(terminal);
        }
    }

    @Override
    public void unsubscribe(IHazelcastInstanceProxy proxy,HazelcastEventBusTerminal terminal) {
        for(String topicName : m_topicNames) {
            LOGGER.debug("Unsubscribing from <{}>",topicName);
            ITopic<EventMessage> topic = proxy.getTopic(topicName);
            topic.removeMessageListener(terminal);
        }
    }
}
