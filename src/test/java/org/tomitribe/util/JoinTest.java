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
package org.tomitribe.util;

import org.tomitribe.util.Join;
import junit.framework.TestCase;

import java.util.Arrays;

public class JoinTest extends TestCase {

    public void test1() throws Exception {

        final String actual = Join.join("&", Arrays.asList(123, "foo", true, new Message("bar")));
        final String expected = "123&foo&true&bar";
        assertEquals(expected, actual);
    }

    public void test2() throws Exception {

        final String actual = Join.join("*", 123, "foo", true, new Message("bar"));
        final String expected = "123*foo*true*bar";
        assertEquals(expected, actual);
    }

    public void test3() throws Exception {

        final String actual = Join.join("*", new Join.NameCallback() {
            @Override
            public String getName(Object object) {
                return "(" + object + ")";
            }
        }, 123, "foo", true, new Message("bar"));

        final String expected = "(123)*(foo)*(true)*(bar)";
        assertEquals(expected, actual);
    }

    public void test4() throws Exception {

        final String actual = Join.join("*", new Join.NameCallback() {
            @Override
            public String getName(Object object) {
                return "(" + object + ")";
            }
        }, Arrays.asList(123, "foo", true, new Message("bar")));

        final String expected = "(123)*(foo)*(true)*(bar)";
        assertEquals(expected, actual);
    }

    public static class Message {
        private final String message;

        public Message(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }
    }
}
