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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Instant;
import java.util.Map.Entry;

import org.junit.Test;

import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;

public class AbstractSpanBuilderTest {
  private final String operationName = "TestOp";
  private final String key = "foo";
  private final String value = "bar";
  
  @Test
  public void ctor() {
    TestSpanBuilder builder = new TestSpanBuilder(operationName);
    assertEquals(operationName, builder.operationName);
    assertNull(builder.references);
    assertNotNull(builder.start);
    
    // TODO - allow these?
    builder = new TestSpanBuilder("");
    assertEquals("", builder.operationName);
    
    builder = new TestSpanBuilder(null);
    assertNull(builder.operationName);
  }
  
  @Test
  public void addReference() {
    SpanContext spanContext = new TestSpanContext();
    
    TestSpanBuilder builder = new TestSpanBuilder(operationName);
    assertNull(builder.references);
    assertSame(builder, builder.addReference(References.CHILD_OF, spanContext));
    assertEquals(1, builder.references.size());
    assertEquals(References.CHILD_OF, builder.references.get(0).getReferenceType());
    assertSame(spanContext, builder.references.get(0).getReferredTo());
    assertFalse(builder.baggageItems().iterator().hasNext());
    
    // null context - no check at present
    builder = new TestSpanBuilder(operationName);
    assertNull(builder.references);
    assertSame(builder, builder.addReference(References.CHILD_OF, null));
    assertEquals(1, builder.references.size());
    assertEquals(References.CHILD_OF, builder.references.get(0).getReferenceType());
    assertNull(builder.references.get(0).getReferredTo());
    assertFalse(builder.baggageItems().iterator().hasNext());
    
    // null type - no check at present
    builder = new TestSpanBuilder(operationName);
    assertNull(builder.references);
    assertSame(builder, builder.addReference(null, spanContext));
    assertEquals(1, builder.references.size());
    assertNull(builder.references.get(0).getReferenceType());
    assertSame(spanContext, builder.references.get(0).getReferredTo());
    assertFalse(builder.baggageItems().iterator().hasNext());
    
    // null both - no checks at present. Just creates an empty reference entry
    builder = new TestSpanBuilder(operationName);
    assertNull(builder.references);
    assertSame(builder, builder.addReference(null, null));
    assertEquals(1, builder.references.size());
    assertNull(builder.references.get(0).getReferenceType());
    assertNull(builder.references.get(0).getReferredTo());
    assertFalse(builder.baggageItems().iterator().hasNext());
  }

  @Test
  public void asChildOfContext() {
    TestSpanContext spanContext = new TestSpanContext();
    
    TestSpanBuilder builder = new TestSpanBuilder(operationName);
    assertNull(builder.references);
    assertFalse(builder.baggageItems().iterator().hasNext());
    assertSame(builder, builder.asChildOf(spanContext));
    assertEquals(1, builder.references.size());
    assertEquals(References.CHILD_OF, builder.references.get(0).getReferenceType());
    assertSame(spanContext, builder.references.get(0).getReferredTo());
    assertFalse(builder.baggageItems().iterator().hasNext());
    
    // with baggage
    spanContext.baggage.put(key, value);
    builder = new TestSpanBuilder(operationName);
    assertNull(builder.references);
    assertFalse(builder.baggageItems().iterator().hasNext());
    assertSame(builder, builder.asChildOf(spanContext));
    assertEquals(1, builder.references.size());
    assertEquals(References.CHILD_OF, builder.references.get(0).getReferenceType());
    assertSame(spanContext, builder.references.get(0).getReferredTo());
    final Entry<String, String> entry = builder.baggageItems().iterator().next();
    assertEquals(key, entry.getKey());
    assertEquals(value, entry.getValue());
    
    // noop
    builder = new TestSpanBuilder(operationName);
    assertNull(builder.references);
    assertFalse(builder.baggageItems().iterator().hasNext());
    assertSame(NoopSpanBuilder.INSTANCE, builder.asChildOf(NoopSpanBuilder.INSTANCE));
    assertNull(builder.references);
    assertFalse(builder.baggageItems().iterator().hasNext());
    
    // two parents TODO - is this valid?
    spanContext.baggage.clear();
    TestSpanContext spanContext2 = new TestSpanContext();
    builder = new TestSpanBuilder(operationName);
    assertNull(builder.references);
    assertFalse(builder.baggageItems().iterator().hasNext());
    assertSame(builder, builder.asChildOf(spanContext));
    assertSame(builder, builder.asChildOf(spanContext2));
    assertEquals(2, builder.references.size());
    assertEquals(References.CHILD_OF, builder.references.get(0).getReferenceType());
    assertSame(spanContext, builder.references.get(0).getReferredTo());
    assertEquals(References.CHILD_OF, builder.references.get(1).getReferenceType());
    assertSame(spanContext2, builder.references.get(1).getReferredTo());
    assertFalse(builder.baggageItems().iterator().hasNext());
    
    // NPE
    builder = new TestSpanBuilder(operationName);
    try {
      builder.asChildOf((SpanContext) null);
      fail("Epxected NullPointerException");
    } catch (NullPointerException npee) { } 
  }
  
