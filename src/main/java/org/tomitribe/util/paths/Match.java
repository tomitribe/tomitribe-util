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
 * matches the given regular expression. The <b>entire</b> name must match
 * (implied {@code ^} and {@code $}), as with {@link java.util.regex.Matcher#matches()}.
 *
 * <p>{@code @Match} is repeatable. When {@link #exclude()} is {@code false}
 * (the default), the annotation <b>includes</b> entries matching the pattern.
 * When {@code exclude = true}, it <b>excludes</b> entries matching the pattern.
 * Includes are applied before excludes, so you can combine them:
 *
 * <pre>{@code
 * public interface Reports extends Dir {
 *     @Match("daily-\\d{4}-\\d{2}-\\d{2}\\.csv")
 *     Stream<Path> dailyReports();
 *
 *     @Match(".*\\.(jpg|png)")
 *     Stream<Path> images();
 *
 *     // All CSS files except reset.css
 *     @Match(".*\\.css")
 *     @Match(value = "reset\\.css", exclude = true)
 *     Stream<Path> cssExceptReset();
 * }
 * }</pre>
 *
 * <h3>Evaluation order</h3>
 *
 * <p>When combined with {@link Filter @Filter}, annotations are applied in
 * order: {@code @Match} includes &rarr; {@code @Match} excludes &rarr;
 * {@code @Filter}. Each filter only sees entries that passed the previous ones.
 *
 * @see Filter
 */
@Repeatable(Matches.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Match {
    String value();

    boolean exclude() default false;
}
