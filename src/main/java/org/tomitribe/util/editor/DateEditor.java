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
package org.tomitribe.util.editor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateEditor extends AbstractConverter {

    private List<DateFormat> formats = new ArrayList<DateFormat>();

    protected DateEditor(final List<DateFormat> formats) {
        this.formats = formats;
    }

    public DateEditor() {

        formats.add(DateFormat.getInstance());
        formats.add(DateFormat.getDateInstance());
        formats.add(new SimpleDateFormat("yyyy-MM-dd")); // Atom (ISO 8601))) -- short version;
        formats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz")); // Atom (ISO 8601)));
    }

    /**
     * Convert the text value of the property into a Date object instance.
     *
     * @return a Date object constructed from the property text value.
     * @throws org.tomitribe.util.editor.PropertyEditorException Unable to parse the string value into a Date.
     */
    protected Object toObjectImpl(final String text) {
        for (final DateFormat format : formats) {
            try {
                return format.parse(text);
            } catch (final ParseException e) {
                // no-op
            }
        }

        try {
            return complexParse(text);
        } catch (final ParseException e) {
            // any format errors show up as a ParseException, which we turn into
            // a PropertyEditorException.
            throw new PropertyEditorException(e);
        }
    }

    private Object complexParse(String text) throws ParseException {
        // find out whether the first token is a locale id and style in that
        // order
        // if there's locale, style is mandatory
        Locale locale = Locale.getDefault();
        int style = DateFormat.MEDIUM;
        final int firstSpaceIndex = text.indexOf(' ');
        if (firstSpaceIndex != -1) {
            String token = text.substring(0, firstSpaceIndex).intern();
            if (token.startsWith("locale")) {
                final String localeStr = token.substring(token.indexOf('=') + 1);
                final int underscoreIndex = localeStr.indexOf('_');
                if (underscoreIndex != -1) {
                    final String language = localeStr.substring(0, underscoreIndex);
                    final String country = localeStr.substring(underscoreIndex + 1);
                    locale = new Locale(language, country);
                } else {
                    locale = new Locale(localeStr);
                }
                // locale is followed by mandatory style
                final int nextSpaceIndex = text.indexOf(' ', firstSpaceIndex + 1);
                token = text.substring(firstSpaceIndex + 1, nextSpaceIndex);
                final String styleStr = token.substring(token.indexOf('=') + 1);
                if ("SHORT".equalsIgnoreCase(styleStr)) {
                    style = DateFormat.SHORT;
                } else if ("MEDIUM".equalsIgnoreCase(styleStr)) {
                    style = DateFormat.MEDIUM;
                } else if ("LONG".equalsIgnoreCase(styleStr)) {
                    style = DateFormat.LONG;
                } else if ("FULL".equalsIgnoreCase(styleStr)) {
                    style = DateFormat.FULL;
                } else {
                    // unknown style name
                    // throw exception or assume default?
                    style = DateFormat.MEDIUM;
                }
                text = text.substring(nextSpaceIndex + 1);
            }
        }
        final DateFormat formats = DateFormat.getDateInstance(style, locale);
        return formats.parse(text);
    }

    protected String toStringImpl(final Object value) {
        final Date date = (Date) value;
        final String text = formats.get(0).format(date);
        return text;
    }
}
