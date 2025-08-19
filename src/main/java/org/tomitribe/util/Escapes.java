/*
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements. See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership. The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License. You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied. See the License for the
  specific language governing permissions and limitations
  under the License.
 */
package org.tomitribe.util;

public class Escapes {

    private Escapes() {
    }

    /**
     * Credit to tchrist on StackOverflow
     *
     *  - https://stackoverflow.com/a/4298836/190816
     *  - https://stackoverflow.com/users/471272/tchrist
     *
     */
    // CHECKSTYLE:OFF
    public static String unescape(String oldstr) {

        final StringBuilder newString = new StringBuilder(oldstr.length());

        boolean sawBackslash = false;

        for (int i = 0; i < oldstr.length(); i++) {
            int cp = oldstr.codePointAt(i);
            if (oldstr.codePointAt(i) > Character.MAX_VALUE) i++;

            if (!sawBackslash) {
                if (cp == '\\') {
                    sawBackslash = true;
                } else {
                    newString.append(Character.toChars(cp));
                }
                continue; /* switch */
            }

            if (cp == '\\') {
                sawBackslash = false;
                newString.append('\\');
                newString.append('\\');
                continue; /* switch */
            }

            switch (cp) {

                case 'r':
                    newString.append('\r');
                    break; /* switch */

                case 'n':
                    newString.append('\n');
                    break; /* switch */

                case 'f':
                    newString.append('\f');
                    break; /* switch */

                /* PASS a \b THROUGH!! */
                case 'b':
                    newString.append("\\b");
                    break; /* switch */

                case 't':
                    newString.append('\t');
                    break; /* switch */

                case 'a':
                    newString.append('\007');
                    break; /* switch */

                case 'e':
                    newString.append('\033');
                    break; /* switch */

                /*
                 * A "control" character is what you get when you xor its
                 * codepoint with '@'==64.  This only makes sense for ASCII,
                 * and may not yield a "control" character after all.
                 *
                 * Strange but true: "\c{" is ";", "\c}" is "=", etc.
                 */
                case 'c': {
                    if (++i == oldstr.length()) {
                        throw new IllegalArgumentException("trailing \\c");
                    }
                    cp = oldstr.codePointAt(i);
                    /*
                     * don't need to grok surrogates, as next line blows them up
                     */
                    if (cp > 0x7f) {
                        throw new IllegalArgumentException("expected ASCII after \\c");
                    }
                    newString.append(Character.toChars(cp ^ 64));
                    break; /* switch */
                }

                case '8':
                case '9':
                    throw new IllegalArgumentException("illegal octal digit");
    /* NOTREACHED */

        /*
         * may be 0 to 2 octal digits following this one
         * so back up one for fallthrough to next case;
         * unread this digit and fall through to next case.
         */
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                    --i;
                          /* FALLTHROUGH */

                /*
                 * Can have 0, 1, or 2 octal digits following a 0
                 * this permits larger values than octal 377, up to
                 * octal 777.
                 */
                case '0': {
                    if (i + 1 == oldstr.length()) {
                        /* found \0 at end of string */
                        newString.append(Character.toChars(0));
                        break; /* switch */
                    }
                    i++;
                    int digits = 0;
                    int j;
                    for (j = 0; j <= 2; j++) {
                        if (i + j == oldstr.length()) {
                            break; /* for */
                        }
                        /* safe because will unread surrogate */
                        int ch = oldstr.charAt(i + j);
                        if (ch < '0' || ch > '7') {
                            break; /* for */
                        }
                        digits++;
                    }
                    if (digits == 0) {
                        --i;
                        newString.append('\0');
                        break; /* switch */
                    }
                    try {
                        int value = Integer.parseInt(oldstr.substring(i, i + digits), 8);
                        newString.append(Character.toChars(value));
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException("invalid octal value for \\0 escape");
                    }
                    i += digits - 1;
                    break; /* switch */
                } /* end case '0' */

                case 'x': {
                    if (i + 2 > oldstr.length()) throw new IllegalArgumentException("string too short for \\x escape");
                    i++;

                    boolean sawBrace = false;
                    if (oldstr.charAt(i) == '{') {
                            /* ^^^^^^ ok to ignore surrogates here */
                        i++;
                        sawBrace = true;
                    }
                    int j;
                    for (j = 0; j < 8; j++) {

                        if (!sawBrace && j == 2) break;  /* for */

                        /*
                         * ASCII test also catches surrogates
                         */
                        int ch = oldstr.charAt(i + j);
                        if (ch > 127) {
                            throw new IllegalArgumentException("illegal non-ASCII hex digit in \\x escape");
                        }

                        if (sawBrace && ch == '}')  break; /* for */

                        if (!((ch >= '0' && ch <= '9')
                                ||
                                (ch >= 'a' && ch <= 'f')
                                ||
                                (ch >= 'A' && ch <= 'F')
                        )
                                ) {
                            throw new IllegalArgumentException(String.format("illegal hex digit #%d '%c' in \\x", ch, ch));
                        }

                    }

                    if (j == 0) {
                        throw new IllegalArgumentException("empty braces in \\x{} escape");
                    }

                    int value = 0;
                    try {
                        value = Integer.parseInt(oldstr.substring(i, i + j), 16);
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException("invalid hex value for \\x escape");
                    }
                    newString.append(Character.toChars(value));
                    if (sawBrace) j++;
                    i += j - 1;
                    break; /* switch */
                }

                case 'u': {
                    if (i + 4 > oldstr.length()) {
                        throw new IllegalArgumentException("string too short for \\u escape");
                    }
                    i++;
                    int j;
                    for (j = 0; j < 4; j++) {
                        /* this also handles the surrogate issue */
                        if (oldstr.charAt(i + j) > 127) {
                            throw new IllegalArgumentException("illegal non-ASCII hex digit in \\u escape");
                        }
                    }
                    int value = 0;
                    try {
                        value = Integer.parseInt(oldstr.substring(i, i + j), 16);
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException("invalid hex value for \\u escape");
                    }
                    newString.append(Character.toChars(value));
                    i += j - 1;
                    break; /* switch */
                }

                case 'U': {
                    if (i + 8 > oldstr.length()) {
                        throw new IllegalArgumentException("string too short for \\U escape");
                    }
                    i++;
                    int j;
                    for (j = 0; j < 8; j++) {
                        /* this also handles the surrogate issue */
                        if (oldstr.charAt(i + j) > 127) {
                            throw new IllegalArgumentException("illegal non-ASCII hex digit in \\U escape");
                        }
                    }
                    int value = 0;
                    try {
                        value = Integer.parseInt(oldstr.substring(i, i + j), 16);
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException("invalid hex value for \\U escape");
                    }
                    newString.append(Character.toChars(value));
                    i += j - 1;
                    break; /* switch */
                }

                default:
                    newString.append('\\');
                    newString.append(Character.toChars(cp));
               /*
                * say(String.format(
                *       "DEFAULT unrecognized escape %c passed through",
                *       cp));
                */
                    break; /* switch */

            }
            sawBackslash = false;
        }

        /* weird to leave one at the end */
        if (sawBackslash) newString.append('\\');

        return newString.toString();
    }
    // CHECKSTYLE:ON

}
