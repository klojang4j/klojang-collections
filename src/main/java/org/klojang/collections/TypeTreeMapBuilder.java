package org.klojang.collections;

import org.klojang.check.Check;
import org.klojang.check.Param;

import java.util.Arrays;
import java.util.TreeMap;

import static org.klojang.check.CommonChecks.keyIn;
import static org.klojang.collections.FixedTypeMapBuilder.duplicateKey;

final class TypeTreeMapBuilder<V> implements TypeMapBuilder<V> {

  private final TreeMap<Class<?>, V> tmp = new TreeMap<>(new BasicTypeComparator());

  private boolean autobox = true;

  TypeTreeMapBuilder() {}

  public TypeTreeMapBuilder<V> autobox(boolean autobox) {
    this.autobox = autobox;
    return this;
  }

  @Override
  public TypeTreeMapBuilder<V> add(Class<?> type, V value) {
    Check.notNull(type, Param.TYPE).isNot(keyIn(), tmp, duplicateKey(type));
    Check.notNull(value, Param.VALUE);
    tmp.put(type, value);
    return this;
  }

  @Override
  public TypeTreeMapBuilder<V> addMultiple(V value, Class<?>... types) {
    Check.notNull(types, "types").ok(Arrays::stream).forEach(t -> add(t, value));
    return this;
  }

  @Override
  public TypeTreeMap<V> freeze() {
    return new TypeTreeMap<>(tmp, autobox);
  }

}