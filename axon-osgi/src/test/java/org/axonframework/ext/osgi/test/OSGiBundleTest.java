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
package org.axonframework.ext.osgi.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * @author lburgazzoli
 */
@RunWith(PaxExam.class)
public class OSGiBundleTest extends OSGiTestCommon {
    private static final Map<String,Integer> BUNDLES = new HashMap<String,Integer>() {{
        put("org.axonframework.axon-core"                    , Bundle.ACTIVE);
        put("org.axonframework.axon-distributed-commandbus"  , Bundle.ACTIVE);
        put("com.github.lburgazzoli-lb-axon-common"          , Bundle.ACTIVE);
        put("com.github.lburgazzoli-lb-axon-hazelcast"       , Bundle.ACTIVE);
    }};

    @Inject
    BundleContext context;

    // *************************************************************************
    // SET-UP
    // *************************************************************************

    @Configuration
    public Option[] config() {	
        return options(
            systemProperty("org.osgi.framework.storage.clean").value("true"),
            systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("WARN"),
            mavenBundleAsInProject("org.slf4j", "slf4j-api"),
            mavenBundleAsInProject("org.apache.logging.log4j","log4j-api"),
            mavenBundleAsInProject("org.apache.logging.log4j","log4j-core"),
            mavenBundleAsInProject("org.apache.logging.log4j","log4j-slf4j-impl"),
            mavenBundleAsInProject("org.apache.commons", "commons-lang3"),
            mavenBundleAsInProject("commons-collections", "commons-collections"),
            mavenBundleAsInProject("com.hazelcast", "hazelcast"),
            mavenBundleAsInProject("org.axonframework", "axon-core"),
            mavenBundleAsInProject("org.axonframework", "axon-distributed-commandbus"),
            mavenBundleAsInProject("com.google.guava","guava"),
            mavenBundleAsInProject("javax.cache", "cache-api"),
            mavenBundleAsInProject("joda-time", "joda-time"),
            mavenBundle("org.apache.geronimo.specs", "geronimo-servlet_3.0_spec"),
            axonBundle("axon-common"),
            axonBundle("axon-hazelcast"),
            junitBundles(),
            systemPackage("com.sun.script.javascript"),
            cleanCaches()
        );
    }

    // *************************************************************************
    // TESTS
    // *************************************************************************

    @Test
    public void checkInject() {
        assertNotNull(context);
    }

    @Test
    public void checkBundle() {
        assertNotNull(context);

        final Bundle[] bundles = context.getBundles();
        for(final Map.Entry<String, Integer> entry : BUNDLES.entrySet()) {
            final Bundle bundle = findBundle(entry.getKey(), bundles);
            assertNotNull("Bundle <" + entry.getKey() + "> does not exist", bundle);
        }
    }
}

