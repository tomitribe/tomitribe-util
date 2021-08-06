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

import java.util.Arrays;

public class Version implements Comparable<Version> {
    private final String version;
    private final int[] components;

    public Version(final String version, final int... components) {
        this.version = version;
        this.components = components;
    }

    public String getVersion() {
        return version;
    }

    public int[] getComponents() {
        return components;
    }

    @Override
    public int compareTo(final Version that) {
        for (int i = 0; i < components.length; i++) {
            if (that.components.length <= i) return 1;

            final int componentA = this.components[i];
            final int componentB = that.components[i];

            final int compare = Integer.compare(componentA, componentB);
            if (compare == 0) continue;
            return compare;
        }
        return 0;
    }

    public static Version parse(final String versionOutput) {
        if (versionOutput == null) {
            return null;
        }

        if (versionOutput.trim().length() == 0) {
            return new Version(versionOutput);
        }

        final String input = versionOutput
                .replaceAll("^[.+_ a-zA-Z-]+", "")  // remove split chars from the start
                .replaceAll("[.+_ a-zA-Z-]+$", ""); // remove split chars from the end

        final String[] split = input.split("[.+_ a-zA-Z-]+");
        final int[] components = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            components[i] = Integer.parseInt(split[i]);
        }
        return new Version(versionOutput, components);
    }

    public static String[] sort(final String[] versions) {
        return Arrays.stream(versions)
                .map(Version::parse)
                .sorted()
                .map(Version::getVersion)
                .toArray(String[]::new);
    }

    @Override
    public String toString() {
        return "Version{" +
                "version='" + version + '\'' +
                ", components=" + Arrays.toString(components) +
                '}';
    }
}
