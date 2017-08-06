package io.opentracing;

import java.io.Closeable;

/**
 * Created by bhs on 7/27/17.
 */
public interface Activator {
     interface Scope extends Closeable {
          @Override
          void close();

          Span span();
     }
     Scope activate(Span span);

     Scope activeScope();
}
