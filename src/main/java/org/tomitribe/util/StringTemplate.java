/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringTemplate {

    public static final Pattern PATTERN = Pattern.compile("(\\{)((\\.|\\w)+)(})");
    private final String template;

    public StringTemplate(String template) {
        this.template = template;
    }

    public String apply(Map<String, Object> map) {
        Matcher matcher = PATTERN.matcher(template);
        StringBuffer buf = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(2);

            if (key == null) throw new IllegalStateException("Key is null. Template '" + template + "'");

            String value = value(map, key);

            if (key.toLowerCase().endsWith(".lc")) {
                value = value(map, key.substring(0, key.length() - 3)).toLowerCase();
            } else if (key.toLowerCase().endsWith(".uc")) {
                value = value(map, key.substring(0, key.length() - 3)).toUpperCase();
            } else if (key.toLowerCase().endsWith(".cc")) {
                value = Strings.camelCase(value(map, key.substring(0, key.length() - 3)));
            }

            if (value == null) throw new IllegalStateException("Value is null for key '" + key + "'. Template '" + template + "'. Keys: " + Join.join(", ", map.keySet()));
            matcher.appendReplacement(buf, value);
        }

        matcher.appendTail(buf);
        return buf.toString();
    }

    private String value(Map<String, Object> map, String key) {
        final Object o = map.get(key);
        if (o == null) throw new IllegalStateException("Missing entry "+ key);
        return o.toString();
    }

    public Set<String> keys() {
        final Set<String> keys = new TreeSet<String>();
        final Matcher matcher = PATTERN.matcher(template);

        while (matcher.find()) {
            String key = matcher.group(2);

            final String op = key.toLowerCase();
            if (op.endsWith(".lc") || op.endsWith(".uc") || op.endsWith(".cc")) {
                key = key.substring(0, key.length() - 3);
            }
            keys.add(key);
        }

        return keys;
    }

}
