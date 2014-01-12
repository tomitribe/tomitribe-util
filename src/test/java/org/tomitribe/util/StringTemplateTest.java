/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.util;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @version $Revision$ $Date$
 */
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
