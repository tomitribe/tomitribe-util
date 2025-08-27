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

public class PropertyEditorException extends IllegalArgumentException {

    /**
     * Default constructor for a PropertyException.
     */
    public PropertyEditorException() {
    }

    /**
     * PropertyEditorException with a wrappered inner exception.
     *
     * @param cause Original root cause of the PropertyEditorException.
     */
    public PropertyEditorException(final Throwable cause) {
        super(cause);
    }

    /**
     * A PropertyEditorException with just an error message.
     *
     * @param message The text error message describing the condition.
     */
    public PropertyEditorException(final String message) {
        super(message);
    }

    /**
     * An exception with both a message and an internal exception.
     *
     * @param message The test error message.
     * @param cause   The root cause for the exception.
     */
    public PropertyEditorException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
