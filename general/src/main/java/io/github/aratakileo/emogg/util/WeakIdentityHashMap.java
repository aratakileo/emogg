package io.github.aratakileo.emogg.util;

//package com.github.gfx.util;
// SOURCE: https://github.com/gfx/Java-WeakIdentityHashMap/blob/master/library/src/main/java/com/github/gfx/util/WeakIdentityHashMap.java

// This is a fork of https://android.googlesource.com/platform/libcore/+/master/luni/src/main/java/java/util/WeakIdentityHashMap.java

/*
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements. See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership. The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License. You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied. See the License for the
  specific language governing permissions and limitations
  under the License.
 */

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * WeakIdentityHashMap is an implementation of IdentityHashMap with keys which are WeakReferences. A
 * key/value mapping is removed when the key is no longer referenced. All
 * optional operations (adding and removing) are supported. Keys and values can
 * be any objects. Note that the garbage collector acts similar to a second
 * thread on this collection, possibly removing keys.
 *
 * @see java.util.IdentityHashMap
 * @see java.util.WeakHashMap
 * @see java.lang.ref.WeakReference
 */
@SuppressWarnings("ALL")
public class WeakIdentityHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V> {
    private static final int DEFAULT_SIZE = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private final ReferenceQueue<K> referenceQueue;
    private int elementCount;
    private Entry<K, V>[] elementData;
    private final int loadFactor;
    private int threshold;
    private volatile int modCount;

    private Set<K> keySet;
    private Collection<V> valuesCollection;

    // Simple utility method to isolate unchecked cast for array creation
    @SuppressWarnings("unchecked")
    private static <K, V> Entry<K, V>[] newEntryArray(int size) {
        return new Entry[size];
    }

    private static final class Entry<K, V> extends WeakReference<K> implements
            Map.Entry<K, V> {
        final int hash;
        boolean isNull;
        V value;
        Entry<K, V> next;

        interface Type<R, K, V> {
            R get(Map.Entry<K, V> entry);
        }

        Entry(K key, V object, ReferenceQueue<K> queue) {
            super(key, queue);
            isNull = key == null;
            hash = isNull ? 0 : System.identityHashCode(key);
            value = object;
        }

        @Override
        public K getKey() {
            return super.get();
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V object) {
            V result = value;
            value = object;
            return result;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) other;
            Object key = super.get();
            return (key == entry.getKey())
                    && (value == null ? value == entry.getValue() : value
                    .equals(entry.getValue()));
        }

        @Override
        public int hashCode() {
            return hash + (value == null ? 0 : value.hashCode());
        }

