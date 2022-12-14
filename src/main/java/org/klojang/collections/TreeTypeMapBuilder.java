package org.klojang.collections;

import org.klojang.check.Check;
import org.klojang.check.Tag;

import java.util.Arrays;
import java.util.TreeMap;

import static org.klojang.check.CommonChecks.keyIn;
import static org.klojang.collections.FixedTypeMapBuilder.duplicateKey;

final class TreeTypeMapBuilder<V> implements TypeMapBuilder<V> {

  private final TreeMap<Class<?>, V> tmp = new TreeMap<>(new BasicTypeComparator());

  private boolean autobox = true;

  TreeTypeMapBuilder() {}

  public TreeTypeMapBuilder<V> autobox(boolean autobox) {
    this.autobox = autobox;
    return this;
  }

  @Override
  public TreeTypeMapBuilder<V> add(Class<?> type, V value) {
    Check.notNull(type, Tag.TYPE).isNot(keyIn(), tmp, duplicateKey(type));
    Check.notNull(value, Tag.VALUE);
    tmp.put(type, value);
    return this;
  }

  @Override
  public TreeTypeMapBuilder<V> addMultiple(V value, Class<?>... types) {
    Check.notNull(types, "types").ok(Arrays::stream).forEach(t -> add(t, value));
    return this;
  }

  @Override
  public TypeTreeMap<V> freeze() {
    return new TypeTreeMap<>(tmp, autobox);
  }

}
