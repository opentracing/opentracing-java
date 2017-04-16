package io.opentracing;

public interface NoopSpanSnapshot {
    static final NoopSpanSnapshotImpl INSTANCE = new NoopSpanSnapshotImpl();
}

final class NoopSpanSnapshotImpl implements NoopSpanSnapshot {

}