        @Override
        public String toString() {
            return super.get() + "=" + value;
        }
    }

    private class HashIterator<R> implements Iterator<R> {
        private int position = 0, expectedModCount;
        private Entry<K, V> currentEntry, nextEntry;
        private K nextKey;
        private final Entry.Type<R, K, V> type;

        HashIterator(Entry.Type<R, K, V> type) {
            this.type = type;
            expectedModCount = modCount;
        }

        @Override
        public boolean hasNext() {
            if (nextEntry != null && (nextKey != null || nextEntry.isNull)) {
                return true;
            }
            while (true) {
                if (nextEntry == null) {
                    while (position < elementData.length) {
                        if ((nextEntry = elementData[position++]) != null) {
                            break;
                        }
                    }
                    if (nextEntry == null) {
                        return false;
                    }
                }
                // ensure key of next entry is not gc'ed
                nextKey = nextEntry.get();
                if (nextKey != null || nextEntry.isNull) {
                    return true;
                }
                nextEntry = nextEntry.next;
            }
        }

        @Override
        public R next() {
            if (expectedModCount == modCount) {
                if (hasNext()) {
                    currentEntry = nextEntry;
                    nextEntry = currentEntry.next;
                    R result = type.get(currentEntry);
                    // free the key
                    nextKey = null;
                    return result;
                }
                throw new NoSuchElementException();
            }
            throw new ConcurrentModificationException();
        }

        @Override
        public void remove() {
            if (expectedModCount == modCount) {
                if (currentEntry != null) {
                    removeEntry(currentEntry);
                    currentEntry = null;
                    expectedModCount++;
                    // cannot poll() as that would change the expectedModCount
                } else {
                    throw new IllegalStateException();
                }
            } else {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Constructs a new empty {@code WeakIdentityHashMap} instance.
     */
    public WeakIdentityHashMap() {
        this(DEFAULT_SIZE);
    }

    /**
     * Constructs a new {@code WeakIdentityHashMap} instance with the specified
     * capacity.
     *
     * @param capacity the initial capacity of this map.
     * @throws IllegalArgumentException if the capacity is less than zero.
     */
    public WeakIdentityHashMap(int capacity) {
        this(capacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new {@code WeakIdentityHashMap} instance with the specified capacity
     * and load factor.
     *
     * @param capacity   the initial capacity of this map.
     * @param loadFactor the initial load factor.
     * @throws IllegalArgumentException if the capacity is less than zero or the load factor is less
     *                                  or equal to zero.
     */
    public WeakIdentityHashMap(int capacity, float loadFactor) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity < 0: " + capacity);
        }
        if (loadFactor <= 0) {
            throw new IllegalArgumentException("loadFactor <= 0: " + loadFactor);
        }
        elementCount = 0;
        elementData = newEntryArray(capacity == 0 ? 1 : capacity);
        this.loadFactor = (int) (loadFactor * 10000);
        computeMaxSize();
        referenceQueue = new ReferenceQueue<K>();
    }

    /**
     * Constructs a new {@code WeakIdentityHashMap} instance containing the mappings
     * from the specified map.
     *
     * @param map the mappings to add.
     */
    public WeakIdentityHashMap(Map<? extends K, ? extends V> map) {
        this(map.size() * 2);
        putAll(map);
    }

    /**
     * Removes all mappings from this map, leaving it empty.
     *
     * @see #isEmpty()
     * @see #size()
     */
    @Override
    public void clear() {
        if (elementCount > 0) {
            elementCount = 0;
            Arrays.fill(elementData, null);
            modCount++;
            while (referenceQueue.poll() != null) {
                // do nothing
            }
        }
    }

    private void computeMaxSize() {
        threshold = (int) ((long) elementData.length * loadFactor / 10000);
    }

    /**
     * Returns whether this map contains the specified key.
     *
     * @param key the key to search for.
     * @return {@code true} if this map contains the specified key,
     * {@code false} otherwise.
     */
    @Override
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    /**
     * Returns a set containing all of the mappings in this map. Each mapping is
     * an instance of {@link Map.Entry}. As the set is backed by this map,
     * changes in one will be reflected in the other. It does not support adding
     * operations.
     *
     * @return a set of the mappings.
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        poll();
        return new AbstractSet<Map.Entry<K, V>>() {
            @Override
            public int size() {
                return WeakIdentityHashMap.this.size();
            }

            @Override
            public void clear() {
                WeakIdentityHashMap.this.clear();
            }

            @Override
            public boolean remove(Object object) {
                if (contains(object)) {
                    WeakIdentityHashMap.this
                            .remove(((Map.Entry<?, ?>) object).getKey());
                    return true;
                }
                return false;
            }

            @Override
            public boolean contains(Object object) {
                if (object instanceof Map.Entry) {
                    Entry<?, ?> entry = getEntry(((Map.Entry<?, ?>) object)
                            .getKey());
                    if (entry != null) {
                        Object key = entry.get();
                        if (key != null || entry.isNull) {
                            return object.equals(entry);
                        }
                    }
                }
                return false;
            }

            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return new HashIterator<Map.Entry<K, V>>(
                        new Entry.Type<Map.Entry<K, V>, K, V>() {
                            public Map.Entry<K, V> get(Map.Entry<K, V> entry) {
                                return entry;
                            }
                        });
            }
        };
    }

    /**
     * Returns a set of the keys contained in this map. The set is backed by
     * this map so changes to one are reflected by the other. The set does not
     * support adding.
     *
     * @return a set of the keys.
     */
    @Override
    public Set<K> keySet() {
        poll();
        if (keySet == null) {
            keySet = new AbstractSet<K>() {
                @Override
                public boolean contains(Object object) {
                    return containsKey(object);
                }

                @Override
                public int size() {
                    return WeakIdentityHashMap.this.size();
                }

                @Override
                public void clear() {
                    WeakIdentityHashMap.this.clear();
                }

                @Override
                public boolean remove(Object key) {
                    if (containsKey(key)) {
                        WeakIdentityHashMap.this.remove(key);
                        return true;
                    }
                    return false;
                }

                @Override
                public Iterator<K> iterator() {
                    return new HashIterator<K>(new Entry.Type<K, K, V>() {
                        public K get(Map.Entry<K, V> entry) {
                            return entry.getKey();
                        }
                    });
                }
            };
        }
        return keySet;
    }

    /**
     * <p>
     * Returns a collection of the values contained in this map. The collection
     * is backed by this map so changes to one are reflected by the other. The
     * collection supports remove, removeAll, retainAll and clear operations,
     * and it does not support add or addAll operations.
     * </p>
     * <p>
     * This method returns a collection which is the subclass of
     * AbstractCollection. The iterator method of this subclass returns a
     * "wrapper object" over the iterator of map's entrySet(). The size method
     * wraps the map's size method and the contains method wraps the map's
     * containsValue method.
     * </p>
     * <p>
     * The collection is created when this method is called at first time and
     * returned in response to all subsequent calls. This method may return
     * different Collection when multiple calls to this method, since it has no
     * synchronization performed.
     * </p>
     * @return a collection of the values contained in this map.
     */
    @Override
    public Collection<V> values() {
        poll();
        if (valuesCollection == null) {
            valuesCollection = new AbstractCollection<V>() {
                @Override
                public int size() {
                    return WeakIdentityHashMap.this.size();
                }

                @Override
                public void clear() {
                    WeakIdentityHashMap.this.clear();
                }

                @Override
                public boolean contains(Object object) {
                    return containsValue(object);
                }

                @Override
                public Iterator<V> iterator() {
                    return new HashIterator<V>(new Entry.Type<V, K, V>() {
                        public V get(Map.Entry<K, V> entry) {
                            return entry.getValue();
                        }
                    });
                }
            };
        }
        return valuesCollection;
    }

    /**
     * Returns the value of the mapping with the specified key.
     *
     * @param key the key.
     * @return the value of the mapping with the specified key, or {@code null}
     * if no mapping for the specified key is found.
     */
    @Override
    public V get(Object key) {
        Entry<K, V> entry = getEntry(key);
        return entry != null ? entry.value : null;
    }

    private Entry<K, V> getEntry(Object key) {
        poll();
        if (key != null) {
            int index = (System.identityHashCode(key) & 0x7FFFFFFF) % elementData.length;
            Entry<K, V> entry = elementData[index];
            while (entry != null) {
                if (key == entry.get()) {
                    return entry;
                }
                entry = entry.next;
            }
            return null;
        }
        Entry<K, V> entry = elementData[0];
        while (entry != null) {
            if (entry.isNull) {
                return entry;
            }
            entry = entry.next;
        }
        return null;
    }

    /**
     * Returns whether this map contains the specified value.
     *
     * @param value the value to search for.
     * @return {@code true} if this map contains the specified value,
     * {@code false} otherwise.
     */
    @Override
    public boolean containsValue(Object value) {
        poll();
        if (value != null) {
            for (int i = elementData.length; --i >= 0; ) {
                Entry<K, V> entry = elementData[i];
                while (entry != null) {
                    K key = entry.get();
                    if ((key != null || entry.isNull)
                            && value.equals(entry.value)) {
                        return true;
                    }
                    entry = entry.next;
                }
            }
        } else {
            for (int i = elementData.length; --i >= 0; ) {
                Entry<K, V> entry = elementData[i];
                while (entry != null) {
                    K key = entry.get();
                    if ((key != null || entry.isNull) && entry.value == null) {
                        return true;
                    }
                    entry = entry.next;
                }
            }
        }
        return false;
    }

    /**
     * @return <code>true</code> if this map is empty. <code>false</code> otherwise.
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @SuppressWarnings("unchecked")
    private void poll() {
        Entry<K, V> toRemove;
        while ((toRemove = (Entry<K, V>) referenceQueue.poll()) != null) {
            removeEntry(toRemove);
        }
    }

    private void removeEntry(Entry<K, V> toRemove) {
        Entry<K, V> entry, last = null;
        int index = (toRemove.hash & 0x7FFFFFFF) % elementData.length;
        entry = elementData[index];
        // Ignore queued entries which cannot be found, the user could
        // have removed them before they were queued, i.e. using clear()
        while (entry != null) {
            if (toRemove == entry) {
                modCount++;
                if (last == null) {
                    elementData[index] = entry.next;
                } else {
                    last.next = entry.next;
                }
                elementCount--;
                break;
            }
            last = entry;
            entry = entry.next;
        }
    }

    /**
     * Maps the specified key to the specified value.
     *
     * @param key   the key.
     * @param value the value.
     * @return the value of any previous mapping with the specified key or
     * {@code null} if there was no mapping.
     */
    @Override
    public V put(K key, V value) {
        poll();
        int index = 0;
        Entry<K, V> entry;
        if (key != null) {
            index = (System.identityHashCode(key) & 0x7FFFFFFF) % elementData.length;
            entry = elementData[index];
            while (entry != null && !(key == entry.get())) {
                entry = entry.next;
            }
        } else {
            entry = elementData[0];
            while (entry != null && !entry.isNull) {
                entry = entry.next;
            }
        }
        if (entry == null) {
            modCount++;
            if (++elementCount > threshold) {
                rehash();
                index = key == null ? 0 : (System.identityHashCode(key) & 0x7FFFFFFF) % elementData.length;
            }
            entry = new Entry<K, V>(key, value, referenceQueue);
            entry.next = elementData[index];
            elementData[index] = entry;
            return null;
        }
        V result = entry.value;
        entry.value = value;
        return result;
    }

    private void rehash() {
        assert elementData.length > 0;
        int length = elementData.length * 2;
        Entry<K, V>[] newData = newEntryArray(length);
        for (Entry<K, V> entry : elementData) {
            while (entry != null) {
                int index = entry.isNull ? 0 : (entry.hash & 0x7FFFFFFF) % length;
                Entry<K, V> next = entry.next;
                entry.next = newData[index];
                newData[index] = entry;
                entry = next;
            }
        }
        elementData = newData;
        computeMaxSize();
    }

    /**
     * Removes the mapping with the specified key from this map.
     *
     * @param key the key of the mapping to remove.
     * @return the value of the removed mapping or {@code null} if no mapping
     * for the specified key was found.
     */
    @Override
    public V remove(Object key) {
        poll();
        int index = 0;
        Entry<K, V> entry, last = null;
        if (key != null) {
            index = (System.identityHashCode(key) & 0x7FFFFFFF) % elementData.length;
            entry = elementData[index];
            while (entry != null && !(key == entry.get())) {
                last = entry;
                entry = entry.next;
            }
        } else {
            entry = elementData[0];
            while (entry != null && !entry.isNull) {
                last = entry;
                entry = entry.next;
            }
        }
        if (entry != null) {
            modCount++;
            if (last == null) {
                elementData[index] = entry.next;
            } else {
                last.next = entry.next;
            }
            elementCount--;
            return entry.value;
        }
        return null;
    }

    /**
     * @return the number of elements in this map.
     */
    @Override
    public int size() {
        poll();
        return elementCount;
    }
}
