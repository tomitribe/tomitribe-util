/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.util.reflect;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.function.Consumer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InerfaceGenericsTest {

    @Test
    public void testGetInterfaceParameter() throws Exception {

        class URIConsumer implements Consumer<URI> {
            @Override
            public void accept(URI uri) {
            }
        }

        final Type[] interfaceTypes = Generics.getInterfaceTypes(Consumer.class, URIConsumer.class);

        // Consumer has only one parameter, so we are expecting one type
        assertEquals(1, interfaceTypes.length);

        // The type we're expecting is URI
        assertEquals(URI.class, interfaceTypes[0]);
    }
}
