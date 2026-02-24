/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tomitribe.util;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringTemplate {

    public static final Pattern PATTERN = Pattern.compile("(\\{)((\\.|\\w)+)(})");
    private final Supplier<String> template;
    private final Pattern pattern;

    public StringTemplate(final String template) {
        this(() -> template, PATTERN);
    }

    public StringTemplate(final String template, final String startDelimiter, final String endDelimiter) {
        this(() -> template, buildPattern(startDelimiter, endDelimiter));
    }

    private StringTemplate(final Supplier<String> template, final Pattern pattern) {
        this.template = template;
        this.pattern = pattern;
    }

    private static Pattern buildPattern(final String startDelimiter, final String endDelimiter) {
        return Pattern.compile(
                "(" + Pattern.quote(startDelimiter) + ")((\\.|\\w)+)(" + Pattern.quote(endDelimiter) + ")"
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public Applier applier() {
        return new Applier(this);
    }

    public String format(final Map<String, Object> map) {
        final Function<String, String> function = new Function<String, String>() {
            @Override
            public String apply(final String s) {
                final Object value = map.get(s);
                return value != null ? value.toString() : "";
            }
        };
        return apply(function);
    }

    public String apply(final Function<String, String> map) {
        final String string = template.get();
        final Matcher matcher = pattern.matcher(string);
        final StringBuffer buf = new StringBuffer();

        while (matcher.find()) {
            final String key = matcher.group(2);

            if (key == null) {
                throw new IllegalStateException("Key is null. Template '" + string + "'");
            }

            final String value;

            if (key.toLowerCase().endsWith(".lc")) {
                final String key1 = key.substring(0, key.length() - 3);
                value = map.apply(key1).toLowerCase();
            } else if (key.toLowerCase().endsWith(".uc")) {
                final String key1 = key.substring(0, key.length() - 3);
                value = map.apply(key1).toUpperCase();
            } else if (key.toLowerCase().endsWith(".cc")) {
                final String key1 = key.substring(0, key.length() - 3);
                value = Strings.camelCase(map.apply(key1));
            } else {
                value = map.apply(key);
            }

            if (value == null) {
                throw new IllegalStateException("Value is null for key '" + key + "'. Template '" + string + "'.");
            }

            matcher.appendReplacement(buf, value);
        }

        matcher.appendTail(buf);
        return buf.toString();
    }

    public String apply(final Map<String, Object> map) {
        final Matcher matcher = pattern.matcher(template.get());
        final StringBuffer buf = new StringBuffer();

        while (matcher.find()) {
            final String key = matcher.group(2);

            if (key == null) {
                throw new IllegalStateException("Key is null. Template '" + template.get() + "'");
            }

            String value = value(map, key);

            if (key.toLowerCase().endsWith(".lc")) {
                value = value(map, key.substring(0, key.length() - 3)).toLowerCase();
            } else if (key.toLowerCase().endsWith(".uc")) {
                value = value(map, key.substring(0, key.length() - 3)).toUpperCase();
            } else if (key.toLowerCase().endsWith(".cc")) {
                value = Strings.camelCase(value(map, key.substring(0, key.length() - 3)));
            }

            if (value == null) {
                throw new IllegalStateException("Value is null for key '" + key + "'. Template '" + template.get() + "'. " +
                        "Keys: " + Join.join(", ", map.keySet()));
            }
            matcher.appendReplacement(buf, value);
        }

        matcher.appendTail(buf);
        return buf.toString();
    }

    private String value(final Map<String, Object> map, final String key) {
        final Object o = map.get(key);
        if (o == null) throw new IllegalStateException("Missing entry " + key);

        return o.toString();
    }

    public Set<String> keys() {
        final Set<String> keys = new TreeSet<String>();
        final Matcher matcher = pattern.matcher(template.get());

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

    public static class Builder {
        private Supplier<String> template;
        private String startDelimiter = "{";
        private String endDelimiter = "}";

        public Builder template(final String template) {
            return template(() -> template);
        }

        public Builder template(final File file) {
            return template(() -> {
                try {
                    return IO.slurp(file);
                } catch (final IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }

        public Builder template(final URL url) {
            return template(() -> {
                try {
                    return IO.slurp(url);
                } catch (final IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }

        public Builder template(final Supplier<String> template) {
            this.template = template;
            return this;
        }

        public Builder delimiters(final String start, final String end) {
            this.startDelimiter = start;
            this.endDelimiter = end;
            return this;
        }

        public StringTemplate build() {
            return new StringTemplate(template, buildPattern(startDelimiter, endDelimiter));
        }
    }

    public static class Applier {
        private final StringTemplate stringTemplate;
        private final Map<String, Object> map = new LinkedHashMap<>();

        private Applier(final StringTemplate stringTemplate) {
            this.stringTemplate = stringTemplate;
        }

        public Applier set(final String key, final Object value) {
            this.map.put(key, value);
            return this;
        }

        public String apply() {
            return stringTemplate.apply(map);
        }
    }

}
