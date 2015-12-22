/**
 * Copyright 2015 The OpenTracing Authors
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

public abstract class Tags {
  // XXX: This setTag/getTag setup is not the right way to do this in Java circa 2015: the below is morally equivalent to the Golang version, nothing more, nothing less.
  public abstract Tags setTag(String key, Object value);

  public abstract Object getTag(String key);
}
