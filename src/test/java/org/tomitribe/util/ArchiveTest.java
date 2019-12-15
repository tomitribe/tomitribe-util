package org.tomitribe.util;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ArchiveTest {

    @Test
    public void add() throws IOException {
        final Archive archive = new Archive()
                .add("colors/red.txt", "crimson")
                .add("colors/green/emerald.txt", "#50c878");

        final File dir = archive.toDir();

        assertTrue(dir.isDirectory());
        assertEquals(1, dir.listFiles().length);
        assertEquals("colors", dir.listFiles()[0].getName());
        assertFile(dir, "colors/red.txt", "crimson");
        assertFile(dir, "colors/green/emerald.txt", "#50c878");
    }

    @Test
    public void add2() throws IOException {

        final Archive nested = new Archive()
                .add("red.txt", "crimson")
                .add("green/emerald.txt", "#50c878");
        final Archive archive = new Archive();
        archive.add("colors", nested);
        final File dir = archive.toDir();

        assertTrue(dir.isDirectory());
        assertEquals(1, dir.listFiles().length);
        assertEquals("colors", dir.listFiles()[0].getName());
        assertFile(dir, "colors/red.txt", "crimson");
        assertFile(dir, "colors/green/emerald.txt", "#50c878");
    }

    private static void assertFile(final File dir, final String name, final String expected) throws IOException {
        final File file = new File(dir, name);
        assertTrue(name, file.exists());
        assertEquals(expected, IO.slurp(file));
    }
}