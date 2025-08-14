/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.tomitribe.util.paths;

import org.junit.Test;
import org.tomitribe.util.Files;

import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.tomitribe.util.paths.Dir.of;

public class DefaultMethodsTest {

    @Test
    public void test() throws Exception {
        final Path dir = Files.tmpdir().toPath();

        final HasDefault hasDefault = of(HasDefault.class, dir);

        final long modified = hasDefault.modified();
        assertTrue(modified > 1573459973000L);
    }

    @Test
    public void hasArgs() throws Exception {
        final Path dir = Files.tmpdir().toPath();

        final HasDefaultWithArgs hasDefault = of(HasDefaultWithArgs.class, dir);

        assertEquals(33, hasDefault.modified(11));
    }

    @Test
    public void hasArgs2() throws Exception {
        final Path dir = Files.tmpdir().toPath();

        final Repository repository = of(Repository.class, dir);

        final Group group = repository.group("org.color");
        assertFiles(dir, "org/color", group.dir());
    }

    private void assertFiles(final Path dir, final String expectedPath, final Path actual) {
        final Path expected = dir.resolve(expectedPath);
        assertEquals(expected.toAbsolutePath().toString(), actual.toAbsolutePath().toString());
    }

    public interface HasDefault extends Dir {
        Path java();

        default long modified() {
            return dir().toFile().lastModified();
        }
    }

    public interface HasDefaultWithArgs extends Dir {
        Path java();

        default long modified(final int offset) {
            return offset * 3;
        }
    }

    public interface Group extends Dir {
    }

    public interface Repository extends Dir {
        default Group group(final String name) {
            final String path = name.replace(".", "/");
            final Group group = of(Group.class, dir().resolve(path));
            group.mkdirs();
            return group;
        }
    }
}