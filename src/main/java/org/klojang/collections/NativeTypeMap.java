package org.klojang.collections;

import org.klojang.check.Check;
import org.klojang.util.ArrayType;

import java.util.*;

import static java.util.AbstractMap.SimpleImmutableEntry;
import static org.klojang.check.CommonChecks.instanceOf;
import static org.klojang.util.ClassMethods.*;
import static org.klojang.util.CollectionMethods.implode;

final class NativeTypeMap<V> extends ImmutableMap<Class<?>, V> implements TypeMap<V> {

  private final boolean autobox;
  private final TypeNode root;
  private final int size;

  private Set<Class<?>> keys;
  private Collection<V> values;
  private Set<Entry<Class<?>, V>> entries;

  NativeTypeMap(TypeNode root, int size, boolean autobox) {
    this.autobox = autobox;
    this.root = root;
    this.size = size;
  }

  @Override
  public V get(Object key) {
    Class<?> type = Check.notNull(key)
        .is(instanceOf(), Class.class)
        .ok(Class.class::cast);
    V val;
    if (type.isPrimitive()) {
      if ((val = root.getPrimitive(type)) == null) {
        if (autobox) {
          val = root.get(box(type));
        }
        if (val == null) {
          val = root.value();
        }
      }
    } else if (isDeeplyPrimitiveArray(type)) {
      if ((val = root.getPrimitive(type)) == null) {
        if (autobox) {
          val = root.get(ArrayType.forClass(type).box());
        }
        if (val == null) {
          val = root.value();
        }
      }
    } else {
      val = root.get(type);
    }
    return val;
  }

  @Override
  public boolean containsKey(Object key) {
    Class<?> type = Check.notNull(key)
        .is(instanceOf(), Class.class)
        .ok(Class.class::cast);
    boolean found;
    if (root.value != null) {
      found = true;
    } else if (type.isPrimitive()) {
      if (autobox) {
        found = containsPrimitiveOrBoxedType(type, box(type));
      } else {
        found = containsPrimitiveType(type);
      }
    } else if (isDeeplyPrimitiveArray(type)) {
      if (autobox) {
        Class<?> boxed = ArrayType.forClass(type).box();
        found = containsPrimitiveOrBoxedType(type, boxed);
      } else {
        found = containsPrimitiveType(type);
      }
    } else if (!type.isInterface()) {
      found = containsExactOrSuperType(type, root.subclasses);
      if (!found) {
        found = containsExactOrSuperType(type, root.extensions);
      }
    } else {
      found = containsExactOrSuperType(type, root.extensions);
    }
    return found;
  }

  @Override
  public boolean containsValue(Object value) {
    return values().contains(value);
  }

  /**
   * Returns a breadth-first view of the type hierarchy within this {@code Map}.
   *
   * @return a breadth-first view of the type hierarchy within this {@code Map}
   */
  @Override
  public Set<Class<?>> keySet() {
    if (keys == null) {
      List<Class<?>> bucket = new ArrayList<>(size);
      if (root.value() != null) { // map contains Object.class
        bucket.add(Object.class);
      }
      root.collectTypes(bucket);
      keys = ArraySet.copyOf(bucket);
    }
    return keys;
  }

  @Override
  public Collection<V> values() {
    if (values == null) {
      List<V> bucket = new ArrayList<>(size);
      if (root.value() != null) {
        bucket.add(root.value());
      }
      root.collectValues(bucket);
      values = ArraySet.copyOf(new HashSet<>(bucket));
    }
    return values;
  }

  @Override
  public Set<Entry<Class<?>, V>> entrySet() {
    if (entries == null) {
      List<Entry<Class<?>, V>> bucket = new ArrayList<>(size);
      if (root.value() != null) {
        bucket.add(new SimpleImmutableEntry<>(Object.class, root.value()));
      }
      root.collectEntries(bucket);
      entries = Set.copyOf(bucket);
    }
    return entries;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof Map<?, ?> m) {
      if (size == m.size()) {
        return entrySet().equals(m.entrySet());
      }
    }
    return false;
  }

  private int hash;

  @Override
  public int hashCode() {
    if (hash == 0) {
      hash = entrySet().hashCode();
    }
    return hash;
  }

  @Override
  public String toString() {
    return '[' + implode(entrySet()) + ']';
  }

  private boolean containsPrimitiveType(Class<?> type) {
    for (var node : root.subclasses) {
      if (node.type == type) {
        return true;
      }
    }
    return false;
  }

  private boolean containsPrimitiveOrBoxedType(Class<?> primitive, Class<?> boxed) {
    for (var node : root.subclasses) {
      if (node.type == primitive || isSupertype(node.type, boxed)) {
        return true;
      }
    }
    return false;
  }

  private boolean containsExactOrSuperType(Class<?> type, TypeNode[] nodes) {
    for (var node : nodes) {
      if (isSupertype(node.type, type)) {
        return true;
      }
    }
    return false;
  }

}
