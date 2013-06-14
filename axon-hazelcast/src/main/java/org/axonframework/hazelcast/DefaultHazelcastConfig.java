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
package org.axonframework.hazelcast;

import com.hazelcast.config.Config;

/**
 *
 */
public class DefaultHazelcastConfig extends Config{

    /**
     * c-tor
     */
    public DefaultHazelcastConfig() {
        super.setProperty("hazelcast.logging.type","slf4j");
        super.getNetworkConfig().setPortAutoIncrement(false);
        super.getNetworkConfig().getInterfaces().setEnabled(false);
        super.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
        super.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        super.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
    }
}
