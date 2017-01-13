/**
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
package io.opentracing.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.opentracing.impl.AbstractSpan.LogData;

public class AbstractSpanTest {

  private final String operationName = "TestOp";
  private final String key = "foo";
  private final String value = "bar";
  
  @Test
  public void ctor() {
    TestSpanImpl span = new TestSpanImpl(operationName);
    assertEquals(operationName, span.getOperationName());
    assertNull(span.baggage);
    assertNotNull(span.getStart());
    assertNull(span.getDuration());
    assertNull(span.tags);
    assertNull(span.logs);
    
    Instant start = Instant.ofEpochSecond(1451606400);
    span = new TestSpanImpl(operationName, start);
    assertEquals(operationName, span.getOperationName());
    assertNull(span.baggage);
    assertSame(start, span.getStart());
    assertNull(span.getDuration());
    assertNull(span.tags);
    assertNull(span.logs);
    
    // empty name
    span = new TestSpanImpl("", start);
    assertEquals("", span.getOperationName());
    assertNull(span.baggage);
    assertSame(start, span.getStart());
    assertNull(span.getDuration());
    assertNull(span.tags);
    assertNull(span.logs);
    
    // null name
    span = new TestSpanImpl(null, start);
    assertNull(span.getOperationName());
    assertNull(span.baggage);
    assertSame(start, span.getStart());
    assertNull(span.getDuration());
    assertNull(span.tags);
    assertNull(span.logs);
    
    // null start. This can't be allowed
    try {
      new TestSpanImpl(operationName, null);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) { }
  }

  @Test
  public void finish() {
    TestSpanImpl span = new TestSpanImpl(operationName);
    assertNull(span.getDuration());
    span.finish();
    assertNotNull(span.getDuration()); 
    // without mocking we can't validate the actual duration time
  }
  
  @Test
  public void finishMicros() {
    Instant start = Instant.ofEpochSecond(1451606400);
    TestSpanImpl span = new TestSpanImpl(operationName, start);
    span.finish(1451606460000000L);
    assertEquals(60000, span.getDuration().toMillis());
  }
  
  @Test
  public void setOperationName() {
    TestSpanImpl span = new TestSpanImpl(operationName);
    assertEquals(operationName, span.getOperationName());
    span.setOperationName("newName");
    assertEquals("newName", span.getOperationName());
    
    span.setOperationName("");
    assertEquals("", span.getOperationName());
    
    span.setOperationName(null);
    assertNull(span.getOperationName());
  }

  @Test
  public void close() {
    TestSpanImpl span = new TestSpanImpl(operationName);
    assertNull(span.getDuration());
    span.close();
    assertNotNull(span.getDuration()); 
    // without mocking we can't validate the actual duration time
  }
  
  @Test
  public void setTagString() {
    TestSpanImpl span = new TestSpanImpl(operationName);
    assertNull(span.tags);
    span.setTag(key, value);
    assertEquals(1, span.tags.size());
    assertEquals(value, span.tags.get(key));
    
    // null value
    span = new TestSpanImpl(operationName);
    assertNull(span.tags);
    span.setTag(key, (String) null);
    assertEquals(1, span.tags.size());
    assertNull(span.tags.get(key));
    
    // null key
    span = new TestSpanImpl(operationName);
    assertNull(span.tags);
    span.setTag(null, value);
    assertEquals(1, span.tags.size());
    assertEquals(value, span.tags.get(null));
    
    // nulls all around!
    span = new TestSpanImpl(operationName);
    assertNull(span.tags);
    span.setTag(null, (String) null);
    assertEquals(1, span.tags.size());
    assertNull(span.tags.get(null));
  }
  
  @Test
  public void setTagBoolean() {
    TestSpanImpl span = new TestSpanImpl(operationName);
    assertNull(span.tags);
    span.setTag(key, true);
    assertEquals(1, span.tags.size());
    assertTrue((boolean) span.tags.get(key));
    
    // null value
    span = new TestSpanImpl(operationName);
    assertNull(span.tags);
    span.setTag(key, false);
    assertEquals(1, span.tags.size());
    assertFalse((boolean) span.tags.get(key));
    
    // null key
    span = new TestSpanImpl(operationName);
    assertNull(span.tags);
    span.setTag(null, true);
    assertEquals(1, span.tags.size());
    assertTrue((boolean) span.tags.get(null));
  }
  
  @Test
  public void setTagNums() {
    TestSpanImpl span = new TestSpanImpl(operationName);
    assertNull(span.tags);
    span.setTag(key, 42);
    assertEquals(1, span.tags.size());
    assertEquals(42, span.tags.get(key));
    
    span = new TestSpanImpl(operationName);
    assertNull(span.tags);
    span.setTag(key, 42.5);
    assertEquals(1, span.tags.size());
    assertEquals(42.5, (double) span.tags.get(key), 0.0001);
    
    // null key
    span = new TestSpanImpl(operationName);
    assertNull(span.tags);
    span.setTag(null, 42);
    assertEquals(1, span.tags.size());
    assertEquals(42, span.tags.get(null));
    
    span = new TestSpanImpl(operationName);
    assertNull(span.tags);
    span.setTag(key, (Number) null);
    assertEquals(1, span.tags.size());
    assertNull(span.tags.get(key));
    
    // nulls all around!
    span = new TestSpanImpl(operationName);
    assertNull(span.tags);
    span.setTag(null, (Number) null);
    assertEquals(1, span.tags.size());
    assertNull(span.tags.get(null));
  }

  @Test
  public void getTags() {
    TestSpanImpl span = new TestSpanImpl(operationName);
    assertTrue(span.getTags().isEmpty());
    
    span.setTag(key, value);
    assertEquals(1, span.getTags().size());
    assertEquals(value, span.getTags().get(key));
    
    try {
      span.getTags().put(key, "Denied");
      fail("Epxected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) { }
  }

  @Test
  public void baggage() {
    TestSpanImpl span = new TestSpanImpl(operationName);
    assertNull(span.baggage);
    assertTrue(span.getBaggage().isEmpty());
    
    span.setBaggageItem(key, value);
    assertEquals(1, span.baggage.size());
    assertEquals(value, span.getBaggage().get(key));
    
    // null value
    span = new TestSpanImpl(operationName);
    span.setBaggageItem(key, null);
    assertNull(span.getBaggage().get(key));
    
    // null key
    span = new TestSpanImpl(operationName);
    span.setBaggageItem(null, value);
    assertEquals(value, span.getBaggage().get(null));
    
    // nulls all around
    span = new TestSpanImpl(operationName);
    span.setBaggageItem(null, null);
    assertNull(span.getBaggage().get(null));
  }
  
  @Test
  public void log() {
    TestSpanImpl span = new TestSpanImpl(operationName);
    assertNull(span.logs);
    span.log("Log1");
    assertEquals(1, span.getLogs().size());
    LogData log = span.getLogs().get(0);
    assertNotNull(log.getTime());
    assertEquals(1, log.getFields().size());
    assertEquals("Log1", log.getFields().get("event"));
    
    // empty string
    span = new TestSpanImpl(operationName);
    assertNull(span.logs);
    span.log("");
    assertEquals(1, span.getLogs().size());
    log = span.getLogs().get(0);
    assertNotNull(log.getTime());
    assertEquals(1, log.getFields().size());
    assertEquals("", log.getFields().get("event"));
    
    // null log entry
    span = new TestSpanImpl(operationName);
    assertNull(span.logs);
    span.log((String) null);
    assertEquals(1, span.getLogs().size());
    log = span.getLogs().get(0);
    assertNotNull(log.getTime());
    assertEquals(1, log.getFields().size());
    assertNull(log.getFields().get("event"));
  }
  
  @Test
  public void logTimestamp() {
    TestSpanImpl span = new TestSpanImpl(operationName);
    assertNull(span.logs);
    span.log(1451606460000000L, "Log1");
    assertEquals(1, span.getLogs().size());
    LogData log = span.getLogs().get(0);
    assertEquals(1451606460, log.getTime().getEpochSecond());
    assertEquals(1, log.getFields().size());
    assertEquals("Log1", log.getFields().get("event"));
    
    // zero time
    span = new TestSpanImpl(operationName);
    assertNull(span.logs);
    span.log(0L, "Log1");
    assertEquals(1, span.getLogs().size());
    log = span.getLogs().get(0);
    assertEquals(0, log.getTime().getEpochSecond());
    assertEquals(1, log.getFields().size());
    assertEquals("Log1", log.getFields().get("event"));
    
    // negative time
    span = new TestSpanImpl(operationName);
    assertNull(span.logs);
    span.log(-1451606460000000L, "Log1");
    assertEquals(1, span.getLogs().size());
    log = span.getLogs().get(0);
    assertEquals(-1451606460, log.getTime().getEpochSecond());
    assertEquals(1, log.getFields().size());
    assertEquals("Log1", log.getFields().get("event"));
  }

  @Test
  public void logFields() {
    final Map<String, Object> fields = new HashMap<>();
    fields.put(key, value);
    fields.put("bytearray", new byte[] { 42 });
    
    TestSpanImpl span = new TestSpanImpl(operationName);
    assertNull(span.logs);
    span.log(fields);
    assertEquals(1, span.getLogs().size());
    LogData log = span.getLogs().get(0);
    assertNotNull(log.getTime());
    assertEquals(2, log.getFields().size());
    assertEquals(value, log.getFields().get(key));
    assertArrayEquals(new byte[] { 42 }, (byte[]) log.getFields().get("bytearray"));
    
    // null fields
    span = new TestSpanImpl(operationName);
    assertNull(span.logs);
    span.log((Map) null);
    assertEquals(1, span.getLogs().size());
    log = span.getLogs().get(0);
    assertNotNull(log.getTime());
    assertTrue(log.getFields().isEmpty());
  }
  
  @Test
  public void logPayload() {
    TestSpanImpl span = new TestSpanImpl(operationName);
    assertNull(span.logs);
    span.log("Log1", new byte[] { 42 });
    assertEquals(1, span.getLogs().size());
    LogData log = span.getLogs().get(0);
    assertNotNull(log.getTime());
    assertEquals(2, log.getFields().size());
    assertEquals("Log1", log.getFields().get("event"));
    assertArrayEquals(new byte[] { 42 }, (byte[]) log.getFields().get("payload"));
    
    // null payload
    span = new TestSpanImpl(operationName);
    assertNull(span.logs);
    span.log("Log1", null);
    assertEquals(1, span.getLogs().size());
    log = span.getLogs().get(0);
    assertNotNull(log.getTime());
    assertEquals(1, log.getFields().size());
    assertEquals("Log1", log.getFields().get("event"));
    assertNull(log.getFields().get("payload"));
  }
}
