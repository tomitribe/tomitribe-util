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

import org.junit.Before;
import org.junit.Test;
import org.tomitribe.util.Archive;
import org.tomitribe.util.Files;
import org.tomitribe.util.Join;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CustomFileObjectWalkTest {

    private File dir;
    private List<String> paths;
    private List<String> expected;
    private Module module;

    @Before
    public void setup() throws Exception {
        dir = Files.tmpdir();

        paths = Arrays.asList(
                "src/main/java/io/superbiz/colors/Red.java",
                "src/main/java/io/superbiz/colors/Green.java",
                "src/main/java/io/superbiz/colors/Blue.java",
                "src/test/java/io/superbiz/colors/RedTest.java",
                "src/test/java/io/superbiz/colors/GreenTest.java",
                "src/test/java/io/superbiz/colors/BlueTest.java"
        );

        for (final String path : paths) {
            final File file = new File(dir, path);
            Files.mkparent(file);
            assertTrue(file.createNewFile());
        }

        module = Dir.of(Module.class, dir);

        expected = paths.stream()
                .map(s -> new File(dir, s))
                .map(File::getAbsolutePath)
                .sorted()
                .collect(Collectors.toList());
    }

    @Test
    public void testStreamOfFiles() throws Exception {

        final List<String> actual = module.streamOfFiles()
                .map(Archivo.class::cast) // explicitly verify type
                .map(Archivo::getAbsolutePath)
                .sorted()
                .collect(Collectors.toList());

        assertEquals(Join.join("\n", expected), Join.join("\n", actual));
    }

    @Test
    public void testStream() throws Exception {

        final List<String> actual = module.streamOfJava()
                .map(Java.class::cast) // explicitly verify type
                .map(Dir::dir)
                .map(File::getAbsolutePath)
                .sorted()
                .collect(Collectors.toList());

        assertEquals(Join.join("\n", expected), Join.join("\n", actual));
    }

    @Test
    public void testArrayOfFiles() throws Exception {

        final Archivo[] values = module.arrayOfFiles();
        final List<String> actual = Stream.of(values)
                .map(Archivo.class::cast) // explicitly verify type
                .map(Archivo::getAbsolutePath)
                .sorted()
                .collect(Collectors.toList());

        assertEquals(Join.join("\n", expected), Join.join("\n", actual));
    }

    @Test
    public void testArray() throws Exception {

        final List<String> actual = Stream.of(module.arrayOfJava())
                .map(Java.class::cast) // explicitly verify type
                .map(Dir::dir)
                .map(File::getAbsolutePath)
                .sorted()
                .collect(Collectors.toList());

        assertEquals(Join.join("\n", expected), Join.join("\n", actual));
    }

    @Test
    public void walk() throws Exception {
        final File dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir();

        final Work work = Dir.of(Work.class, dir);
        final List<Archivo> list = work.nofilter().collect(Collectors.toList());

        final List<String> paths = paths(dir, list);

        assertEquals("/\n" +
                "/repository/\n" +
                "/repository/io.tomitribe/\n" +
                "/repository/io.tomitribe/crest/\n" +
                "/repository/io.tomitribe/crest/5/\n" +
                "/repository/io.tomitribe/crest/5/5.4.1.2/\n" +
                "/repository/io.tomitribe/crest/5/5.4.1.2/baz.txt\n" +
                "/repository/junit/\n" +
                "/repository/junit/junit/\n" +
                "/repository/junit/junit/4/\n" +
                "/repository/junit/junit/4/4.12/\n" +
                "/repository/junit/junit/4/4.12/bar.txt\n" +
                "/repository/org.color.bright/\n" +
                "/repository/org.color.bright/green/\n" +
                "/repository/org.color.bright/green/1/\n" +
                "/repository/org.color.bright/green/1/1.4/\n" +
                "/repository/org.color.bright/green/1/1.4/foo.txt\n" +
                "/repository/org.color/\n" +
                "/repository/org.color/red/\n" +
                "/repository/org.color/red/1/\n" +
                "/repository/org.color/red/1/1.4/\n" +
                "/repository/org.color/red/1/1.4/foo.txt", Join.join("\n", paths));
    }

    @Test
    public void maxDepthOne() throws Exception {
        final File dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir();

        final Work work = Dir.of(Work.class, dir);
        final List<Archivo> list = work.maxOne().collect(Collectors.toList());

        final List<String> paths = paths(dir, list);

        assertEquals("/\n" +
                "/repository/", Join.join("\n", paths));
    }

    @Test
    public void maxDepthTwo() throws Exception {
        final File dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir();

        final Work work = Dir.of(Work.class, dir);
        final List<Archivo> list = work.maxTwo().collect(Collectors.toList());

        final List<String> paths = paths(dir, list);

        assertEquals("/\n" +
                "/repository/\n" +
                "/repository/io.tomitribe/\n" +
                "/repository/junit/\n" +
                "/repository/org.color.bright/\n" +
                "/repository/org.color/", Join.join("\n", paths));
    }

    @Test
    public void minDepthOne() throws Exception {
        final File dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir();

        final Work work = Dir.of(Work.class, dir);
        final List<Archivo> list = work.minOne().collect(Collectors.toList());

        final List<String> paths = paths(dir, list);

        assertEquals("/repository/\n" +
                "/repository/io.tomitribe/\n" +
                "/repository/io.tomitribe/crest/\n" +
                "/repository/io.tomitribe/crest/5/\n" +
                "/repository/io.tomitribe/crest/5/5.4.1.2/\n" +
                "/repository/io.tomitribe/crest/5/5.4.1.2/baz.txt\n" +
                "/repository/junit/\n" +
                "/repository/junit/junit/\n" +
                "/repository/junit/junit/4/\n" +
                "/repository/junit/junit/4/4.12/\n" +
                "/repository/junit/junit/4/4.12/bar.txt\n" +
                "/repository/org.color.bright/\n" +
                "/repository/org.color.bright/green/\n" +
                "/repository/org.color.bright/green/1/\n" +
                "/repository/org.color.bright/green/1/1.4/\n" +
                "/repository/org.color.bright/green/1/1.4/foo.txt\n" +
                "/repository/org.color/\n" +
                "/repository/org.color/red/\n" +
                "/repository/org.color/red/1/\n" +
                "/repository/org.color/red/1/1.4/\n" +
                "/repository/org.color/red/1/1.4/foo.txt", Join.join("\n", paths));
    }

    @Test
    public void minDepthTwo() throws Exception {
        final File dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir();

        final Work work = Dir.of(Work.class, dir);
        final List<Archivo> list = work.minTwo().collect(Collectors.toList());

        final List<String> paths = paths(dir, list);

        assertEquals("" +
                "/repository/io.tomitribe/\n" +
                "/repository/io.tomitribe/crest/\n" +
                "/repository/io.tomitribe/crest/5/\n" +
                "/repository/io.tomitribe/crest/5/5.4.1.2/\n" +
                "/repository/io.tomitribe/crest/5/5.4.1.2/baz.txt\n" +
                "/repository/junit/\n" +
                "/repository/junit/junit/\n" +
                "/repository/junit/junit/4/\n" +
                "/repository/junit/junit/4/4.12/\n" +
                "/repository/junit/junit/4/4.12/bar.txt\n" +
                "/repository/org.color.bright/\n" +
                "/repository/org.color.bright/green/\n" +
                "/repository/org.color.bright/green/1/\n" +
                "/repository/org.color.bright/green/1/1.4/\n" +
                "/repository/org.color.bright/green/1/1.4/foo.txt\n" +
                "/repository/org.color/\n" +
                "/repository/org.color/red/\n" +
                "/repository/org.color/red/1/\n" +
                "/repository/org.color/red/1/1.4/\n" +
                "/repository/org.color/red/1/1.4/foo.txt", Join.join("\n", paths));
    }

    @Test
    public void minOneMaxTwo() throws Exception {
        final File dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir();

        final Work work = Dir.of(Work.class, dir);
        final List<Archivo> list = work.minOneMaxTwo().collect(Collectors.toList());

        final List<String> paths = paths(dir, list);

        assertEquals("/repository/\n" +
                "/repository/io.tomitribe/\n" +
                "/repository/junit/\n" +
                "/repository/org.color.bright/\n" +
                "/repository/org.color/", Join.join("\n", paths));
    }

    @Test
    public void minTwoMaxTwo() throws Exception {
        final File dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir();

        final Work work = Dir.of(Work.class, dir);
        final List<Archivo> list = work.minTwoMaxTwo().collect(Collectors.toList());

        final List<String> paths = paths(dir, list);

        assertEquals("" +
                "/repository/io.tomitribe/\n" +
                "/repository/junit/\n" +
                "/repository/org.color.bright/\n" +
                "/repository/org.color/", Join.join("\n", paths));
    }

    public String path(final File file) {
        if (file.isDirectory()) return file.getAbsolutePath() + "/";
        else return file.getAbsolutePath();
    }
    public String path(final Archivo file) {
        if (file.isDirectory()) return file.getAbsolutePath() + "/";
        else return file.getAbsolutePath();
    }

    private List<String> paths(final File dir, final List<Archivo> list) {
        final int trim = path(dir).length() - 1;
        return list.stream()
                .map(this::path)
                .map(s -> s.substring(trim))
                .sorted()
                .collect(Collectors.toList());
    }

    public interface Work extends Dir {
        @Walk
        Stream<Archivo> nofilter();

        @Walk(maxDepth = 1)
        Stream<Archivo> maxOne();

        @Walk(maxDepth = 2)
        Stream<Archivo> maxTwo();

        @Walk(minDepth = 1)
        Stream<Archivo> minOne();

        @Walk(minDepth = 2)
        Stream<Archivo> minTwo();

        @Walk(minDepth = 1, maxDepth = 2)
        Stream<Archivo> minOneMaxTwo();

        @Walk(minDepth = 2, maxDepth = 2)
        Stream<Archivo> minTwoMaxTwo();
    }

    public interface Module extends Dir {

        @Walk
        @Filter(IsJava.class)
        Stream<Archivo> streamOfFiles();

        @Walk
        @Filter(IsJava.class)
        Stream<Java> streamOfJava();

        @Walk
        @Filter(IsJava.class)
        Archivo[] arrayOfFiles();

        @Walk
        @Filter(IsJava.class)
        Java[] arrayOfJava();

    }

    public interface Java extends Dir {

    }

    public static class IsJava implements FileFilter {
        @Override
        public boolean accept(final File pathname) {
            return pathname.getName().endsWith(".java");
        }
    }


    public static class Archivo {
        private final File file;

        private Archivo(final File file, final boolean ignored) {
            this.file = file;
        }

        public String getName() {
            return file.getName();
        }

        public boolean isDirectory() {
            return file.isDirectory();
        }

        public String getAbsolutePath() {
            return file.getAbsolutePath();
        }

        public static Archivo from(final File file) {
            return new Archivo(file, false);
        }
    }

}
