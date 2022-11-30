package org.klojang.collections;

/**
 * Defines an interface for building {@link TypeMap} instances. You obtain a concrete
 * {@code TypeMapBuilder} through one of the static factory methods on the
 * {@code TypeMap} interface.
 *
 * @param <V> The type of the values in the {@code TypeMap}.
 * @see TypeMap#nativeTypeMapBuilder()
 * @see TypeMap#fixedTypeMapBuilder()
 * @see TypeMap#greedyTypeMapBuilder()
 * @see TypeMap#treeTypeMapBuilder()
 */
public sealed interface TypeMapBuilder<V> permits TreeTypeMapBuilder,
    NativeTypeMapBuilder,
    FixedTypeMapBuilder,
    GreedyTypeMapBuilder {

  /**
   * Whether to enable <a href="TypeMap.html#autoboxing">autoboxing</a>.
   *
   * @param autobox whether to enable "autoboxing"
   * @return this {@code Builder} instance
   */
  TypeMapBuilder<V> autobox(boolean autobox);

  /**
   * Associates the specified type with the specified value.
   *
   * @param type The type
   * @param value The value
   * @return this {@code Builder} instance
   */
  TypeMapBuilder<V> add(Class<?> type, V value);

  /**
   * Associates multiple types with the same value.
   *
   * @param value The value
   * @param types The types to associate the value with
   * @return this {@code Builder} instance
   */
  TypeMapBuilder<V> addMultiple(V value, Class<?>... types);

  /**
   * Returns a {@code Map} with the configured types and behaviour.
   *
   * @return a {@code TypeMap} with the configured types and behaviour
   */
  TypeMap<V> freeze();

}
