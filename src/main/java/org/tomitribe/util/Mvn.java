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

/**
 * Will resolve the location of already downloaded maven artifacts relative to this jar.
 *
 * Extremely simple utility class that assumes:
 *
 *  - This class is inside a jar that is inside a local maven repository
 *  - Maven has already downloaded the dependency you wish to locate on the filesystem
 */
public class Mvn {

    private Mvn() {
    }

    public static File mvn(final String coordinates) {
        final String[] parts = coordinates.split(":");

        // org.apache.tomee:apache-tomee:zip:plus:7.1.0
        if (parts.length == 5) {
            final String group = parts[0];
            final String artifact = parts[1];
            final String packaging = parts[2];
            final String classifier = parts[3];
            final String version = parts[4];
            return mvn(group, artifact, version, packaging, classifier);
        }

        // org.apache.tomee:tomee-util:jar:7.1.0
        if (parts.length == 4) {
            final String group = parts[0];
            final String artifact = parts[1];
            final String packaging = parts[2];
            final String version = parts[3];
            return mvn(group, artifact, version, packaging);
        }

        throw new IllegalArgumentException("Unsupported coordinates (GAV): " + coordinates);
    }

    public static File mvn(final String group, final String artifact, final String version, final String packaging) {
        final File repository = repository();

        // org/apache/tomee/tomee-util/7.1.0/tomee-util-7.1.0.jar
        final File archive = Files.file(
                repository,
                group.replace('.', '/'),
                artifact,
                version,
                String.format("%s-%s.%s", artifact, version, packaging));

        Files.exists(archive);
        Files.file(archive);
        Files.readable(archive);
        return archive;
    }

    public static File mvn(final String group, final String artifact, final String version, final String packaging, String classifier) {
        final File repository = repository();

        // org/apache/tomee/apache-tomee/7.1.0/apache-tomee-7.1.0-plus.zip
        final File archive = Files.file(
                repository,
                group.replace('.', '/'),
                artifact,
                version,
                String.format("%s-%s-%s.%s", artifact, version, classifier, packaging));

        Files.exists(archive);
        Files.file(archive);
        Files.readable(archive);
        return archive;
    }

    private static File repository() {
        File file = JarLocation.jarLocation(Mvn.class);

        while ((file = file.getParentFile()) != null) {
            if (file.getName().equals("org")) return file.getParentFile();
            if (file.getName().equals("repository")) return file;
        }

        throw new IllegalStateException("Unable to find maven.repo.local directory");
    }

}
