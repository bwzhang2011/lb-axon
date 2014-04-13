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
package org.axonframework.ext.hazelcast.samples.executor;

import com.hazelcast.core.IMap;
import org.axonframework.ext.hazelcast.HzConstants;
import org.axonframework.ext.hazelcast.HzProxy;
import org.axonframework.ext.hazelcast.samples.model.DataItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AxonProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AxonProcessor.class);

    // *************************************************************************
    //
    // *************************************************************************

    public static void main(String[] args) {
        System.setProperty("hazelcast.logging.type","slf4j");
        System.setProperty("hazelcast.jmx","false");

        ConfigurableApplicationContext context = null;
        AxonService engine = null;
        HzProxy proxy = null;

        try {
            context = new ClassPathXmlApplicationContext("axon-processor.xml");

            proxy = context.getBean("axon-hz-proxy",HzProxy.class);

            engine = context.getBean("axon-service",AxonService.class);
            engine.addAggregateType(DataItem.class);

            try {
                for(int i=0;i<10000;i++) {
                    LOGGER.debug("Sleep");

                    IMap<String,String> map = proxy.getMap(HzConstants.REG_AGGREGATES);
                    for(String key : map.localKeySet()) {
                        LOGGER.debug("Local Key : {}",key);
                    }

                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
                LOGGER.warn("InterruptedException", e);
            }

        } catch(Exception e) {
            LOGGER.warn("Exception",e);
        } finally {
            context.close();
        }
    }
}
