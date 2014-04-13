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
package org.axonframework.ext.hazelcast.samples.executor;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.axonframework.cache.Cache;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.annotation.AggregateAnnotationCommandHandler;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.domain.AggregateRoot;
import org.axonframework.eventhandling.ClusteringEventBus;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventListener;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventhandling.annotation.AnnotationEventListenerAdapter;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.ext.cache.GuavaCache;
import org.axonframework.ext.hazelcast.HzConstants;
import org.axonframework.ext.hazelcast.HzProxy;
import org.axonframework.ext.hazelcast.distributed.commandbus.HzCommand;
import org.axonframework.ext.hazelcast.distributed.commandbus.HzCommandReply;
import org.axonframework.ext.hazelcast.distributed.commandbus.executor.HzCommandBusConnector;
import org.axonframework.ext.hazelcast.distributed.commandbus.executor.HzTaskDispatcher;
import org.axonframework.ext.hazelcast.eventhandling.HzEventBusTerminal;
import org.axonframework.ext.hazelcast.eventhandling.IHzTopicPublisher;
import org.axonframework.ext.hazelcast.eventhandling.IHzTopicSubscriber;
import org.axonframework.ext.hazelcast.store.HzEventStore;
import org.axonframework.ext.repository.CachingEventSourcingRepositoryFactory;
import org.axonframework.ext.repository.IRepositoryFactory;
import org.axonframework.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

