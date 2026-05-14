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

package org.tomitribe.util.editor;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Editors {

    private static final Map<Class<?>, Class<? extends PropertyEditor>> BUILTIN;

    static {
        final Map<Class<?>, Class<? extends PropertyEditor>> map = new HashMap<>();
        map.put(Path.class, PathEditor.class);
        map.put(Date.class, DateEditor.class);
        map.put(Character.class, CharacterEditor.class);
        BUILTIN = Collections.unmodifiableMap(map);
    }

    private Editors() {
        // no-op
    }

    public static PropertyEditor get(final Class<?> type) {
        final PropertyEditor editor = PropertyEditorManager.findEditor(type);

        if (editor != null) return editor;

        final Class<? extends PropertyEditor> editorClass = BUILTIN.get(type);
        if (editorClass == null) return null;

        PropertyEditorManager.registerEditor(type, editorClass);
        return PropertyEditorManager.findEditor(type);
    }
}