  @Test
  public void asChildOfSpan() {
    TestSpanImpl span = new TestSpanImpl("Dummy");
    TestSpanBuilder builder = new TestSpanBuilder(operationName);
    assertNull(builder.references);
    assertFalse(builder.baggageItems().iterator().hasNext());
    assertSame(builder, builder.asChildOf((Span) span));
    assertEquals(1, builder.references.size());
    assertEquals(References.CHILD_OF, builder.references.get(0).getReferenceType());
    assertFalse(builder.baggageItems().iterator().hasNext());
    
    // with baggage
    span.setBaggageItem(key, value);
    builder = new TestSpanBuilder(operationName);
    assertNull(builder.references);
    assertFalse(builder.baggageItems().iterator().hasNext());
    assertSame(builder, builder.asChildOf((Span) span));
    assertEquals(1, builder.references.size());
    assertEquals(References.CHILD_OF, builder.references.get(0).getReferenceType());
    final Entry<String, String> entry = builder.baggageItems().iterator().next();
    assertEquals(key, entry.getKey());
    assertEquals(value, entry.getValue());
    
    // noop
    builder = new TestSpanBuilder(operationName);
    assertNull(builder.references);
    assertFalse(builder.baggageItems().iterator().hasNext());
    assertSame(NoopSpanBuilder.INSTANCE, builder.asChildOf((Span) NoopSpan.INSTANCE));
    assertNull(builder.references);
    assertFalse(builder.baggageItems().iterator().hasNext());
    
    // NPE
    builder = new TestSpanBuilder(operationName);
    try {
      builder.asChildOf((Span) null);
      fail("Epxected NullPointerException");
    } catch (NullPointerException npee) { } 
  }
  
  @Test
  public void withTagString() {
    TestSpanBuilder builder = new TestSpanBuilder(operationName);
    assertNull(builder.stringTags);
    assertNull(builder.booleanTags);
    assertNull(builder.numberTags);
    assertSame(builder, builder.withTag(key, value));
    assertEquals(1, builder.stringTags.size());
    assertNull(builder.booleanTags);
    assertNull(builder.numberTags);
    assertEquals(value, builder.stringTags.get(key));
    
    // null value
    builder = new TestSpanBuilder(operationName);
    assertNull(builder.stringTags);
    assertNull(builder.booleanTags);
    assertNull(builder.numberTags);
    assertSame(builder, builder.withTag(key, (String) null));
    assertEquals(1, builder.stringTags.size());
    assertNull(builder.booleanTags);
    assertNull(builder.numberTags);
    assertNull(builder.stringTags.get(key));
    
    // null key (*sigh* HashMap lets you store null keys)
    builder = new TestSpanBuilder(operationName);
    assertNull(builder.stringTags);
    assertSame(builder, builder.withTag(null, value));
    assertEquals(1, builder.stringTags.size());
    assertNull(builder.booleanTags);
    assertNull(builder.numberTags);
    assertEquals(value, builder.stringTags.get(null));
    
    // nulls all around
    builder = new TestSpanBuilder(operationName);
    assertNull(builder.stringTags);
    assertSame(builder, builder.withTag(null, (String) null));
    assertEquals(1, builder.stringTags.size());
    assertNull(builder.booleanTags);
    assertNull(builder.numberTags);
    assertNull(builder.stringTags.get(null));
  }
  
  @Test
  public void withTagBoolean() {
    TestSpanBuilder builder = new TestSpanBuilder(operationName);
    assertNull(builder.stringTags);
    assertNull(builder.booleanTags);
    assertNull(builder.numberTags);
    assertSame(builder, builder.withTag(key, true));
    assertNull(builder.stringTags);
    assertEquals(1, builder.booleanTags.size());
    assertNull(builder.numberTags);
    assertTrue(builder.booleanTags.get(key));
    
    builder = new TestSpanBuilder(operationName);
    assertNull(builder.booleanTags);
    assertSame(builder, builder.withTag(key, false));
    assertNull(builder.stringTags);
    assertEquals(1, builder.booleanTags.size());
    assertNull(builder.numberTags);
    assertFalse(builder.booleanTags.get(key));
    
    // null key
    builder = new TestSpanBuilder(operationName);
    assertNull(builder.booleanTags);
    assertSame(builder, builder.withTag(null, false));
    assertNull(builder.stringTags);
    assertEquals(1, builder.booleanTags.size());
    assertNull(builder.numberTags);
    assertFalse(builder.booleanTags.get(null));
  }
  
