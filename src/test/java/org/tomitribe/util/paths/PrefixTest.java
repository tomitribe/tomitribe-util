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

package org.tomitribe.util.paths;

import org.junit.Test;
import org.tomitribe.util.Archive;
import org.tomitribe.util.Join;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class PrefixTest {

    private File dir() throws Exception {
        return new Archive()
                .add("repository/org.apache/tomcat/10.1.18/tomcat-10.1.18.jar", "")
                .add("repository/org.apache/tomcat/10.1.18/tomcat-10.1.18.pom", "")
                .add("repository/org.apache/tomcat/10.1.19/tomcat-10.1.19.jar", "")
                .add("repository/org.apache/tomcat/9.0.85/tomcat-9.0.85.jar", "")
                .add("repository/org.apache/tomcat/11.0.0/tomcat-11.0.0.jar", "")
                .add("repository/org.apache/tomcat/10.1.0-M1/tomcat-10.1.0-M1.jar", "")
                .add("repository/junit/junit/4.12/junit-4.12.jar", "")
                .toDir();
    }

    /**
     * Without @Walk only the immediate children are considered
     */
    @Test
    public void prefix() throws Exception {
        final File dir = dir();
        final Tomcat tomcat = Dir.of(Tomcat.class, dir.toPath().resolve("repository/org.apache/tomcat"));
        final List<Path> list = tomcat.version10().collect(Collectors.toList());

        assertEquals("" +
                "/repository/org.apache/tomcat/10.1.0-M1/\n" +
                "/repository/org.apache/tomcat/10.1.18/\n" +
                "/repository/org.apache/tomcat/10.1.19/", Join.join("\n", paths(dir, list)));
    }

    /**
     * The prefix is tested against the name only, not the full path
     */
    @Test
    public void prefixWalk() throws Exception {
        final File dir = dir();
        final Work work = Dir.of(Work.class, dir.toPath());
        final List<Path> list = work.tomcatFiles().collect(Collectors.toList());

        assertEquals("" +
                "/repository/org.apache/tomcat/10.1.0-M1/tomcat-10.1.0-M1.jar\n" +
                "/repository/org.apache/tomcat/10.1.18/tomcat-10.1.18.jar\n" +
                "/repository/org.apache/tomcat/10.1.18/tomcat-10.1.18.pom\n" +
                "/repository/org.apache/tomcat/10.1.19/tomcat-10.1.19.jar\n" +
                "/repository/org.apache/tomcat/11.0.0/tomcat-11.0.0.jar\n" +
                "/repository/org.apache/tomcat/9.0.85/tomcat-9.0.85.jar", Join.join("\n", paths(dir, list)));
    }

    @Test
    public void noMatches() throws Exception {
        final File dir = dir();
        final Work work = Dir.of(Work.class, dir.toPath());
        final List<Path> list = work.nothing().collect(Collectors.toList());

        assertEquals("", Join.join("\n", paths(dir, list)));
    }

    @Test
    public void prefixAndSuffix() throws Exception {
        final File dir = dir();
        final Work work = Dir.of(Work.class, dir.toPath());
        final List<Path> list = work.tomcatJars().collect(Collectors.toList());

        assertEquals("" +
                "/repository/org.apache/tomcat/10.1.0-M1/tomcat-10.1.0-M1.jar\n" +
                "/repository/org.apache/tomcat/10.1.18/tomcat-10.1.18.jar\n" +
                "/repository/org.apache/tomcat/10.1.19/tomcat-10.1.19.jar\n" +
                "/repository/org.apache/tomcat/11.0.0/tomcat-11.0.0.jar\n" +
                "/repository/org.apache/tomcat/9.0.85/tomcat-9.0.85.jar", Join.join("\n", paths(dir, list)));
    }

    @Test
    public void prefixAndMatch() throws Exception {
        final File dir = dir();
        final Work work = Dir.of(Work.class, dir.toPath());
        final List<Path> list = work.tomcat10Jars().collect(Collectors.toList());

        assertEquals("" +
                "/repository/org.apache/tomcat/10.1.0-M1/tomcat-10.1.0-M1.jar\n" +
                "/repository/org.apache/tomcat/10.1.18/tomcat-10.1.18.jar\n" +
                "/repository/org.apache/tomcat/10.1.19/tomcat-10.1.19.jar", Join.join("\n", paths(dir, list)));
    }

    /**
     * Multiple values are OR'd
     */
    @Test
    public void multipleValues() throws Exception {
        final File dir = dir();
        final Tomcat tomcat = Dir.of(Tomcat.class, dir.toPath().resolve("repository/org.apache/tomcat"));
        final List<Path> list = tomcat.modern().collect(Collectors.toList());

        assertEquals("" +
                "/repository/org.apache/tomcat/10.1.0-M1/\n" +
                "/repository/org.apache/tomcat/10.1.18/\n" +
                "/repository/org.apache/tomcat/10.1.19/\n" +
                "/repository/org.apache/tomcat/11.0.0/", Join.join("\n", paths(dir, list)));
    }

    /**
     * Repeated includes are OR'd, same as listing the values in one annotation
     */
    @Test
    public void stackedIncludes() throws Exception {
        final File dir = dir();
        final Tomcat tomcat = Dir.of(Tomcat.class, dir.toPath().resolve("repository/org.apache/tomcat"));
        final List<Path> list = tomcat.modernStacked().collect(Collectors.toList());

        assertEquals("" +
                "/repository/org.apache/tomcat/10.1.0-M1/\n" +
                "/repository/org.apache/tomcat/10.1.18/\n" +
                "/repository/org.apache/tomcat/10.1.19/\n" +
                "/repository/org.apache/tomcat/11.0.0/", Join.join("\n", paths(dir, list)));
    }

    /**
     * Repeated excludes are OR'd
     */
    @Test
    public void stackedExcludes() throws Exception {
        final File dir = dir();
        final Tomcat tomcat = Dir.of(Tomcat.class, dir.toPath().resolve("repository/org.apache/tomcat"));
        final List<Path> list = tomcat.legacy().collect(Collectors.toList());

        assertEquals("/repository/org.apache/tomcat/9.0.85/", Join.join("\n", paths(dir, list)));
    }

    @Test
    public void exclude() throws Exception {
        final File dir = dir();
        final Tomcat tomcat = Dir.of(Tomcat.class, dir.toPath().resolve("repository/org.apache/tomcat"));
        final List<Path> list = tomcat.notVersion10().collect(Collectors.toList());

        assertEquals("" +
                "/repository/org.apache/tomcat/11.0.0/\n" +
                "/repository/org.apache/tomcat/9.0.85/", Join.join("\n", paths(dir, list)));
    }

    @Test
    public void includeAndExclude() throws Exception {
        final File dir = dir();
        final Tomcat tomcat = Dir.of(Tomcat.class, dir.toPath().resolve("repository/org.apache/tomcat"));
        final List<Path> list = tomcat.version10Releases().collect(Collectors.toList());

        assertEquals("" +
                "/repository/org.apache/tomcat/10.1.18/\n" +
                "/repository/org.apache/tomcat/10.1.19/", Join.join("\n", paths(dir, list)));
    }

    @Test
    public void array() throws Exception {
        final File dir = dir();
        final Tomcat tomcat = Dir.of(Tomcat.class, dir.toPath().resolve("repository/org.apache/tomcat"));
        final List<Path> list = Arrays.asList(tomcat.version10Array());

        assertEquals("" +
                "/repository/org.apache/tomcat/10.1.0-M1/\n" +
                "/repository/org.apache/tomcat/10.1.18/\n" +
                "/repository/org.apache/tomcat/10.1.19/", Join.join("\n", paths(dir, list)));
    }

    public String path(final Path file) {
        if (Files.isDirectory(file)) return file.toAbsolutePath() + "/";
        else return file.toAbsolutePath().toString();
    }

    private List<String> paths(final File dir, final List<Path> list) {
        final int trim = path(dir.toPath()).length() - 1;
        return list.stream()
                .map(this::path)
                .map(s -> s.substring(trim))
                .sorted()
                .collect(Collectors.toList());
    }

    public interface Tomcat extends Dir {
        @Prefix("10.")
        Stream<Path> version10();

        @Prefix("10.")
        Path[] version10Array();

        @Prefix({"10.", "11."})
        Stream<Path> modern();

        @Prefix("10.")
        @Prefix("11.")
        Stream<Path> modernStacked();

        @Prefix(value = "10.", exclude = true)
        @Prefix(value = "11.", exclude = true)
        Stream<Path> legacy();

        @Prefix(value = "10.", exclude = true)
        Stream<Path> notVersion10();

        @Prefix("10.")
        @Prefix(value = "10.1.0-M", exclude = true)
        Stream<Path> version10Releases();
    }

    public interface Work extends Dir {
        @Walk
        @Prefix("tomcat-")
        Stream<Path> tomcatFiles();

        @Walk
        @Prefix("org.apache/tomcat")
        Stream<Path> nothing();

        @Walk
        @Prefix("tomcat-")
        @Suffix(".jar")
        Stream<Path> tomcatJars();

        @Walk
        @Prefix("tomcat-")
        @Match(".*-10\\..*\\.jar")
        Stream<Path> tomcat10Jars();
    }
}
