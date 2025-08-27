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

import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FuturesTest {

    /**
     * If we pass in false to cancel, all the Futures should see false
     * If we pass in true to cancel, all the Futures should see true
     */
    @Test
    public void cancelMayInterruptIfRunning() {
        final List<Boolean> args = new ArrayList<Boolean>();

        final Future<List<URI>> futures = Futures.of(
                new MockFuture<URI>() {
                    @Override
                    public boolean cancel(final boolean mayInterruptIfRunning) {
                        args.add(mayInterruptIfRunning);
                        return false;
                    }
                },
                new MockFuture<URI>() {
                    @Override
                    public boolean cancel(final boolean mayInterruptIfRunning) {
                        args.add(mayInterruptIfRunning);
                        return true;
                    }
                }
        );

        { // If we pass in false to cancel, all the Futures should see false
            args.clear();
            futures.cancel(false);
            assertEquals("false\n" +
                    "false", Join.join("\n", args));
        }

        { // If we pass in true to cancel, all the Futures should see true
            args.clear();
            futures.cancel(true);
            assertEquals("true\n" +
                    "true", Join.join("\n", args));
        }
    }

    /**
     * All Futures must return true to cancel to get a true result.
     * If one future returns false, the answer should be false
     */
    @Test
    public void cancelAllOrNothing() {
        final Future<List<URI>> falseCancel = Futures.of(
                new MockFuture<URI>() {
                    @Override
                    public boolean cancel(final boolean mayInterruptIfRunning) {
                        return false;
                    }
                },
                new MockFuture<URI>() {
                    @Override
                    public boolean cancel(final boolean mayInterruptIfRunning) {
                        return true;
                    }
                }
        );

        final Future<List<URI>> trueCancel = Futures.of(
                new MockFuture<URI>() {
                    @Override
                    public boolean cancel(final boolean mayInterruptIfRunning) {
                        return true;
                    }
                },
                new MockFuture<URI>() {
                    @Override
                    public boolean cancel(final boolean mayInterruptIfRunning) {
                        return true;
                    }
                }
        );

        assertTrue(trueCancel.cancel(false));
        assertFalse(falseCancel.cancel(false));
    }

    /**
     * All Futures must return true to isCancelled to get a true result.
     * If one future returns false, the answer should be false
     */
    @Test
    public void isCancelled() {
        final Future<List<URI>> falseIsCancelled = Futures.of(
                new MockFuture<URI>() {
                    @Override
                    public boolean isCancelled() {
                        return false;
                    }
                },
                new MockFuture<URI>() {
                    @Override
                    public boolean isCancelled() {
                        return true;
                    }
                }
        );

        final Future<List<URI>> trueIsCancelled = Futures.of(
                new MockFuture<URI>() {
                    @Override
                    public boolean isCancelled() {
                        return true;
                    }
                },
                new MockFuture<URI>() {
                    @Override
                    public boolean isCancelled() {
                        return true;
                    }
                }
        );

        assertTrue(trueIsCancelled.isCancelled());
        assertFalse(falseIsCancelled.isCancelled());
    }

    /**
     * All Futures must return true to isDone to get a true result.
     * If one future returns false, the answer should be false
     */
    @Test
    public void isDone() {
        final Future<List<URI>> falseIsDone = Futures.of(
                new MockFuture<URI>() {
                    @Override
                    public boolean isDone() {
                        return false;
                    }
                },
                new MockFuture<URI>() {
                    @Override
                    public boolean isDone() {
                        return true;
                    }
                }
        );

        final Future<List<URI>> trueIsDone = Futures.of(
                new MockFuture<URI>() {
                    @Override
                    public boolean isDone() {
                        return true;
                    }
                },
                new MockFuture<URI>() {
                    @Override
                    public boolean isDone() {
                        return true;
                    }
                }
        );

        assertTrue(trueIsDone.isDone());
        assertFalse(falseIsDone.isDone());
    }

    @Test
    public void get() throws ExecutionException, InterruptedException {
        final Future<List<URI>> futures = Futures.of(
                new MockFuture<URI>() {
                    @Override
                    public URI get() throws InterruptedException, ExecutionException {
                        return URI.create("color://red");
                    }
                },
                new MockFuture<URI>() {
                    @Override
                    public URI get() throws InterruptedException, ExecutionException {
                        return URI.create("color://green");
                    }
                },
                new MockFuture<URI>() {
                    @Override
                    public URI get() throws InterruptedException, ExecutionException {
                        return URI.create("color://blue");
                    }
                }
        );

        final List<URI> uris = futures.get();

        assertEquals("color://red\n" +
                "color://green\n" +
                "color://blue", Join.join("\n", uris));
    }

    /**
     * As each Future consumes time, the subsequent Futures should see diminishing allowed time
     * 
     */
    @Test
    public void getTimeout() throws ExecutionException, InterruptedException, TimeoutException {
        final List<Duration> times = new ArrayList<>();

        final Future<List<URI>> futures = Futures.of(
                new MockFuture<URI>() {
                    @Override
                    public URI get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        times.add(new Duration(timeout, unit));
                        Thread.sleep(3000);
                        return URI.create("color://red");
                    }
                },
                new MockFuture<URI>() {
                    @Override
                    public URI get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        times.add(new Duration(timeout, unit));
                        Thread.sleep(2000);
                        return URI.create("color://green");
                    }
                },
                new MockFuture<URI>() {
                    @Override
                    public URI get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        times.add(new Duration(timeout, unit));
                        Thread.sleep(1000);
                        return URI.create("color://blue");
                    }
                }
        );

        final List<URI> uris = futures.get(10, TimeUnit.SECONDS);

        assertEquals("color://red\n" +
                "color://green\n" +
                "color://blue", Join.join("\n", uris));

        // This technique of evaluating is a bit suspect.  We'll need to rework it
        // if things turn out to be a bit flakey
        final List<Long> seconds = times.stream()
                .map(duration -> duration.getTime(TimeUnit.SECONDS))
                .collect(Collectors.toList());
        assertEquals("9\n" +
                "6\n" +
                "4", Join.join("\n", seconds));
    }
    /**
     * When there is no time left, a TimeoutException should be thrown and we
     * should stop calling get() on the Futures
     */
    @Test
    public void getTimeoutFailure() throws ExecutionException, InterruptedException, TimeoutException {
        final List<Duration> times = new ArrayList<>();

        final Future<List<URI>> futures = Futures.of(
                new MockFuture<URI>() {
                    @Override
                    public URI get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        times.add(new Duration(timeout, unit));
                        Thread.sleep(1000);
                        return URI.create("color://red");
                    }
                },
                new MockFuture<URI>() {
                    @Override
                    public URI get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        times.add(new Duration(timeout, unit));
                        Thread.sleep(1001);
                        return URI.create("color://green");
                    }
                },
                new MockFuture<URI>() {
                    @Override
                    public URI get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        times.add(new Duration(timeout, unit));
                        return URI.create("color://blue");
                    }
                }
        );

        try {
            futures.get(2, TimeUnit.SECONDS);
            fail("Expected TimeoutException");
        } catch (TimeoutException e) {
            // pass
        }

        // Only two of our Futures should have been called
        assertEquals(2, times.size());
    }


    public static class MockFuture<V> implements Future<V> {
        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isCancelled() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDone() {
            throw new UnsupportedOperationException();
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            throw new UnsupportedOperationException();
        }

        @Override
        public V get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            throw new UnsupportedOperationException();
        }
    }
}
