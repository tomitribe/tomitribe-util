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

import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.isDirectory;
import static org.junit.Assert.assertEquals;
import static org.tomitribe.util.paths.Dir.of;

public class FilterTest {

    @Test
    public void filter() throws Exception {
        final Path dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir()
                .toPath();

        final Work work = of(Work.class, dir);
        final List<Path> list = work.junitFiles().collect(Collectors.toList());

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
        final Path dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/junit/junit/4/4.12/pom.xml", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir()
                .toPath();

        final Work work = of(Work.class, dir);
        final List<Path> list = work.txtFiles().collect(Collectors.toList());

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
     * Stream&lt;Path&gt; junitTxtFiles();
     * </pre>
     */
    @Test
    public void filters() throws Exception {
        final Path dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/junit/junit/4/4.12/pom.xml", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir()
                .toPath();

        final Work work = of(Work.class, dir);
        final List<Path> list = work.junitTxtFiles().collect(Collectors.toList());

        final List<String> paths = paths(dir, list);

        assertEquals("/repository/junit/junit/4/4.12/bar.txt", Join.join("\n", paths));
    }

    public String path(final Path path) {
        if (isDirectory(path)) return path.toAbsolutePath() + "/";
        else return path.toAbsolutePath().toString();
    }

    private List<String> paths(final Path dir, final List<Path> list) {
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
        Stream<Path> junitFiles();

        @Walk
        @Filter(IsTxt.class)
        Stream<Path> txtFiles();

        @Walk
        @Filter(IsJunit.class)
        @Filter(IsTxt.class)
        Stream<Path> junitTxtFiles();
    }

    public static class IsJunit implements Predicate<Path> {
        @Override
        public boolean test(final Path path) {
            return path.toAbsolutePath().toString().contains("junit");
        }
    }

    public static class IsTxt implements Predicate<Path> {
        @Override
        public boolean test(final Path path) {
            return path.getFileName().toString().endsWith(".txt");
        }
    }
}
