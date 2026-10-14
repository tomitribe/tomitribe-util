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
 * starts with one of the given prefixes. Multiple values are OR'd &mdash;
 * an entry matches if its name starts with <b>any</b> of them.
 *
 * <p>This is the cheapest of the name filters and is applied first.
 * For matching on file extension use {@link Suffix @Suffix}, for more
 * complex name patterns use {@link Match @Match}, and for arbitrary
 * criteria use {@link Filter @Filter}.
 *
 * <p>{@code @Prefix} is repeatable. When {@link #exclude()} is
 * {@code false} (the default), the annotation <b>includes</b> entries
 * matching any of its prefixes. When {@code exclude = true}, it
 * <b>excludes</b> entries matching any of its prefixes. Includes are
 * applied before excludes, so you can combine them:
 *
 * <pre>
 * public interface Tomcat extends Dir {
 *     // Only version directories starting with "10."
 *     // Matches 10.1.18/, 10.1.19/, etc.
 *     // Skips 9.0.85/, 11.0.0/, etc.
 *     &#64;Prefix("10.")
 *     Stream&lt;Path&gt; version10();
 *
 *     &#64;Prefix({"10.", "11."})
 *     Stream&lt;Path&gt; modern();
 *
 *     // All 10.x versions except milestones
 *     &#64;Prefix("10.")
 *     &#64;Prefix(value = "10.1.0-M", exclude = true)
 *     Stream&lt;Path&gt; version10Releases();
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
 * {@code @Prefix} &rarr;
 * {@link Suffix @Suffix} &rarr; {@link Match @Match}
 * &rarr; {@link Filter @Filter}. Each kind only sees entries that
 * passed the previous ones.
 *
 * @see Suffix
 * @see Match
 * @see Filter
 */
@Repeatable(Prefixes.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Prefix {
    String[] value();

    boolean exclude() default false;
}