//TODO: review generics
@SuppressWarnings("unchecked")
public class AxonService implements HzTaskDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(AxonService.class);

    private final String m_nodeName;
    private final HazelcastInstance m_hzInstance;
    private HzEventBusTerminal m_evtBusTer;
    private HzCommandBusConnector m_connector;
    private IRepositoryFactory m_repositoryFactory;
    private HzEventStore m_evtStore;
    private EventBus m_evtBus;
    private CommandBus m_cmdBus;
    private CommandGateway m_cmdGw;
    private final Cache m_cache;

    private final Set<EventListener> m_eventListeners;
    private final Map<Object,EventListener> m_eventHandlers;
    private final Map<Class<? extends AggregateRoot>,AggregateSubscription<? extends AggregateRoot>> m_aggregates;

    public AxonService(final String nodeName, final Config config) {
        this(nodeName,Hazelcast.newHazelcastInstance(config));
    }

    public AxonService(final String nodeName, final HazelcastInstance hz) {
        this(nodeName,new HzProxy(hz));
    }

    public AxonService(final String nodeName, final HzProxy proxy) {
        m_hzInstance = proxy;
        m_nodeName     = nodeName;
        m_evtBusTer    = null;
        m_connector    = null;
        m_cmdBus       = null;
        m_cmdGw        = null;
        m_evtStore     = null;
        m_evtBus       = null;

        m_cache          = new GuavaCache(m_nodeName);
        m_eventListeners = Sets.newHashSet();
        m_eventHandlers  = Maps.newConcurrentMap();
        m_aggregates     = Maps.newConcurrentMap();
    }

    // *************************************************************************
    //
    // *************************************************************************

    public void init() {
        createEventBus();
        cerateEventStore();
        createCommandBus();
        createCommandGateway();

        m_hzInstance.getUserContext().put(HzConstants.USER_CONTEXT_NAME,this);
    }

    public void destroy() {
        LOGGER.debug("Cleanup - EventListeners ({})",m_eventListeners.size());
        for(EventListener listener : m_eventListeners) {
            m_evtBus.unsubscribe(listener);
        }

        m_eventListeners.clear();

        LOGGER.debug("Cleanup - EventHandlers ({})",m_eventHandlers.size());
        for(EventListener listener : m_eventHandlers.values()) {
            m_evtBus.unsubscribe(listener);
        }

        m_eventHandlers.clear();

        LOGGER.debug("Cleanup - AggregateSubscription ({})",m_aggregates.size());
        for(AggregateSubscription<?> subscription : m_aggregates.values()) {
            for (String supportedCommand : subscription.handler.supportedCommands()) {
                m_cmdBus.unsubscribe(supportedCommand, subscription.handler);
            }
        }

        m_aggregates.clear();

        LOGGER.debug("Cleanup - CommandBusConnector");
        if(m_connector != null) {
            m_connector.close();
        }

        LOGGER.debug("Cleanup - EventStore");
        if(m_evtStore != null) {
            try {
                m_evtStore.close();
            } catch(IOException e) {
                LOGGER.warn("EventStore - IOException",e);
            }
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public void send(Object command) {
        m_cmdGw.send(command);
    }

    public <R> void send(Object command, CommandCallback<R> callback) {
        m_cmdGw.send(command,callback);
    }

    public <R> R sendAndWait(Object command) {
        return m_cmdGw.sendAndWait(command);
    }

    public <R> R sendAndWait(Object command, long timeout, TimeUnit unit) {
        return m_cmdGw.sendAndWait(command,timeout,unit);
    }

    public Future<HzCommandReply> dispatch(final HzCommand command) {
        return m_connector.dispatch(command);
    }

    // *************************************************************************
    //
    // *************************************************************************

    public void addEventHandler(Object eventHandler) {
        if(!m_eventHandlers.containsKey(eventHandler)) {
            EventListener eventListener = new AnnotationEventListenerAdapter(eventHandler);
            m_evtBus.subscribe(eventListener);

            m_eventHandlers.put(eventHandler,eventListener);
        }
    }

    public void removeEventHandler(Object eventHandler) {
        if(m_eventHandlers.containsKey(eventHandler)) {
            m_evtBus.unsubscribe(m_eventHandlers.get(eventHandler));
            m_eventHandlers.remove(eventHandler);
        }
    }

    public void addEventListener(EventListener eventListener) {
        if(m_eventListeners.add(eventListener)) {
            m_evtBus.subscribe(eventListener);
        }
    }

    public void removeEventListener(EventListener eventListener) {
        if(eventListener != null) {
            m_evtBus.unsubscribe(eventListener);
        }
    }

    public <T extends EventSourcedAggregateRoot> void addAggregateType(Class<T> aggregateType) {
        removeAggregateType(aggregateType);

        m_aggregates.put(
            aggregateType,
            createAggregateSubscription(
                m_repositoryFactory.createRepository(aggregateType),
                aggregateType)
        );
    }

    public void removeAggregateType(Class<? extends EventSourcedAggregateRoot> aggregateType) {
        if(m_aggregates.containsKey(aggregateType)) {
            AggregateSubscription<?> subscription = m_aggregates.get(aggregateType);
            for (String supportedCommand : subscription.handler.supportedCommands()) {
                m_cmdBus.subscribe(supportedCommand, subscription.handler);
            }

            m_aggregates.remove(aggregateType);
        }
    }

    // *************************************************************************
    // Getters/Setters
    // *************************************************************************

    public void setPublisher(IHzTopicPublisher publisher) {
        createEventBusTerminal().setPublisher(publisher);
    }

    public void setSubscriber(IHzTopicSubscriber subscriber) {
        createEventBusTerminal().setSubscriber(subscriber);
    }

    // *************************************************************************
    // Helpers
    // *************************************************************************

    private HzEventBusTerminal createEventBusTerminal() {
        if(m_evtBusTer == null) {
            m_evtBusTer = new HzEventBusTerminal(m_hzInstance);
        }

        return m_evtBusTer;
    }

    private CommandBus createCommandBus() {
        if(m_cmdBus == null && m_evtStore != null && m_evtBus != null) {
            // The EventSourcingRepository factory
            m_repositoryFactory = new CachingEventSourcingRepositoryFactory(
                m_cache,
                m_evtStore,
                m_evtBus);

            // The CommandBus connector
            // TODO: check
            m_connector = new HzCommandBusConnector(
                m_hzInstance,
                new SimpleCommandBus(),
                m_hzInstance.getName(),
                m_nodeName);

            m_connector.open();

            m_cmdBus = new DistributedCommandBus(m_connector);
        }

        return m_cmdBus;
    }

    private HzEventStore cerateEventStore() {
        if(m_evtStore == null) {
            m_evtStore = new HzEventStore(m_hzInstance);
        }

        return m_evtStore;
    }

    private EventBus createEventBus() {
        if(m_evtBus == null) {
            m_evtBus = (m_evtBusTer != null)
                ? new ClusteringEventBus(m_evtBusTer)
                : new SimpleEventBus();
        }

        return m_evtBus;
    }

    private CommandGateway createCommandGateway() {
        if(m_cmdGw == null && m_cmdBus != null) {
            m_cmdGw = new DefaultCommandGateway(m_cmdBus);
        }

        return m_cmdGw;
    }

    private <T extends EventSourcedAggregateRoot> AggregateSubscription<T> createAggregateSubscription(
        Repository<T> repo, Class<T> aggregateType) {
        return new AggregateSubscription(
            repo,
            AggregateAnnotationCommandHandler.subscribe(
                aggregateType,
                repo,
                m_cmdBus)
        );
    }

    // *************************************************************************
    //
    // *************************************************************************

    private final class AggregateSubscription<T extends AggregateRoot> {

        public final Repository<T> repository;
        public final AggregateAnnotationCommandHandler<T> handler;

        public AggregateSubscription(final Repository<T> repository,final AggregateAnnotationCommandHandler<T> handler) {
            this.repository = repository;
            this.handler    = handler;
        }
    }
}
