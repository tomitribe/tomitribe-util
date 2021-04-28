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
import java.util.function.Function;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


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

    @Test
    public void parametersSpecifiedByParent() throws Exception {

        class URIConsumer implements Consumer<URI> {
            @Override
            public void accept(URI uri) {
            }
        }

        class SpecializedConsumer extends URIConsumer {
        }

        final Type[] interfaceTypes = Generics.getInterfaceTypes(Consumer.class, SpecializedConsumer.class);

        // Consumer has only one parameter, so we are expecting one type
        assertEquals(1, interfaceTypes.length);

        // The type we're expecting is URI
        assertEquals(URI.class, interfaceTypes[0]);
    }

    /**
     * Scenario: our parent class implemented the generic interface and did
     * not specify the actual type either.  The actual type is declared
     * by the subclass.
     */
    @Test
    public void parametersDeferredByParent() {

        class URIConsumer<T> implements Consumer<T> {
            @Override
            public void accept(T uri) {
            }
        }

        class SpecializedConsumer extends URIConsumer<URI> {
        }

        final Type[] interfaceTypes = Generics.getInterfaceTypes(Consumer.class, SpecializedConsumer.class);

        // Consumer has only one parameter, so we are expecting one type
        assertEquals(1, interfaceTypes.length);

        // The type we're expecting is URI
        assertEquals(URI.class, interfaceTypes[0]);
    }

    /**
     * Scenario: our parent class implemented the generic interface and did
     * not specify the actual type either.  The actual type is declared
     * by the subclass.
     */
    @Test
    public void parametersDeferredByParentOfParent() {

        class URIConsumer<T> implements Consumer<T> {
            @Override
            public void accept(T uri) {
            }
        }

        class SpecializedConsumer<V> extends URIConsumer<V> {
        }

        class VerySpecializedConsumer extends SpecializedConsumer<URI> {
        }

        final Type[] interfaceTypes = Generics.getInterfaceTypes(Consumer.class, VerySpecializedConsumer.class);

        // Consumer has only one parameter, so we are expecting one type
        assertEquals(1, interfaceTypes.length);

        // The type we're expecting is URI
        assertEquals(URI.class, interfaceTypes[0]);
    }

    /**
     * The interface we are after is coming to us from another
     * interface we implement.  Let's ensure we can resolve it.
     */
    @Test
    public void interfaceInheritance() {

        class URIConsumer implements ImprovedConsumer<URI> {
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
    interface ImprovedConsumer<T> extends Consumer<T>{}


    /**
     * Our parent has a type variable that maps to
     * a type variable of one of its interfaces that
     * itself maps to an interface
     */
    @Test
    public void interfaceInheritanceVariable() {

        class URIConsumer<R> implements ImprovedConsumer<R> {
            @Override
            public void accept(R uri) {

            }
        }

        class SpecializedConsumer extends URIConsumer<URI> {
        }

        final Type[] interfaceTypes = Generics.getInterfaceTypes(Consumer.class, SpecializedConsumer.class);

        // Consumer has only one parameter, so we are expecting one type
        assertEquals(1, interfaceTypes.length);

        // The type we're expecting is URI
        assertEquals(URI.class, interfaceTypes[0]);
    }



    /**
     * If the specified class does not implement the interface, null will
     * be returned
     */
    @Test
    public void interfaceNotImplemented() throws Exception {

        class URIConsumer implements Consumer<URI> {
            @Override
            public void accept(URI uri) {
            }
        }

        final Type[] interfaceTypes = Generics.getInterfaceTypes(Function.class, URIConsumer.class);

        // Consumer has only one parameter, so we are expecting one type
        assertNull(interfaceTypes);
    }
}
