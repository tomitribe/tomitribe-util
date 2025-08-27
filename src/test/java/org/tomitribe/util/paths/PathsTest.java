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

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.util.JarLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.tomitribe.util.Join.join;

public class PathsTest extends Assert {

    private final Path testClasses = Paths.path(JarLocation.jarLocation(PathsTest.class).getAbsolutePath());
    private final Path colors = testClasses.resolve("colors");

    @Test
    public void testCollect() {
        final List<Path> files = Paths.collect(colors);
        assertAll(testClasses, files);
    }

    @Test
    public void testVisit() {
        final List<Path> files = new ArrayList<>();
        Paths.visit(colors, new Paths.Visitor() {
            @Override
            public boolean visit(final Path path) {
                files.add(path);
                return true;
            }
        });
        assertAll(testClasses, files);
    }

    @Test
    public void testIterate() {
        final List<Path> files = new ArrayList<>();
        for (final Path file : Paths.iterate(colors)) {
            files.add(file);
        }
        assertAll(testClasses, files);
    }

    @Test
    public void testCollectFiltered() {
        final List<Path> files = Paths.collect(colors, Pattern.compile(".*\\.txt"));
        assertTxtFiles(testClasses, files);
    }

    @Test
    public void testVisitFiltered() {
        final List<Path> files = new ArrayList<>();
        Paths.visit(colors, Pattern.compile(".*\\.txt"), path -> {
            files.add(path);
            return true;
        });
        assertTxtFiles(testClasses, files);
    }

    @Test
    public void testIterateFiltered() {
        final List<Path> files = new ArrayList<>();
        for (final Path path : Paths.iterate(colors, ".*\\.txt")) {
            files.add(path);
        }
        assertTxtFiles(testClasses, files);
    }

    private String[] relativize(final Path base, final List<Path> paths) {
        return paths.stream()
                .map(p -> "/" + base.relativize(p).toString().replace('\\', '/'))
                .toArray(String[]::new);
    }

    private void assertTxtFiles(final Path base, final List<Path> files) {
        final String[] actual = relativize(base, files);

        final String[] expected = {
                "/colors/blue/midnight.txt",
                "/colors/blue/pastel/powder.txt",
                "/colors/orange.txt",
                "/colors/other/cmyk/magenta.txt",
                "/colors/other/cmyk/yellow.txt",
                "/colors/other/hsb/hue.txt",
                "/colors/red/crimson.txt"
        };

        Arrays.sort(actual);
        Arrays.sort(expected);
        assertEquals(join("\n", expected), join("\n", actual));
    }

    private void assertAll(final Path base, final List<Path> files) {
        final String[] actual = relativize(base, files);

        final String[] expected = {
                "/colors/blue",
                "/colors/blue/midnight.txt",
                "/colors/blue/navy.csv",
                "/colors/blue/pastel",
                "/colors/blue/pastel/powder.txt",
                "/colors/green",
                "/colors/green/emerald.csv",
                "/colors/orange.txt",
                "/colors/other",
                "/colors/other/cmyk",
                "/colors/other/cmyk/cyan.csv",
                "/colors/other/cmyk/key.sh",
                "/colors/other/cmyk/magenta.txt",
                "/colors/other/cmyk/yellow.txt",
                "/colors/other/hsb",
                "/colors/other/hsb/brightness.csv",
                "/colors/other/hsb/hue.txt",
                "/colors/other/hsb/saturation",
                "/colors/red",
                "/colors/red/crimson.txt"
        };

        Arrays.sort(actual);
        Arrays.sort(expected);
        assertEquals(join("\n", expected), join("\n", actual));
    }
}