  @Test
  public void withTagNumber() {
    TestSpanBuilder builder = new TestSpanBuilder(operationName);
    assertNull(builder.stringTags);
    assertNull(builder.booleanTags);
    assertNull(builder.numberTags);
    assertSame(builder, builder.withTag(key, 42));
    assertNull(builder.stringTags);
    assertNull(builder.booleanTags);
    assertEquals(1, builder.numberTags.size());
    assertEquals(42, builder.numberTags.get(key));
    
    builder = new TestSpanBuilder(operationName);
    assertSame(builder, builder.withTag(key, 42.5));
    assertNull(builder.stringTags);
    assertNull(builder.booleanTags);
    assertEquals(1, builder.numberTags.size());
    assertEquals(42.5, (double) builder.numberTags.get(key), 0.0001);
    
    // null value
    builder = new TestSpanBuilder(operationName);
    assertSame(builder, builder.withTag(key, (Number) null));
    assertNull(builder.stringTags);
    assertNull(builder.booleanTags);
    assertEquals(1, builder.numberTags.size());
    assertNull(builder.numberTags.get(null));
    
    // null key
    builder = new TestSpanBuilder(operationName);
    assertSame(builder, builder.withTag(null, 42));
    assertNull(builder.stringTags);
    assertNull(builder.booleanTags);
    assertEquals(1, builder.numberTags.size());
    assertEquals(42, builder.numberTags.get(null));
    
    // nulls all around
    builder = new TestSpanBuilder(operationName);
    assertSame(builder, builder.withTag(null, (Number) null));
    assertNull(builder.stringTags);
    assertNull(builder.booleanTags);
    assertEquals(1, builder.numberTags.size());
    assertNull(builder.numberTags.get(null));
  }

  @Test
  public void withStartTimestamp() {
    TestSpanBuilder builder = new TestSpanBuilder(operationName);
    Instant start = builder.start;
    assertSame(builder, builder.withStartTimestamp(0));
    assertNotSame(start, builder.start);
    assertEquals(0, builder.start.getEpochSecond());
    
    builder = new TestSpanBuilder(operationName);
    start = builder.start;
    assertSame(builder, builder.withStartTimestamp(1451606400000050L));
    assertNotSame(start, builder.start);
    assertEquals(1451606400, builder.start.getEpochSecond());
    assertEquals(50000, builder.start.getNano());
    
    builder = new TestSpanBuilder(operationName);
    start = builder.start;
    assertSame(builder, builder.withStartTimestamp(-1451606400000050L));
    assertNotSame(start, builder.start);
    assertEquals(-1451606401, builder.start.getEpochSecond());
    assertEquals(999950000L, builder.start.getNano());
  }
  
  @Test
  public void withBaggageItem() {
    TestSpanBuilder builder = new TestSpanBuilder(operationName);
    assertNull(builder.baggage);
    assertSame(builder, builder.withBaggageItem(key, value));
    assertEquals(1, builder.baggage.size());
    assertEquals(value, builder.baggage.get(key));
    
    // null value
    builder = new TestSpanBuilder(operationName);
    assertNull(builder.baggage);
    assertSame(builder, builder.withBaggageItem(key, null));
    assertEquals(1, builder.baggage.size());
    assertNull(builder.baggage.get(key));
    
    // null key
    builder = new TestSpanBuilder(operationName);
    assertNull(builder.baggage);
    assertSame(builder, builder.withBaggageItem(null, value));
    assertEquals(1, builder.baggage.size());
    assertEquals(value, builder.baggage.get(null));
    
    builder = new TestSpanBuilder(operationName);
    assertNull(builder.baggage);
    assertSame(builder, builder.withBaggageItem(null, null));
    assertEquals(1, builder.baggage.size());
    assertNull(builder.baggage.get(null));
  }

  @Test
  public void baggageItems() {
    TestSpanBuilder builder = new TestSpanBuilder(operationName);
    assertNull(builder.baggage);
    assertFalse(builder.baggageItems().iterator().hasNext());
    
    assertSame(builder, builder.withBaggageItem(key, value));
    assertEquals(value, builder.baggageItems().iterator().next().getValue());
  }

  @Test
  public void start() {
    TestSpanBuilder builder = new TestSpanBuilder(operationName);
    AbstractSpan span = (AbstractSpan) builder.start();
    assertSame(span, span.context());
    assertNull(span.baggage);
    assertTrue(span.getTags().isEmpty());
    assertNull(span.logs);
    
    builder = new TestSpanBuilder(operationName);
    builder.withTag(key, value);
    builder.withTag("bool", true);
    builder.withTag("num", 42);
    span = (AbstractSpan) builder.start();
    assertSame(span, span.context());
    assertNull(span.baggage);
    assertEquals(3, span.tags.size());
    assertEquals(value, span.tags.get(key));
    assertTrue((boolean) span.tags.get("bool"));
    assertEquals(42, (int) span.tags.get("num"));
    assertNull(span.logs);
    
    builder = new TestSpanBuilder(operationName);
    builder.withBaggageItem(key, value);
    span = (AbstractSpan) builder.start();
    assertSame(span, span.context());
    assertEquals(1, span.baggage.size());
    assertEquals(value, span.getBaggageItem(key));
    assertNull(span.tags);
    assertNull(span.logs);
  }

}
