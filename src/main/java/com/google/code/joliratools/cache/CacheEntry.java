/**
 * 
 */
package com.google.code.joliratools.cache;

import java.lang.ref.SoftReference;

class CacheEntry<V> {
    final SoftReference<V> ref;
    final long expiration;

    CacheEntry(final V value, final long expiration) {
        ref = new SoftReference<V>(value);
        this.expiration = expiration;
    }

    V get() {
        return ref.get();
    }

    boolean hasExpired(final long current) {
        return expiration < current;
    }
}