/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.util;

/**
 * @version $Revision$ $Date$
 */
/**
 * @version $Rev$ $Date$
 */
public class Strings {


    public static String lc(String string){
        return lowercase(string);
    }

    public static String lowercase(String string) {
        if (string == null) return null;

        StringBuilder sb = new StringBuilder(string);
        for (int i = 0; i < sb.length(); i++) {
              sb.setCharAt(i, Character.toLowerCase(sb.charAt(i)));
        }
        return sb.toString();
    }

    public static String uc(String string){
        return uppercase(string);
    }

    public static String uppercase(String string) {
        if (string == null) return null;

        StringBuilder sb = new StringBuilder(string);
        for (int i = 0; i < sb.length(); i++) {
              sb.setCharAt(i, Character.toUpperCase(sb.charAt(i)));
        }
        return sb.toString();
    }

    public static String ucfirst(String string){
        if (string == null) return null;

        StringBuilder sb = new StringBuilder(string);
        if (sb.length() > 0){
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }
        return sb.toString();
    }

    public static String lcfirst(String string){
        if (string == null) return null;

        StringBuilder sb = new StringBuilder(string);
        if (sb.length() > 0){
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        }
        return sb.toString();
    }

    public static String camelCase(String string) {
        return camelCase(string, "-");
    }

    public static String camelCase(final String string, final String delimiter){
        final StringBuilder sb = new StringBuilder();
        final String[] strings = string.split(delimiter);

        for (String s : strings) {
            int l = sb.length();
            sb.append(s);
            sb.setCharAt(l, Character.toUpperCase(sb.charAt(l)));
        }
        return sb.toString();
    }
}