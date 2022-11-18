package org.klojang.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class FixedTypeMap<V> extends NonExpandingTypeMap<V> {

  private final Map<Class<?>, V> backend;

  FixedTypeMap(HashMap<Class<?>, V> src, boolean autobox) {
    super(autobox);
    this.backend = Map.copyOf(src);
  }

  @Override
  Map<Class<?>, V> backend() {
    return backend;
  }

  @Override
  public Set<Class<?>> keySet() {
    return backend.keySet();
  }

  @Override
  public Collection<V> values() {
    // Keep behaviour consistent across impls
    return Set.copyOf(backend.values());
  }

  @Override
  public Set<Entry<Class<?>, V>> entrySet() {
    return backend.entrySet();
  }


}
