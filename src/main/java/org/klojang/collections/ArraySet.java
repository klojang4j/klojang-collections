package org.klojang.collections;

import org.klojang.check.Check;
import org.klojang.util.ArrayMethods;
import org.klojang.util.InvokeMethods;

import java.util.*;

import static org.klojang.check.CommonChecks.lt;
import static org.klojang.util.ArrayMethods.EMPTY_OBJECT_ARRAY;
import static org.klojang.util.ArrayMethods.implode;

final class ArraySet<E> extends ImmutableSet<E> {

  @SuppressWarnings({"rawtypes"})
  private static final ArraySet EMPTY = new ArraySet(EMPTY_OBJECT_ARRAY);

  @SuppressWarnings({"unchecked"})
  private static <E> ArraySet<E> empty() {
    return (ArraySet<E>) EMPTY;
  }

  /*
   * If trusted, the provided array is supposed to be internally generated, going out
   * of scope immediately, and known to contain unique values only. All bets are off
   * if this is not the case!!!! In that case the array will be swallowed rather than
   * copied into the ArraySet.
   */
  static <E> ArraySet<E> of(E[] values) {
    return values.length != 0 ? new ArraySet<>(values) : empty();
  }

  static <E> ArraySet<E> copyOf(Collection<E> values) {
    return values.size() != 0 ? new ArraySet<>(values.toArray()) : empty();
  }

  private final Object[] elems;

  private ArraySet(Object[] elems) {
    this.elems = elems;
  }

  @Override
  public int size() {
    return elems.length;
  }

  @Override
  public boolean isEmpty() {
    return elems.length == 0;
  }

  @Override
  public boolean contains(Object o) {
    return ArrayMethods.isElementOf(o, elems);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return new HashSet<>(this).containsAll(c);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T[] toArray(T[] a) {
    Check.notNull(a);
    int sz = elems.length;
    if (a.length < sz) {
      a = (T[]) InvokeMethods.newArray(a.getClass(), sz);
    }
    int i = 0;
    Object[] result = a;
    for (E val : this) {
      result[i++] = val;
    }
    if (a.length > sz) {
      a[sz] = null;
    }
    return a;
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<>() {
      private int i = 0;

      @Override
      public boolean hasNext() {
        return i < elems.length;
      }

      @Override
      @SuppressWarnings({"unchecked"})
      public E next() {
        Check.that(i).is(lt(), size(), NoSuchElementException::new);
        return (E) elems[i++];
      }
    };
  }

  @Override
  public Object[] toArray() {
    if (this == EMPTY) {
      return EMPTY_OBJECT_ARRAY;
    }
    Object[] objs = new Object[elems.length];
    System.arraycopy(elems, 0, objs, 0, elems.length);
    return objs;
  }

  private int hash;
  private String str;

  @Override
  public int hashCode() {
    if (hash == 0) {
      hash = Arrays.hashCode(elems);
    }
    return hash;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof ArraySet<?> other) {
      int len = other.elems.length;
      if (elems.length == len) {
        for (int i = 0; i < len; ++i) {
          if (!elems[i].equals(other.elems[i])) {
            return false;
          }
        }
        return true;
      }
      return false;
    }
     if (o instanceof Set s) {
      Iterator<E> itr = s.iterator();
      for (Object e : elems) {
        if (!itr.hasNext() || !e.equals(itr.next())) {
          return false;
        }
      }
      return !itr.hasNext();
    }
    return false;
  }

  @Override
  public String toString() {
    if (str == null) {
      str = '[' + implode(elems) + ']';
    }
    return str;
  }

}
