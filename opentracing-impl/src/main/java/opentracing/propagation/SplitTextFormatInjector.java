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
package opentracing.propagation;

import java.util.Map;

/**
 *
 */
interface SplitTextFormatInjector extends Injector<Map> {
    static final String TRACE_IDENTIFIERS = "Open-Tracing-Context-Id-";
    static final String TRACE_ATTRIBUTES = "Open-Tracing-Trace-Tags-";
}
