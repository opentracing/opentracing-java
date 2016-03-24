/**
 * Copyright 2016 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package opentracing;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PlaygroundTest {

  @Test
  public void playground() {

    // Eventhough src/main is Java 7, we can use Java 8 types in tests
    CompletableFuture<String> fooCompleted = CompletableFuture.completedFuture("foo");
    assertThat(fooCompleted).isCompleted();

    assertThat(Arrays.asList("foo", "bar"))
        .filteredOn("bar"::equals) // We can use method references
        .filteredOn(e -> !e.equals("foo")) // We can also use lambdas
        .containsOnly("bar");

  }
}
