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

import junit.framework.TestCase;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class StringTemplateTest extends TestCase {

    public void testApply() throws Exception {
        final StringTemplate template = new StringTemplate("http://{host}:{port}/{path}");

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("host", "localhost");
        params.put("port", 90);
        params.put("path", new StringBuffer("one/two/three"));

        final String string = template.apply(params);

        assertEquals("http://localhost:90/one/two/three", string);
    }

    public void testKeys() throws Exception {
        final StringTemplate template = new StringTemplate("http://{host}:{port}/{path}");

        final Set<String> keys = template.keys();
        assertEquals(3, keys.size());

        final Iterator<String> iterator = keys.iterator();
        assertEquals("host", iterator.next());
        assertEquals("path", iterator.next());
        assertEquals("port", iterator.next());
    }

    public void testCustomDelimiters() throws Exception {
        final StringTemplate template = new StringTemplate("http://{{host}}:{{port}}/{{path}}", "{{", "}}");

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("host", "localhost");
        params.put("port", 90);
        params.put("path", new StringBuffer("one/two/three"));

        final String string = template.apply(params);

        assertEquals("http://localhost:90/one/two/three", string);
    }

    public void testCustomDelimitersKeys() throws Exception {
        final StringTemplate template = new StringTemplate("http://{{host}}:{{port}}/{{path}}", "{{", "}}");

        final Set<String> keys = template.keys();
        assertEquals(3, keys.size());

        final Iterator<String> iterator = keys.iterator();
        assertEquals("host", iterator.next());
        assertEquals("path", iterator.next());
        assertEquals("port", iterator.next());
    }

    public void testBuilder() throws Exception {
        final StringTemplate template = StringTemplate.builder()
                .template("http://{{host}}:{{port}}/{{path}}")
                .delimiters("{{", "}}")
                .build();

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("host", "localhost");
        params.put("port", 90);
        params.put("path", new StringBuffer("one/two/three"));

        final String string = template.apply(params);

        assertEquals("http://localhost:90/one/two/three", string);
    }

    public void testBuilderDefaultDelimiters() throws Exception {
        final StringTemplate template = StringTemplate.builder()
                .template("http://{host}:{port}/{path}")
                .build();

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("host", "localhost");
        params.put("port", 90);
        params.put("path", new StringBuffer("one/two/three"));

        final String string = template.apply(params);

        assertEquals("http://localhost:90/one/two/three", string);
    }

    public void testApplier() throws Exception {
        final StringTemplate template = new StringTemplate("http://{host}:{port}/{path}");

        final String string = template.applier()
                .set("host", "localhost")
                .set("port", 90)
                .set("path", new StringBuffer("one/two/three"))
                .apply();

        assertEquals("http://localhost:90/one/two/three", string);
    }

    public void testBuilderTemplateFile() throws Exception {
        final File file = File.createTempFile("template", ".txt");
        file.deleteOnExit();
        IO.copy("http://{host}:{port}/{path}", file);

        final String string = StringTemplate.builder()
                .template(file)
                .build()
                .applier()
                .set("host", "localhost")
                .set("port", 90)
                .set("path", "one/two/three")
                .apply();

        assertEquals("http://localhost:90/one/two/three", string);
    }

    public void testBuilderTemplateFileCustomDelimiters() throws Exception {
        final File file = File.createTempFile("template", ".txt");
        file.deleteOnExit();
        IO.copy("http://{{host}}:{{port}}/{{path}}", file);

        final String string = StringTemplate.builder()
                .template(file)
                .delimiters("{{", "}}")
                .build()
                .applier()
                .set("host", "localhost")
                .set("port", 90)
                .set("path", "one/two/three")
                .apply();

        assertEquals("http://localhost:90/one/two/three", string);
    }

    public void testBuilderTemplateSupplier() throws Exception {
        final String string = StringTemplate.builder()
                .template(() -> "http://{host}:{port}/{path}")
                .build()
                .applier()
                .set("host", "localhost")
                .set("port", 90)
                .set("path", "one/two/three")
                .apply();

        assertEquals("http://localhost:90/one/two/three", string);
    }

    public void testBuilderTemplateURL() throws Exception {
        final File file = File.createTempFile("template", ".txt");
        file.deleteOnExit();
        IO.copy("http://{host}:{port}/{path}", file);

        final URL url = file.toURI().toURL();

        final String string = StringTemplate.builder()
                .template(url)
                .build()
                .applier()
                .set("host", "localhost")
                .set("port", 90)
                .set("path", "one/two/three")
                .apply();

        assertEquals("http://localhost:90/one/two/three", string);
    }

    public void testBuilderTemplateURLCustomDelimiters() throws Exception {
        final File file = File.createTempFile("template", ".txt");
        file.deleteOnExit();
        IO.copy("http://{{host}}:{{port}}/{{path}}", file);

        final URL url = file.toURI().toURL();

        final String string = StringTemplate.builder()
                .template(url)
                .delimiters("{{", "}}")
                .build()
                .applier()
                .set("host", "localhost")
                .set("port", 90)
                .set("path", "one/two/three")
                .apply();

        assertEquals("http://localhost:90/one/two/three", string);
    }

    public void testBuilderWithApplier() throws Exception {
        final String string = StringTemplate.builder()
                .template("http://{{host}}:{{port}}/{{path}}")
                .delimiters("{{", "}}")
                .build()
                .applier()
                .set("host", "localhost")
                .set("port", 90)
                .set("path", "one/two/three")
                .apply();

        assertEquals("http://localhost:90/one/two/three", string);
    }

}
