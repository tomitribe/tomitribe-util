/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.tomitribe.util.reflect;

import junit.framework.TestCase;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class GenericsTest extends TestCase {

    public void testGetFieldType() throws Exception {
        assertEquals(URI.class, Generics.getType(Orange.class.getField("uris")));
    }

    public void testGetMethodParameterType() throws Exception {
        final Parameter param = Reflection.params(Orange.class.getMethod("set", List.class)).iterator().next();
        assertEquals(Integer.class, Generics.getType(param));
    }

    public void testGetConstructorParameterType() throws Exception {
        final Parameter param = Reflection.params(Orange.class.getConstructor(Queue.class)).iterator().next();
        assertEquals(URI.class, Generics.getType(param));
    }

    public void testGetReturnType() throws Exception {
        assertEquals(URL.class, Generics.getReturnType(Orange.class.getMethod("urls")));
    }

    public static class Orange {

        public Collection<URI> uris;

        public Orange(Queue<URI> uris) {
            this.uris = uris;
        }

        public Set<URL> urls() {
            return null;
        }

        public void set(List<Integer> integers) {
        }
    }
}
