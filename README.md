Expiring Cache
==============

Sometimes it is good to have a cache that contains entries that expire. This is 
a simple implementation of such as cache which has served me well in very, very
big applications for several years now.

This component is available in [Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.jolira%22%20AND%20a%3A%22expiring-cache%22).

A usage example (from the tests):

```
    private ExpiringCache<Integer, String> loadOneThousand(final long ttl,
            final int maxSize) {
        final ExpiringCache<Integer, String> map = new ExpiringCache<Integer, String>(
                ttl, maxSize);

        for (int idx = 0; idx < ONETHOUSAND; idx++) {
            map.put(Integer.valueOf(idx), Integer.toHexString(idx));
        }

        return map;
    }
    
    final Map<Integer, String> map1 = loadOneThousand(Integer.MAX_VALUE);
    final String val1 = map1.get(Integer.valueOf(255));

    assertEquals("ff", val1);

    final Map<Integer, String> map2 = loadOneThousand(1);

    Thread.sleep(10);

    final String val2 = map2.get(Integer.valueOf(255));

    assertNull(val2);
```
