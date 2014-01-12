/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.util;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class StringsTest extends TestCase {

    public void testLc() throws Exception {
        assertEquals("orange", Strings.lc("OrAngE"));
    }

    public void testLowercase() throws Exception {
        assertEquals("orange", Strings.lowercase("oRANge"));
    }

    public void testUc() throws Exception {
        assertEquals("ORANGE", Strings.uc("oRANge"));
    }

    public void testUppercase() throws Exception {
        assertEquals("ORANGE", Strings.uppercase("OrAngE"));
    }

    public void testUcfirst() throws Exception {
        assertEquals("Orange", Strings.ucfirst("orange"));
    }

    public void testLcfirst() throws Exception {
        assertEquals("oRANGE", Strings.lcfirst("ORANGE"));
    }

    public void testCamelCase() throws Exception {
        assertEquals("Red", Strings.camelCase("red"));
        assertEquals("RedGreen", Strings.camelCase("red-green"));
        assertEquals("RedGreenBlue", Strings.camelCase("red-green-blue"));
    }

    public void testCamelCase1() throws Exception {
        assertEquals("Red", Strings.camelCase("red", "_"));
        assertEquals("RedGreen", Strings.camelCase("red_green", "_"));
        assertEquals("RedGreenBlue", Strings.camelCase("red_green_blue", "_"));

        assertEquals("Red", Strings.camelCase("red", "[_-]"));
        assertEquals("RedGreen", Strings.camelCase("red_green", "[_-]"));
        assertEquals("RedGreenBlue", Strings.camelCase("red_green-blue", "[_-]"));
    }
}
