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

package org.tomitribe.util.dir;

import org.junit.Test;
import org.tomitribe.util.Archive;
import org.tomitribe.util.Join;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class SuffixTest {

    private File dir() throws Exception {
        return new Archive()
                .add("repository/org.apache/tomcat/10.1.18/tomcat-10.1.18.jar", "")
                .add("repository/org.apache/tomcat/10.1.18/tomcat-10.1.18-sources.jar", "")
                .add("repository/org.apache/tomcat/10.1.18/tomcat-10.1.18-javadoc.jar", "")
                .add("repository/org.apache/tomcat/10.1.18/tomcat-10.1.18.pom", "")
                .add("repository/junit/junit/4.12/junit-4.12.jar", "")
                .add("repository/junit/junit/4.12/junit-4.12.pom", "")
                .add("repository/junit/junit/4.12/junit-4.12.jar.sha1", "")
                .toDir();
    }

    @Test
    public void suffix() throws Exception {
        final File dir = dir();
        final Work work = Dir.of(Work.class, dir);
        final List<File> list = work.poms().collect(Collectors.toList());

        assertEquals("" +
                "/repository/junit/junit/4.12/junit-4.12.pom\n" +
                "/repository/org.apache/tomcat/10.1.18/tomcat-10.1.18.pom", Join.join("\n", paths(dir, list)));
    }

    /**
     * The entire name must end with the suffix. ".jar" does not match "junit-4.12.jar.sha1"
     */
    @Test
    public void suffixIsAnchored() throws Exception {
        final File dir = dir();
        final Work work = Dir.of(Work.class, dir);
        final List<File> list = work.jars().collect(Collectors.toList());

        assertEquals("" +
                "/repository/junit/junit/4.12/junit-4.12.jar\n" +
                "/repository/org.apache/tomcat/10.1.18/tomcat-10.1.18-javadoc.jar\n" +
                "/repository/org.apache/tomcat/10.1.18/tomcat-10.1.18-sources.jar\n" +
                "/repository/org.apache/tomcat/10.1.18/tomcat-10.1.18.jar", Join.join("\n", paths(dir, list)));
    }

    /**
     * Multiple values are OR'd
     */
    @Test
    public void multipleValues() throws Exception {
        final File dir = dir();
        final Work work = Dir.of(Work.class, dir);
        final List<File> list = work.jarsAndPoms().collect(Collectors.toList());

        assertEquals("" +
                "/repository/junit/junit/4.12/junit-4.12.jar\n" +
                "/repository/junit/junit/4.12/junit-4.12.pom\n" +
                "/repository/org.apache/tomcat/10.1.18/tomcat-10.1.18-javadoc.jar\n" +
                "/repository/org.apache/tomcat/10.1.18/tomcat-10.1.18-sources.jar\n" +
                "/repository/org.apache/tomcat/10.1.18/tomcat-10.1.18.jar\n" +
                "/repository/org.apache/tomcat/10.1.18/tomcat-10.1.18.pom", Join.join("\n", paths(dir, list)));
    }

    /**
     * Repeated includes are OR'd, same as listing the values in one annotation
     */
    @Test
    public void stackedIncludes() throws Exception {
        final File dir = dir();
        final Work work = Dir.of(Work.class, dir);
        final List<File> list = work.jarsAndPomsStacked().collect(Collectors.toList());

        assertEquals("" +
                "/repository/junit/junit/4.12/junit-4.12.jar\n" +
                "/repository/junit/junit/4.12/junit-4.12.pom\n" +
                "/repository/org.apache/tomcat/10.1.18/tomcat-10.1.18-javadoc.jar\n" +
                "/repository/org.apache/tomcat/10.1.18/tomcat-10.1.18-sources.jar\n" +
                "/repository/org.apache/tomcat/10.1.18/tomcat-10.1.18.jar\n" +
                "/repository/org.apache/tomcat/10.1.18/tomcat-10.1.18.pom", Join.join("\n", paths(dir, list)));
    }

    /**
     * Repeated excludes are OR'd
     */
    @Test
    public void stackedExcludes() throws Exception {
        final File dir = dir();
        final Work work = Dir.of(Work.class, dir);
        final List<File> list = work.binaryJarsStacked().collect(Collectors.toList());

        assertEquals("" +
                "/repository/junit/junit/4.12/junit-4.12.jar\n" +
                "/repository/org.apache/tomcat/10.1.18/tomcat-10.1.18.jar", Join.join("\n", paths(dir, list)));
    }

    @Test
    public void exclude() throws Exception {
        final File dir = dir();
        final Work work = Dir.of(Work.class, dir);
        final List<File> list = work.notJars().collect(Collectors.toList());

        assertEquals("" +
                "/repository/junit/junit/4.12/junit-4.12.jar.sha1\n" +
                "/repository/junit/junit/4.12/junit-4.12.pom\n" +
                "/repository/org.apache/tomcat/10.1.18/tomcat-10.1.18.pom", Join.join("\n", paths(dir, list)));
    }

    @Test
    public void includeAndExclude() throws Exception {
        final File dir = dir();
        final Work work = Dir.of(Work.class, dir);
        final List<File> list = work.binaryJars().collect(Collectors.toList());

        assertEquals("" +
                "/repository/junit/junit/4.12/junit-4.12.jar\n" +
                "/repository/org.apache/tomcat/10.1.18/tomcat-10.1.18.jar", Join.join("\n", paths(dir, list)));
    }

    /**
     * @Match and @Filter only see entries that already passed @Suffix
     */
    @Test
    public void suffixMatchAndFilter() throws Exception {
        final File dir = dir();
        final Work work = Dir.of(Work.class, dir);
        final List<File> list = work.junitBinaryJars().collect(Collectors.toList());

        assertEquals("/repository/junit/junit/4.12/junit-4.12.jar", Join.join("\n", paths(dir, list)));
    }

    @Test
    public void array() throws Exception {
        final File dir = dir();
        final Version version = Dir.of(Version.class, new File(dir, "repository/junit/junit/4.12"));
        final List<File> list = Arrays.asList(version.poms());

        assertEquals("/repository/junit/junit/4.12/junit-4.12.pom", Join.join("\n", paths(dir, list)));
    }

    public String path(final File file) {
        if (file.isDirectory()) return file.getAbsolutePath() + "/";
        else return file.getAbsolutePath();
    }

    private List<String> paths(final File dir, final List<File> list) {
        final int trim = path(dir).length() - 1;
        return list.stream()
                .map(this::path)
                .map(s -> s.substring(trim))
                .sorted()
                .collect(Collectors.toList());
    }

    public interface Work extends Dir {
        @Walk
        @Suffix(".pom")
        Stream<File> poms();

        @Walk
        @Suffix(".jar")
        Stream<File> jars();

        @Walk
        @Suffix({".jar", ".pom"})
        Stream<File> jarsAndPoms();

        @Walk
        @Suffix(".jar")
        @Suffix(".pom")
        Stream<File> jarsAndPomsStacked();

        @Walk
        @Suffix(".jar")
        @Suffix(value = "-sources.jar", exclude = true)
        @Suffix(value = "-javadoc.jar", exclude = true)
        Stream<File> binaryJarsStacked();

        @Walk
        @Filter(IsFile.class)
        @Suffix(value = ".jar", exclude = true)
        Stream<File> notJars();

        @Walk
        @Suffix(".jar")
        @Suffix(value = {"-sources.jar", "-javadoc.jar"}, exclude = true)
        Stream<File> binaryJars();

        @Walk
        @Suffix(".jar")
        @Match(value = ".*-(sources|javadoc)\\.jar", exclude = true)
        @Filter(IsJunit.class)
        Stream<File> junitBinaryJars();
    }

    public interface Version extends Dir {
        @Suffix(".pom")
        File[] poms();
    }

    public static class IsJunit implements FileFilter {
        @Override
        public boolean accept(final File pathname) {
            return pathname.getAbsolutePath().contains("junit");
        }
    }

    public static class IsFile implements FileFilter {
        @Override
        public boolean accept(final File pathname) {
            return pathname.isFile();
        }
    }
}
