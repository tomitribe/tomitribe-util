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

package org.tomitribe.util.reflect;

import java.lang.reflect.AccessibleObject;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @version $Rev: 1594983 $ $Date: 2014-05-15 10:10:15 -0700 (Thu, 15 May 2014) $
 */
public class SetAccessible implements PrivilegedAction {
    private final AccessibleObject object;

    public SetAccessible(final AccessibleObject object) {
        this.object = object;
    }

    public Object run() {
        object.setAccessible(true);
        return object;
    }

    public static <T extends AccessibleObject> T on(final T object) {
        return (T) AccessController.doPrivileged(new SetAccessible(object));
    }
}
