package com.pavel.queueorganizer;

@FunctionalInterface
public interface QuadConsumer<T, R, M, N> {
    void accept(T t, R r, M m, N n);
}
