/**
 * (C) 2009 jolira (http://www.jolira.com). Licensed under the GNU General
 * Public License, Version 3.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * http://www.gnu.org/licenses/gpl-3.0-standalone.html Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.google.code.joliratools.cache;

import java.io.Serializable;
import java.lang.ref.SoftReference;

/**
 * The entry used to store the entry as well as the time the entry expires. This
 * class has become public in version 1.0.16 of this library as users would like
 * to inherit from it. My strong recommendation would be not to inherit from this
 * class but use aggregation instead.
 * 
 * @author jfk
 * @since 1.0
 *
 * @param <V>
 */
public class CacheEntry<V> implements Serializable {
    private static final long serialVersionUID = -4063049639315804932L;

    final transient SoftReference<V> ref;
    final transient long expiration;

    /**
     * Create a new entry.
     * 
     * @param value the value
     * @param expiration the time the value expires
     */
    public CacheEntry(final V value, final long expiration) {
        ref = new SoftReference<V>(value);
        this.expiration = expiration;
    }

    V get() {
        if (ref == null) {
            return null;
        }

        return ref.get();
    }

    boolean hasExpired(final long current) {
        return expiration < current;
    }
}