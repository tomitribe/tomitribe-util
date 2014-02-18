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
package com.tomitribe.util;

import com.tomitribe.util.StringTemplate;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class StringTemplateTest extends TestCase {

    public void testApply() throws Exception {
        final StringTemplate template = new StringTemplate("http://{host}:{port}/{path}");

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("host", "localhost");
        params.put("port", 90);
        params.put("path", new StringBuffer("one/two/three"));

        final String string = template.apply(params);

        assertEquals("http://localhost:90/one/two/three", string);
    }

    public void testKeys() throws Exception {
        final StringTemplate template = new StringTemplate("http://{host}:{port}/{path}");

        final Set<String> keys = template.keys();
        assertEquals(3, keys.size());

        final Iterator<String> iterator = keys.iterator();
        assertEquals("host", iterator.next());
        assertEquals("path", iterator.next());
        assertEquals("port", iterator.next());
    }

}
