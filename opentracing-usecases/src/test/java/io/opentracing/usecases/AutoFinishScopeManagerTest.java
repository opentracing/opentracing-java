/*
 * Copyright 2016-2017 The OpenTracing Authors
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
package io.opentracing.usecases;

import io.opentracing.Scope;
import io.opentracing.Span;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AutoFinishScopeManagerTest {
    private AutoFinishScopeManager autoFinishActivator;

    @Before
    public void before() throws Exception {
        autoFinishActivator = new AutoFinishScopeManager();
    }

    @Test
    public void missingActiveSpan() throws Exception {
        Scope missingScope = autoFinishActivator.activeScope();
        Assert.assertNull(missingScope);
    }

    @Test
    public void makeActiveSpan() throws Exception {
        Span span = Mockito.mock(Span.class);

        try (AutoFinishScopeManager.AutoFinishScope scope = autoFinishActivator.activate(span)) {
            Assert.assertNotNull(scope);
            Scope otherScope = autoFinishActivator.activeScope();
            Assert.assertEquals(otherScope, scope);
        }

        // Make sure the Span got finish()ed.
        Mockito.verify(span).finish();

        // And now it's gone:
        Scope missingScope = autoFinishActivator.activeScope();
        Assert.assertNull(missingScope);
    }

    @Test
    public void deferring() throws Exception {
        Span span = Mockito.mock(Span.class);

        AutoFinishScopeManager.AutoFinishScope.Continuation continuation = null;
        {
            // We can't use 1.7 features like try-with-resources in this repo without meddling with pom details for tests.
            try (AutoFinishScopeManager.AutoFinishScope scope = autoFinishActivator.activate(span)) {
                // Take a reference...
                continuation = scope.defer();
                Assert.assertNotNull(scope);
                Scope otherScope = autoFinishActivator.activeScope();
                Assert.assertEquals(otherScope, scope);
            }
        }

        // Make sure the Span has not been finish()ed yet.
        Mockito.verify(span, Mockito.never()).finish();

        // Activate the Continuation and close that second reference.
        try (AutoFinishScopeManager.AutoFinishScope reactivated = continuation.activate()) {
            // Nothing to do.
        }

        // Make sure the Span got finish()ed this time.
        Mockito.verify(span).finish();

        // And now it's gone:
        Scope missingScope = autoFinishActivator.activeScope();
        Assert.assertNull(missingScope);
    }

}
