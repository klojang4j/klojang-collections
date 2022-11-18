package org.klojang.collections;

import org.junit.Test;
import org.klojang.check.aux.DuplicateValueException;
import org.klojang.util.util.MutableInt;

import java.util.*;

import static org.junit.Assert.*;
import static org.klojang.util.ArrayMethods.pack;

public class ArraySetTest {

  @Test
  public void of00() {
    ArraySet<Integer> set = ArraySet.of(pack(0, 1, 2, 3, 4));
    assertEquals(Set.of(0, 1, 2, 3, 4), set);
    set = ArraySet.of(pack(0, 1, 2, 3, 4));
  }

  @Test
  public void copyOfList00() {
    ArraySet<Integer> set = ArraySet.copyOf(List.of(0, 1, 2, 3, 4));
    assertEquals(Set.of(0, 1, 2, 3, 4), set);
    set = ArraySet.copyOf(List.of(0, 1, 2, 3, 4));
    assertEquals(Set.of(0, 1, 2, 3, 4), set);
  }

  @Test
  public void iterator00() {
    ArraySet<Integer> set = ArraySet.copyOf(List.of(0, 1, 2, 3, 4, 5));
    MutableInt mi = new MutableInt();
    for (Iterator<Integer> itr = set.iterator(); itr.hasNext(); ) {
      assertEquals(mi.pp(), (int) itr.next());
    }
  }

  @Test
  public void toArray00() {
    ArraySet<Integer> set = ArraySet.copyOf(List.of(0, 1, 2, 3, 4, 5));
    assertEquals(pack(0, 1, 2, 3, 4, 5), set.toArray());
    assertEquals(pack(0, 1, 2, 3, 4, 5), set.toArray(Integer[]::new));
    assertEquals(pack(0, 1, 2, 3, 4, 5, null), set.toArray(new Integer[7]));
  }

}
