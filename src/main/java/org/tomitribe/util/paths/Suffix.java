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

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Filters listing results to entries whose {@link java.nio.file.Path#getFileName() file name}
 * ends with one of the given suffixes. Multiple values are OR'd &mdash;
 * an entry matches if its name ends with <b>any</b> of them.
 *
 * <p>This is a shorthand for the common case of filtering by file
 * extension. For more complex name patterns use {@link Match @Match}.
 * For arbitrary criteria use {@link Filter @Filter}.
 *
 * <p>{@code @Suffix} is repeatable. When {@link #exclude()} is
 * {@code false} (the default), the annotation <b>includes</b> entries
 * matching any of its suffixes. When {@code exclude = true}, it
 * <b>excludes</b> entries matching any of its suffixes. Includes are
 * applied before excludes, so you can combine them:
 *
 * <pre>
 * public interface Assets extends Dir {
 *     &#64;Suffix(".css")
 *     Stream&lt;Path&gt; stylesheets();
 *
 *     &#64;Suffix({".jpg", ".png", ".gif"})
 *     Stream&lt;Path&gt; images();
 *
 *     // All .jar files except sources and javadoc jars
 *     &#64;Suffix(".jar")
 *     &#64;Suffix(value = {"-sources.jar", "-javadoc.jar"}, exclude = true)
 *     Stream&lt;Path&gt; binaryJars();
 * }
 * </pre>
 *
 * <h2>Evaluation order</h2>
 *
 * <p>Within one annotation kind, all includes are OR'd together, all
 * excludes are OR'd together, and an entry passes if it matches an
 * include and does not match an exclude. Repeating the annotation is
 * therefore the same as listing more values in one annotation.
 *
 * <p>Different kinds are AND'd, applied in order:
 * {@link Prefix @Prefix} &rarr;
 * {@code @Suffix} &rarr; {@link Match @Match}
 * &rarr; {@link Filter @Filter}. Each kind only sees entries that
 * passed the previous ones.
 *
 * @see Prefix
 * @see Match
 * @see Filter
 */
@Repeatable(Suffixes.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Suffix {
    String[] value();

    boolean exclude() default false;
}
