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
package org.axonframework.hazelcast.distributed;

import com.hazelcast.core.IQueue;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.hazelcast.distributed.msg.HzCommand;
import org.axonframework.hazelcast.distributed.msg.HzCommandAck;
import org.axonframework.hazelcast.distributed.msg.HzCommandReply;
import org.axonframework.hazelcast.distributed.msg.HzMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class HzCommandListener extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(HzCommandListener.class);

    private final CommandBus m_segment;
    private final IQueue<HzMessage> m_queue;
    private final AtomicBoolean m_running;
    private final HzCommandBusAgent m_agent;

    /**
     * c-tor
     *
     * @param segment
     * @param queue
     */
    public HzCommandListener(HzCommandBusAgent agent, CommandBus segment, IQueue<HzMessage> queue) {
        m_segment = segment;
        m_queue   = queue;
        m_running = new AtomicBoolean(true);
        m_agent   = agent;
    }

    /**
     *
     */
    public void shutdown() {
        m_running.set(false);

        try {
            this.join(1000 * 5);
        } catch (InterruptedException e) {
            LOGGER.warn("Exception",e);
        }
    }

    @Override
    public void run() {
        while(m_running.get()) {
            try {
                LOGGER.debug("poll...");
                HzMessage msg = m_queue.poll(1, TimeUnit.SECONDS);
                if(msg != null) {
                    LOGGER.debug(".. got a message of type {}",msg.getClass().getName());
                    if(msg instanceof HzCommand) {
                        onHazelcastCommand((HzCommand)msg);
                    } else if(msg instanceof HzCommandReply) {
                        onHazelcastCommandReply((HzCommandReply) msg);
                    } else if(msg instanceof HzCommandAck) {
                        onHazelcastCommandReply((HzCommandAck)msg);
                    }
                }

            } catch (InterruptedException e) {
                LOGGER.warn("Exception",e);
            }
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     * @param msg
     */
    private void onHazelcastCommand(HzCommand msg) {
        if(m_segment != null) {
            HzCommand cmd = (HzCommand)msg;
            m_segment.dispatch(cmd.getMessage());
        }
    }

    /**
     *
     * @param msg
     */
    private void onHazelcastCommandReply(HzCommandReply msg) {
        HzCommandReply rpl = (HzCommandReply)msg;
        CommandCallback       cbk = m_agent.getCallback(rpl.getCommandId());
        if(cbk != null) {
            if(rpl.isSuccess()) {
                cbk.onSuccess(rpl.getReturnValue());
            } else {
                cbk.onFailure(rpl.getError());
            }
        }
    }

    /**
     *
     * @param msg
     */
    private void onHazelcastCommandReply(HzCommandAck msg) {
    }
}