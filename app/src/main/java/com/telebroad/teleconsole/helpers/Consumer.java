package com.telebroad.teleconsole.helpers;

@FunctionalInterface
public interface Consumer<T extends Object> {
    void accept(T t);
}

