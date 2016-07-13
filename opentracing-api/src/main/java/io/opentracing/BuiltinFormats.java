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
package io.opentracing;

import java.nio.ByteBuffer;
import io.opentracing.propagation.TextMapReader;
import io.opentracing.propagation.TextMapWriter;

/**
 * XXX comment
 */
public class BuiltinFormats<R, W> implements Format<R, W> {
    public final static Format<TextMapReader, TextMapWriter> TEXT_MAP = new BuiltinFormats<>();
    public final static Format<TextMapReader, TextMapWriter> HTTP_HEADER = new BuiltinFormats<>();
    public final static Format<ByteBuffer, ByteBuffer> BINARY= new BuiltinFormats<>();
}
