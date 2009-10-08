package com.google.code.joliratools.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

public class ExpiringCacheTest {
    private static final int ONETHOUSAND = 1000;

    private ExpiringCache<Integer, String> loadOneThousand(final int ttl) {
        final ExpiringCache<Integer, String> map = new ExpiringCache<Integer, String>(
                ttl);

        for (int idx = 0; idx < ONETHOUSAND; idx++) {
            map.put(Integer.valueOf(idx), Integer.toHexString(idx));
        }

        return map;
    }

    @Test
    public void testClear() {
        final Map<Integer, String> map = loadOneThousand(Integer.MAX_VALUE);

        assertEquals(ONETHOUSAND, map.size());

        map.clear();

        assertEquals(0, map.size());
    }

    @Test
    public void testContainsKey() {
        final Map<Integer, String> map = loadOneThousand(Integer.MAX_VALUE);

        assertTrue(map.containsKey(Integer.valueOf(255)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testContainsValue() {
        final Map<Integer, String> map = loadOneThousand(Integer.MAX_VALUE);

        assertTrue(map.containsValue("255"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEntrySet() {
        final Map<Integer, String> map = loadOneThousand(Integer.MAX_VALUE);

        map.entrySet();
    }

    @Test
    public void testGet() throws InterruptedException {
        final Map<Integer, String> map1 = loadOneThousand(Integer.MAX_VALUE);
        final String val1 = map1.get(Integer.valueOf(255));

        assertEquals("ff", val1);

        final Map<Integer, String> map2 = loadOneThousand(1);

        Thread.sleep(10);

        final String val2 = map2.get(Integer.valueOf(255));

        assertNull(val2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidArgument() {
        new ExpiringCache<Object, Object>(-1);
    }

    @Test
    public void testIsEmpty() {
        final Map<Integer, String> map = loadOneThousand(Integer.MAX_VALUE);

        assertFalse(map.isEmpty());
        map.clear();
        assertTrue(map.isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testKeySet() {
        final Map<Integer, String> map = loadOneThousand(1);

        map.keySet();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPutAll() {
        final Map<Integer, String> map1 = loadOneThousand(Integer.MAX_VALUE);
        final Map<Integer, String> map2 = loadOneThousand(1);

        map2.putAll(map1);
    }

    @Test
    public void testPutIfAbsent() {
        final ExpiringCache<Integer, String> map = loadOneThousand(Integer.MAX_VALUE);

        assertEquals(ONETHOUSAND, map.size());

        final int fivehundred = ONETHOUSAND / 2;

        for (int idx = fivehundred; idx < ONETHOUSAND + fivehundred; idx++) {
            final String value = "jolira-" + Integer.toString(idx);
            final String chosen = map.putIfAbsent(Integer.valueOf(idx), value);

            if (idx < ONETHOUSAND) {
                assertEquals(Integer.toHexString(idx), chosen);
            } else {
                assertEquals(value, chosen);
            }
        }
    }

    @Test
    public void testRemove() {
        final Map<Integer, String> map = loadOneThousand(Integer.MAX_VALUE);

        for (int idx = ONETHOUSAND - 1; idx >= 0; idx--) {
            final String removed = map.remove(Integer.valueOf(idx));

            assertEquals(Integer.toHexString(idx), removed);
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testValues() {
        final Map<Integer, String> map = loadOneThousand(Integer.MAX_VALUE);

        map.values();
    }

}
