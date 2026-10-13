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

public class MatchTest {

    private File dir() throws Exception {
        return new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/junit/junit/4/4.12/pom.xml", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/notes.csv", "")
                .toDir();
    }

    @Test
    public void include() throws Exception {
        final File dir = dir();
        final Work work = Dir.of(Work.class, dir);
        final List<File> list = work.txtFiles().collect(Collectors.toList());

        assertEquals("" +
                "/repository/io.tomitribe/crest/5/5.4.1.2/baz.txt\n" +
                "/repository/junit/junit/4/4.12/bar.txt\n" +
                "/repository/org.color.bright/green/1/1.4/foo.txt\n" +
                "/repository/org.color/red/1/1.4/foo.txt", Join.join("\n", paths(dir, list)));
    }

    /**
     * The entire name must match. A pattern of "txt" matches nothing
     * because no file is named exactly "txt".
     */
    @Test
    public void fullMatchSemantics() throws Exception {
        final File dir = dir();
        final Work work = Dir.of(Work.class, dir);
        final List<File> list = work.namedTxt().collect(Collectors.toList());

        assertEquals("", Join.join("\n", paths(dir, list)));
    }

    @Test
    public void alternation() throws Exception {
        final File dir = dir();
        final Work work = Dir.of(Work.class, dir);
        final List<File> list = work.txtOrCsv().collect(Collectors.toList());

        assertEquals("" +
                "/repository/io.tomitribe/crest/5/5.4.1.2/baz.txt\n" +
                "/repository/io.tomitribe/crest/5/5.4.1.2/notes.csv\n" +
                "/repository/junit/junit/4/4.12/bar.txt\n" +
                "/repository/org.color.bright/green/1/1.4/foo.txt\n" +
                "/repository/org.color/red/1/1.4/foo.txt", Join.join("\n", paths(dir, list)));
    }

    @Test
    public void exclude() throws Exception {
        final File dir = dir();
        final Work work = Dir.of(Work.class, dir);
        final List<File> list = work.notTxt().collect(Collectors.toList());

        assertEquals("" +
                "/repository/io.tomitribe/crest/5/5.4.1.2/notes.csv\n" +
                "/repository/junit/junit/4/4.12/pom.xml", Join.join("\n", paths(dir, list)));
    }

    @Test
    public void includeAndExclude() throws Exception {
        final File dir = dir();
        final Work work = Dir.of(Work.class, dir);
        final List<File> list = work.txtExceptFoo().collect(Collectors.toList());

        assertEquals("" +
                "/repository/io.tomitribe/crest/5/5.4.1.2/baz.txt\n" +
                "/repository/junit/junit/4/4.12/bar.txt", Join.join("\n", paths(dir, list)));
    }

    /**
     * @Filter only sees entries that already passed @Match
     */
    @Test
    public void matchAndFilter() throws Exception {
        final File dir = dir();
        final Work work = Dir.of(Work.class, dir);
        final List<File> list = work.junitTxt().collect(Collectors.toList());

        assertEquals("/repository/junit/junit/4/4.12/bar.txt", Join.join("\n", paths(dir, list)));
    }

    @Test
    public void array() throws Exception {
        final File dir = dir();
        final Work work = Dir.of(Work.class, dir);
        final List<File> list = Arrays.asList(work.txtArray());

        assertEquals("" +
                "/repository/io.tomitribe/crest/5/5.4.1.2/baz.txt\n" +
                "/repository/junit/junit/4/4.12/bar.txt\n" +
                "/repository/org.color.bright/green/1/1.4/foo.txt\n" +
                "/repository/org.color/red/1/1.4/foo.txt", Join.join("\n", paths(dir, list)));
    }

    /**
     * Without @Walk only the immediate children are considered
     */
    @Test
    public void noWalk() throws Exception {
        final File dir = dir();
        final Repository repository = Dir.of(Repository.class, new File(dir, "repository"));
        final List<File> list = repository.orgs().collect(Collectors.toList());

        assertEquals("" +
                "/repository/org.color.bright/\n" +
                "/repository/org.color/", Join.join("\n", paths(dir, list)));
    }

    @Test
    public void interfaceStream() throws Exception {
        final File dir = dir();
        final Repository repository = Dir.of(Repository.class, new File(dir, "repository"));
        final List<File> list = repository.orgGroups()
                .map(Group::get)
                .collect(Collectors.toList());

        assertEquals("" +
                "/repository/org.color.bright/\n" +
                "/repository/org.color/", Join.join("\n", paths(dir, list)));
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
        @Match(".*\\.txt")
        Stream<File> txtFiles();

        @Walk
        @Match("txt")
        Stream<File> namedTxt();

        @Walk
        @Match(".*\\.(txt|csv)")
        Stream<File> txtOrCsv();

        @Walk
        @Filter(IsFile.class)
        @Match(value = ".*\\.txt", exclude = true)
        Stream<File> notTxt();

        @Walk
        @Match(".*\\.txt")
        @Match(value = "foo\\.txt", exclude = true)
        Stream<File> txtExceptFoo();

        @Walk
        @Match(".*\\.txt")
        @Filter(IsJunit.class)
        Stream<File> junitTxt();

        @Walk
        @Match(".*\\.txt")
        File[] txtArray();
    }

    public interface Repository extends Dir {
        @Match("org\\..*")
        Stream<File> orgs();

        @Match("org\\..*")
        Stream<Group> orgGroups();
    }

    public interface Group extends Dir {
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
