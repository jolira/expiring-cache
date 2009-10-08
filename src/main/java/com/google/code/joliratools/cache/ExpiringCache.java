package com.google.code.joliratools.cache;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A simple thread-safe cache that implements expires entries bases on a time to
 * live.
 * 
 * @author Joachim Kainz
 * 
 * @param <K>
 * @param <V>
 * @see Map
 */
public final class ExpiringCache<K, V> implements Map<K, V> {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<K, CacheEntry<V>> m = new LinkedHashMap<K, CacheEntry<V>>();
    private final int ttl;

    public ExpiringCache(final int ttl) {
        if (ttl <= 0) {
            throw new IllegalArgumentException(
                    "time to live must be greater than 0");
        }

        this.ttl = ttl;
    }

    private void cleanup(final long current) {
        final Collection<Entry<K, CacheEntry<V>>> entries = m.entrySet();
        final Iterator<Entry<K, CacheEntry<V>>> it = entries.iterator();

        if (!it.hasNext()) {
            return;
        }

        final Entry<K, CacheEntry<V>> entry = it.next();
        final CacheEntry<V> wrapper = entry.getValue();

        if (!wrapper.hasExpired(current)) {
            return;
        }

        final K key = entry.getKey();
        final CacheEntry<V> removed = m.remove(key);

        assert removed == wrapper;

        cleanup(current);
    }

    @Override
    public void clear() {
        final Lock l = lock.writeLock();

        l.lock();

        try {
            m.clear();
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean containsKey(final Object key) {
        final Lock l = lock.readLock();

        l.lock();

        try {
            return m.containsKey(key);
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean containsValue(final Object value) {
        final Collection<V> values = values();

        for (final V val : values) {
            if (val == value) {
                return true;
            }

            if (value == null) {
                continue;
            }

            if (value.equals(val)) {
                return true;
            }
        }

        return false;
    }

    /**
     * This method is not (yet) supported.
     * 
     * @throws UnsupportedOperationException
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V get(final Object key) {
        final Lock l = lock.readLock();

        l.lock();

        try {
            final CacheEntry<V> ref = m.get(key);
            final long current = System.currentTimeMillis();

            if (ref == null || ref.hasExpired(current)) {
                return null;
            }

            return ref.get();
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        final Lock l = lock.readLock();

        l.lock();

        try {
            return m.isEmpty();
        } finally {
            l.unlock();
        }
    }

    /**
     * This method is not (yet) supported.
     * 
     * @throws UnsupportedOperationException
     */
    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V put(final K key, final V value) {
        final Lock l = lock.writeLock();

        l.lock();

        try {
            final long current = System.currentTimeMillis();

            cleanup(current);

            final CacheEntry<V> wrapper = new CacheEntry<V>(value, current
                    + ttl);
            final CacheEntry<V> overridden = m.put(key, wrapper);

            if (overridden == null) {
                return null;
            }

            return overridden.get();
        } finally {
            l.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void putAll(final Map<? extends K, ? extends V> other) {
        final Set<?> set = other.entrySet();

        if (set == null) {
            return;
        }

        for (final Object o : set) {
            final Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
            final K key = entry.getKey();
            final V value = entry.getValue();

            put(key, value);
        }
    }

    /**
     * This put operation only put the value into the map if no other,
     * non-expired value currently exists in the map. If this key is already in
     * the map, the existing value is returned instead.
     * 
     * @param key
     *            the key to be used
     * @param value
     *            the new value
     * @return the new value, if there was no existing entry for this key;
     *         otherwise the existing value.
     */
    public V putIfAbsent(final K key, final V value) {
        final V existing = get(key);

        if (existing != null) {
            return existing;
        }

        final Lock l = lock.writeLock();

        l.lock();

        try {
            final long current = System.currentTimeMillis();

            cleanup(current);

            final CacheEntry<V> _existing = m.get(key);

            if (_existing != null) {
                final V val = _existing.get();

                if (val != null) {
                    return val;
                }
            }

            final CacheEntry<V> wrapper = new CacheEntry<V>(value, current
                    + ttl);
            final CacheEntry<V> overridden = m.put(key, wrapper);

            assert overridden == _existing;

            return value;
        } finally {
            l.unlock();
        }
    }

    @Override
    public V remove(final Object key) {
        final Lock l = lock.writeLock();

        l.lock();

        try {
            final long current = System.currentTimeMillis();

            cleanup(current);

            final CacheEntry<V> removed = m.remove(key);

            if (removed == null || removed.hasExpired(current)) {
                return null;
            }

            return removed.get();
        } finally {
            l.unlock();
        }
    }

    @Override
    public int size() {
        final Lock l = lock.readLock();

        l.lock();

        try {
            return m.size();
        } finally {
            l.unlock();
        }
    }

    /**
     * This method is not (yet) supported.
     * 
     * @throws UnsupportedOperationException
     */
    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }
}
