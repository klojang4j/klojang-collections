package org.klojang.collections;

import org.klojang.check.Check;
import org.klojang.check.Tag;

import java.util.Arrays;
import java.util.HashMap;

import static org.klojang.check.CommonChecks.keyIn;
import static org.klojang.collections.FixedTypeMapBuilder.duplicateKey;

final class GreedyTypeMapBuilder<V> implements TypeMapBuilder<V> {

  private final HashMap<Class<?>, V> tmp = new HashMap<>();

  private boolean autobox = true;

  GreedyTypeMapBuilder() {}

  @Override
  public GreedyTypeMapBuilder<V> autobox(boolean autobox) {
    this.autobox = autobox;
    return this;
  }

  @Override
  public GreedyTypeMapBuilder<V> add(Class<?> type, V value) {
    Check.notNull(type, Tag.TYPE).isNot(keyIn(), tmp, duplicateKey(type));
    Check.notNull(value, Tag.VALUE);
    tmp.put(type, value);
    return this;
  }

  @Override
  public GreedyTypeMapBuilder<V> addMultiple(V value, Class<?>... types) {
    Check.notNull(types, "types").ok(Arrays::stream).forEach(t -> add(t, value));
    return this;
  }

  @Override
  public GreedyTypeMap<V> freeze() {
    return new GreedyTypeMap<>(tmp, autobox);
  }

}
