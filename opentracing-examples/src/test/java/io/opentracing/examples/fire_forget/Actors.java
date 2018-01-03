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
package io.opentracing.examples.fire_forget;

import io.opentracing.Span;
import io.opentracing.tag.Tags;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Actors
{
    public static class WorkerActor extends Actor
    {
        Actor nextActor;

        public WorkerActor(Actor nextActor) {
            this.nextActor = nextActor;
        }

        @Override
        public void receive(Object message) {
            Map<String, Object> map = (Map<String, Object>)message;

            Map<String, Object> nextMessage = new TreeMap<String, Object>();
            nextMessage.put("span", (Span)map.get("span"));
            nextMessage.put("working.value", (int)map.get("working.value") * 2);
            nextActor.tell(nextMessage);
        }
    }

    public static class ErrorActor extends Actor
    {
        @Override
        public void receive(Object message) {
            Map<String, Object> map = (Map<String, Object>)message;
            Span span = (Span)map.get("span");
            span.setTag(Tags.ERROR.getKey(), Boolean.TRUE);
            span.setTag("return.value", (int)map.get("error.code"));
            span.finish();
        }
    }

    public static class FinishActor extends Actor
    {
        @Override
        public void receive(Object message) {
            Map<String, Object> map = (Map<String, Object>)message;
            Span span = (Span)map.get("span");
            span.setTag("return.value", (int)map.get("working.value"));
            span.finish();
        }
    }
}
