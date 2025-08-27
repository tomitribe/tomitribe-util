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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class FilterTest {

    @Test
    public void filter() throws Exception {
        final File dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir();

        final Work work = Dir.of(Work.class, dir);
        final List<File> list = work.junitFiles().collect(Collectors.toList());

        final List<String> paths = paths(dir, list);

        assertEquals("" +
                "/repository/junit/\n" +
                "/repository/junit/junit/\n" +
                "/repository/junit/junit/4/\n" +
                "/repository/junit/junit/4/4.12/\n" +
                "/repository/junit/junit/4/4.12/bar.txt", Join.join("\n", paths));
    }

    @Test
    public void filterTxt() throws Exception {
        final File dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/junit/junit/4/4.12/pom.xml", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir();

        final Work work = Dir.of(Work.class, dir);
        final List<File> list = work.txtFiles().collect(Collectors.toList());

        final List<String> paths = paths(dir, list);

        assertEquals("" +
                "/repository/io.tomitribe/crest/5/5.4.1.2/baz.txt\n" +
                "/repository/junit/junit/4/4.12/bar.txt\n" +
                "/repository/org.color.bright/green/1/1.4/foo.txt\n" +
                "/repository/org.color/red/1/1.4/foo.txt", Join.join("\n", paths));
    }

    /**
     * Introduced in 1.3.14, @Filter is now a repeatable annotation
     *
     * This tests both filters are applied
     *
     * <pre>
     * &commat;Walk
     * &commat;Filter(IsJunit.class)
     * &commat;Filter(IsTxt.class)
     * Stream<File> junitTxtFiles();
     * </pre>
     */
    @Test
    public void filters() throws Exception {
        final File dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/junit/junit/4/4.12/pom.xml", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir();

        final Work work = Dir.of(Work.class, dir);
        final List<File> list = work.junitTxtFiles().collect(Collectors.toList());

        final List<String> paths = paths(dir, list);

        assertEquals("/repository/junit/junit/4/4.12/bar.txt", Join.join("\n", paths));
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
        @Filter(IsJunit.class)
        Stream<File> junitFiles();

        @Walk
        @Filter(IsTxt.class)
        Stream<File> txtFiles();

        @Walk
        @Filter(IsJunit.class)
        @Filter(IsTxt.class)
        Stream<File> junitTxtFiles();

    }

    public static class IsJunit implements FileFilter {
        @Override
        public boolean accept(final File pathname) {
            return pathname.getAbsolutePath().contains("junit");
        }
    }

    public static class IsTxt implements FileFilter {
        @Override
        public boolean accept(final File pathname) {
            return pathname.getName().endsWith(".txt");
        }
    }

}
