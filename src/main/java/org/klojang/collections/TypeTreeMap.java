package org.klojang.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

import static java.util.AbstractMap.SimpleImmutableEntry;
import static java.util.stream.Collectors.toUnmodifiableSet;

final class TypeTreeMap<V> extends
    NonExpandingTypeMap<V> {

  private final TreeMap<Class<?>, V> backend;

  TypeTreeMap(TreeMap<Class<?>, V> src, boolean autobox) {
    super(autobox);
    this.backend = src;
  }

  @Override
  Map<Class<?>, V> backend() {
    return backend;
  }

  @Override
  public Set<Class<?>> keySet() {
    return Set.copyOf(backend.keySet());
  }

  @Override
  public Collection<V> values() {
    return Set.copyOf(backend.values());
  }

  @Override
  public Set<Entry<Class<?>, V>> entrySet() {
    return backend.entrySet()
        .stream()
        .map(toImmutableEntry())
        .collect(toUnmodifiableSet());
  }

  private UnaryOperator<Entry<Class<?>, V>> toImmutableEntry() {
    return e -> new SimpleImmutableEntry<>(e.getKey(), e.getValue());
  }

}
