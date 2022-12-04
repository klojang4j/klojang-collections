package org.klojang.collections;

import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyListIterator;
import static org.junit.Assert.*;
import static org.klojang.util.ArrayMethods.EMPTY_OBJECT_ARRAY;
import static org.klojang.util.ArrayMethods.pack;

public class CrisprListTest {

  /*
    Don't throw away; nodeAfter & nodeBefore are private
    methods with a central role, so we may want to make
    them package private once in a while for testing purposes.

    @Test
    public void nodeAfter00() {
      var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

      var node1 = cl0.nodeAt(2);
      assertEquals((Integer) 2, node1.val);

      var node2 = cl0.nodeAfter(node1, 2, 2);
      assertEquals((Integer) 2, node2.val);
      node2 = cl0.nodeAfter(node1, 2, 3);
      assertEquals((Integer) 3, node2.val);
      node2 = cl0.nodeAfter(node1, 2, 5);
      assertEquals((Integer) 5, node2.val);
      node2 = cl0.nodeAfter(node1, 2, 8);
      assertEquals((Integer) 8, node2.val);
      node2 = cl0.nodeAfter(node1, 2, 9);
      assertEquals((Integer) 9, node2.val);

      node1 = cl0.nodeAt(7);
      node2 = cl0.nodeAfter(node1, 7, 8);
      assertEquals((Integer) 8, node2.val);
      node2 = cl0.nodeAfter(node1, 7, 9);
      assertEquals((Integer) 9, node2.val);

      node1 = cl0.nodeAt(0);
      node2 = cl0.nodeAfter(node1, 0, 9);
      assertEquals((Integer) 9, node2.val);
    }

    @Test
    public void nodeBefore00() {
      var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

      var node1 = cl0.nodeAt(8);
      assertEquals((Integer) 8, node1.val);

      var node2 = cl0.nodeBefore(node1, 8, 7);
      assertEquals((Integer) 7, node2.val);
      node2 = cl0.nodeBefore(node1, 8, 3);
      assertEquals((Integer) 3, node2.val);
      node2 = cl0.nodeBefore(node1, 8, 0);
      assertEquals((Integer) 0, node2.val);

      node1 = cl0.nodeAt(1);
      assertEquals((Integer) 1, node1.val);
      node2 = cl0.nodeBefore(node1, 1, 0);
      assertEquals((Integer) 0, node2.val);

      node1 = cl0.nodeAt(9);
      assertEquals((Integer) 9, node1.val);
      node2 = cl0.nodeBefore(node1, 9, 0);
      assertEquals((Integer) 0, node2.val);

    }
  */

  @Test
  public void join() {
    var cl0 = CrisprList.of("a");
    var cl1 = CrisprList.<String>of(null);
    var cl2 = CrisprList.<String>of(null);
    var cl3 = CrisprList.of("b", "c");
    var cl4 = CrisprList.<String>of();
    var cl5 = CrisprList.join(List.of(cl0, cl1, cl2, cl3, cl4));
    assertEquals(Arrays.asList("a", null, null, "b", "c"), cl5);
  }

  @Test
  public void append00() {
    var cl = new CrisprList<String>();
    assertTrue(cl.isEmpty());
    cl.append("John");
    assertFalse(cl.isEmpty());
    assertEquals(1, cl.size());
    assertEquals("John", cl.get(0));
  }

  @Test
  public void append01() {
    var cl = new CrisprList<String>();
    cl.append("John");
    cl.append(null);
    assertEquals(2, cl.size());
    assertEquals("John", cl.get(0));
    assertNull(cl.get(1));
  }

  @Test
  public void append02() {
    var cl = new CrisprList<String>();
    cl.append("John");
    cl.append(null);
    cl.append("Jim");
    assertEquals(3, cl.size());
    assertEquals("John", cl.get(0));
    assertNull(cl.get(1));
    assertEquals("Jim", cl.get(2));
  }

  @Test
  public void append03() {
    var cl = new CrisprList<String>();
    cl.append("John");
    cl.append(null);
    cl.append("Jim");
    cl.append(null);
    assertEquals(4, cl.size());
    assertEquals("John", cl.get(0));
    assertNull(cl.get(1));
    assertEquals("Jim", cl.get(2));
    assertNull(cl.get(3));
  }

  @Test
  public void unshift00() {
    var cl = new CrisprList<String>();
    assertTrue(cl.isEmpty());
    cl.prepend("John");
    assertFalse(cl.isEmpty());
    assertEquals(1, cl.size());
    assertEquals("John", cl.get(0));
  }

  @Test
  public void unshift01() {
    var cl = new CrisprList<String>();
    cl.prepend("John");
    cl.prepend(null);
    assertEquals(2, cl.size());
    assertNull(cl.get(0));
    assertEquals("John", cl.get(1));
  }

  @Test
  public void unshift02() {
    var cl = new CrisprList<String>();
    cl.prepend("John");
    cl.prepend(null);
    cl.prepend("Jim");
    assertEquals(3, cl.size());
    assertEquals("Jim", cl.get(0));
    assertNull(cl.get(1));
    assertEquals("John", cl.get(2));
  }

  @Test
  public void unshift03() {
    var cl = new CrisprList<String>();
    cl.prepend("John");
    cl.prepend(null);
    cl.prepend("Jim");
    cl.prepend((String) null);
    assertEquals(4, cl.size());
    assertNull(cl.get(0));
    assertEquals("Jim", cl.get(1));
    assertNull(cl.get(2));
    assertEquals("John", cl.get(3));
  }

  @Test
  public void deleteFirst() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertEquals(0, (int) cl.first());
    cl.deleteFirst();
    assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9), cl);
    assertEquals(1, (int) cl.first());
    cl.deleteFirst();
    assertEquals(List.of(2, 3, 4, 5, 6, 7, 8, 9), cl);
    cl.prepend(1);
    assertEquals(1, (int) cl.first());
    assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9), cl);
    cl.prepend(0);
    assertEquals(0, (int) cl.first());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl);
  }

  @Test
  public void deleteLast00() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertEquals(9, (int) cl.last());
    cl.deleteLast();
    assertEquals(8, (int) cl.last());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8), cl);
    cl.deleteLast();
    assertEquals(7, (int) cl.last());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7), cl);
    cl.append(8);
    assertEquals(8, (int) cl.last());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8), cl);
    cl.append(9);
    assertEquals(9, (int) cl.last());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl);
  }

  @Test
  public void add00() {
    var cl = new CrisprList<String>();
    cl.add(0, "John");
    assertEquals(List.of("John"), cl);
    cl.add(0, "Mark");
    assertEquals(List.of("Mark", "John"), cl);
    cl.add(2, "Michael");
    assertEquals(List.of("Mark", "John", "Michael"), cl);
    cl.add(2, "James");
    assertEquals(List.of("Mark", "John", "James", "Michael"), cl);
    cl.add(1, "Simon");
    assertEquals(List.of("Mark", "Simon", "John", "James", "Michael"), cl);
    cl.add(1, "Josh");
    assertEquals(List.of("Mark", "Josh", "Simon", "John", "James", "Michael"), cl);
    cl.add(4, "Mary");
    assertEquals(List.of("Mark",
        "Josh",
        "Simon",
        "John",
        "Mary",
        "James",
        "Michael"), cl);
    cl.add(3, "Jill");
    assertEquals(List.of("Mark",
        "Josh",
        "Simon",
        "Jill",
        "John",
        "Mary",
        "James",
        "Michael"), cl);
    cl.add(4, "Ana");
    assertEquals(List.of("Mark",
        "Josh",
        "Simon",
        "Jill",
        "Ana",
        "John",
        "Mary",
        "James",
        "Michael"), cl);
  }

  @Test
  public void insert00() {
    var cl = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6);
    var cl2 = cl.insert(2, "a");
    assertSame(cl, cl2);
    assertEquals(List.of(0, 1, "a", 2, 3, 4, 5, 6), cl);
  }

  @Test
  public void insert01() {
    var cl = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6);
    cl.insert(2, "a");
    assertEquals(List.of(0, 1, "a", 2, 3, 4, 5, 6), cl);
  }

  @Test
  public void insert02() {
    var cl = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6);
    cl.insert(7, "a");
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, "a"), cl);
  }

  @Test
  public void addAll00() {
    var cl = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6);
    cl.addAll(2, List.of("a", "b", "c"));
    assertEquals(List.of(0, 1, "a", "b", "c", 2, 3, 4, 5, 6), cl);
  }

  @Test
  public void addAll01() {
    var cl = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6);
    cl.addAll(2, List.of("a"));
    assertEquals(List.of(0, 1, "a", 2, 3, 4, 5, 6), cl);
  }

  @Test
  public void addAll02() {
    var cl = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6);
    cl.addAll(2, List.of());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6), cl);
  }

  @Test
  public void addAll03() {
    var cl = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6);
    cl.addAll(cl.size(), List.of("a", "b", "c"));
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, "a", "b", "c"), cl);
  }

  @Test
  public void addAll04() {
    var cl = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6);
    cl.addAll(0, List.of("a", "b", "c"));
    assertEquals(List.of("a", "b", "c", 0, 1, 2, 3, 4, 5, 6), cl);
  }

  @Test
  public void addAll05() {
    var cl = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6);
    cl.addAll(cl.size(), List.of("a"));
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, "a"), cl);
  }

  @Test
  public void addAll06() {
    var cl = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6);
    cl.addAll(0, List.of("a"));
    assertEquals(List.of("a", 0, 1, 2, 3, 4, 5, 6), cl);
  }

  @Test
  public void addAll07() {
    var cl = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6);
    cl.addAll(cl.size(), List.of());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6), cl);
  }

  @Test
  public void addAll08() {
    var cl = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6);
    cl.addAll(0, List.of());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6), cl);
  }

  @Test
  public void addAll09() {
    var cl = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6);
    cl.addAll(2, CrisprList.of("a", "b", "c"));
    assertEquals(List.of(0, 1, "a", "b", "c", 2, 3, 4, 5, 6), cl);
  }

  @Test
  public void addAll10() {
    var cl = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6);
    cl.addAll(2, CrisprList.of("a"));
    assertEquals(List.of(0, 1, "a", 2, 3, 4, 5, 6), cl);
  }

  @Test
  public void addAll11() {
    var cl = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6);
    cl.addAll(2, new CrisprList<>());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6), cl);
  }

  @Test
  public void addAll12() {
    var cl = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6);
    cl.addAll(List.of("a", "b", "c", "d"));
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, "a", "b", "c", "d"), cl);
  }

  @Test
  public void insertAll00() {
    var cl = CrisprList.ofElements(new Object[] {0, 1, 2, 3, 4, 5, 6});
    var cl2 = cl.insertAll(2, List.of("a", "b", "c"));
    assertSame(cl, cl2);
    assertEquals(List.of(0, 1, "a", "b", "c", 2, 3, 4, 5, 6), cl);
  }

  @Test
  public void insertAll01() {
    var cl = CrisprList.ofElements(new Object[] {0, 1, 2, 3, 4, 5, 6});
    var cl2 = cl.insertAll(7, List.of("a", "b", "c"));
    assertSame(cl, cl2);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, "a", "b", "c"), cl);
  }

  @Test
  public void insertAll02() {
    var cl = CrisprList.ofElements(new Object[] {0, 1, 2, 3, 4, 5, 6});
    var cl2 = cl.insertAll(0, List.of("a", "b", "c"));
    assertSame(cl, cl2);
    assertEquals(List.of("a", "b", "c", 0, 1, 2, 3, 4, 5, 6), cl);
  }

  @Test
  public void insertAll03() {
    var cl = CrisprList.ofElements(new Object[] {0, 1, 2, 3, 4, 5, 6});
    var cl2 = cl.insertAll(0, List.of());
    assertSame(cl, cl2);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6), cl);
  }

  @Test
  public void insertAll04() {
    var cl = CrisprList.<Object>of();
    var cl2 = cl.insertAll(0, List.of());
    assertSame(cl, cl2);
    assertEquals(List.of(), cl);
  }

  @Test
  public void insertAll05() {
    var cl = CrisprList.<Object>of();
    var cl2 = cl.insertAll(0, List.of(1, 2, 3));
    assertSame(cl, cl2);
    assertEquals(List.of(1, 2, 3), cl);
  }

  @Test
  public void prependAll00() {
    var cl = CrisprList.ofElements(new Object[] {0, 1, 2, 3, 4, 5, 6});
    var cl2 = cl.prependAll(List.of("a"));
    assertSame(cl, cl2);
    assertEquals(List.of("a", 0, 1, 2, 3, 4, 5, 6), cl);
  }

  @Test
  public void prependAll01() {
    var cl = CrisprList.ofElements(new Object[] {0, 1, 2, 3, 4, 5, 6});
    var cl2 = cl.prependAll(List.of());
    assertSame(cl, cl2);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6), cl);
  }

  @Test
  public void appendAll00() {
    var cl = CrisprList.ofElements(new Object[] {0, 1, 2, 3, 4, 5, 6});
    var cl2 = cl.appendAll(List.of("a", "b"));
    assertSame(cl, cl2);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, "a", "b"), cl);
  }

  @Test
  public void appendAll01() {
    var cl = CrisprList.ofElements(new Object[] {0, 1, 2, 3, 4, 5, 6});
    var cl2 = cl.appendAll(List.of());
    assertSame(cl, cl2);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6), cl);
  }

  @Test
  public void cut01() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = cl0.cut(0, 2);
    assertEquals(8, cl0.size());
    assertEquals(2, cl1.size());
    assertEquals(CrisprList.of(2, 3, 4, 5, 6, 7, 8, 9), cl0);
    assertEquals(CrisprList.of(0, 1), cl1);
  }

  @Test
  public void cut02() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = cl0.cut(9, 10);
    assertEquals(9, cl0.size());
    assertEquals(1, cl1.size());
    assertEquals(CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8), cl0);
    assertEquals(List.of(9), cl1);
  }

  @Test
  public void cut03() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = cl0.cut(0, 0);
    assertEquals(10, cl0.size());
    assertEquals(0, cl1.size());
    assertEquals(CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl0);
  }

  @Test
  public void cut04() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = cl0.cut(6, 10);
    assertEquals(6, cl0.size());
    assertEquals(4, cl1.size());
    assertEquals(CrisprList.of(0, 1, 2, 3, 4, 5), cl0);
    assertEquals(CrisprList.of(6, 7, 8, 9), cl1);
  }

  @Test
  public void cut05() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = cl0.cut(0, 10);
    assertEquals(0, cl0.size());
    assertEquals(10, cl1.size());
    assertEquals(CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl1);
  }

  @Test
  public void cut06() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = cl0.cut(1, 9);
    assertEquals(2, cl0.size());
    assertEquals(8, cl1.size());
    assertEquals(CrisprList.of(0, 9), cl0);
    assertEquals(CrisprList.of(1, 2, 3, 4, 5, 6, 7, 8), cl1);
  }

  @Test
  public void remove00() {
    var cl = new CrisprList<String>();
    cl.add(0, "John"); // John
    cl.add(0, "Mark"); // Mark, John
    cl.add(2, "Michael"); // Mark, John, Michael
    cl.add(2, "James"); // Mark, John, James, Michael
    cl.add(1, "Simon"); // Mark, Simon, John, James, Michael
    assertEquals(List.of("Mark", "Simon", "John", "James", "Michael"), cl);
    assertEquals(5, cl.size());
    cl.remove(0);
    assertEquals(4, cl.size());
    assertEquals(List.of("Simon", "John", "James", "Michael"), cl);
    cl.remove(2);
    assertEquals(3, cl.size());
    assertEquals(List.of("Simon", "John", "Michael"), cl);
    cl.remove(2);
    assertEquals(2, cl.size());
    assertEquals(List.of("Simon", "John"), cl);
    cl.remove(1);
    assertEquals(1, cl.size());
    assertEquals(List.of("Simon"), cl);
    cl.remove(0);
    assertEquals(0, cl.size());
    assertEquals(List.of(), cl);
  }

  @Test
  public void remove01() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4);
    assertEquals(5, cl0.size());
    cl0.remove(0);
    assertEquals(List.of(1, 2, 3, 4), cl0);
    cl0.remove(0);
    assertEquals(List.of(2, 3, 4), cl0);
    cl0.remove(0);
    assertEquals(List.of(3, 4), cl0);
    cl0.remove(0);
    assertEquals(List.of(4), cl0);
    cl0.remove(0);
    assertEquals(List.of(), cl0);
  }

  @Test
  public void remove02() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4);
    assertEquals(5, cl0.size());
    cl0.remove(4);
    assertEquals(List.of(0, 1, 2, 3), cl0);
    cl0.remove(3);
    assertEquals(List.of(0, 1, 2), cl0);
    cl0.remove(2);
    assertEquals(List.of(0, 1), cl0);
    cl0.remove(1);
    assertEquals(List.of(0), cl0);
    cl0.remove(0);
    assertEquals(List.of(), cl0);
  }

  @Test
  public void remove03() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4);
    assertEquals(5, cl0.size());
    cl0.remove(2);
    assertEquals(List.of(0, 1, 3, 4), cl0);
    cl0.remove(2);
    assertEquals(List.of(0, 1, 4), cl0);
    cl0.remove(1);
    assertEquals(List.of(0, 4), cl0);
  }

  @Test
  public void remove04() {
    var cl0 = CrisprList.of("0", "1", "2", "3", "4");
    assertTrue(cl0.remove("2"));
    assertEquals(List.of("0", "1", "3", "4"), cl0);
    assertFalse(cl0.remove("734"));
    assertEquals(List.of("0", "1", "3", "4"), cl0);
    assertTrue(cl0.remove("0"));
    assertEquals(List.of("1", "3", "4"), cl0);
    assertTrue(cl0.remove("4"));
    assertEquals(List.of("1", "3"), cl0);
  }

  @Test
  public void remove05() {
    var cl0 = CrisprList.of("0", null, "2", null, "4");
    assertFalse(cl0.remove("1"));
    assertEquals(CrisprList.of("0", null, "2", null, "4"), cl0);
    assertTrue(cl0.remove(null));
    assertEquals(CrisprList.of("0", "2", null, "4"), cl0);
    assertTrue(cl0.remove(null));
    assertEquals(CrisprList.of("0", "2", "4"), cl0);
  }

  @Test
  public void embed00() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = CrisprList.of("a", "b");
    cl0.embed(0, cl1);
    assertEquals(12, cl0.size());
    assertEquals(0, cl1.size());
    assertEquals(CrisprList.of("a", "b", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl0);
  }

  @Test
  public void embed01() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = CrisprList.of("a", "b");
    cl0.embed(10, cl1);
    assertEquals(12, cl0.size());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, "a", "b"), cl0);
  }

  @Test
  public void embed02() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = CrisprList.of("a", "b");
    cl0.embed(9, cl1);
    assertEquals(12, cl0.size());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, "a", "b", 9), cl0);
  }

  @Test
  public void embed03() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = CrisprList.of("a");
    cl0.embed(10, cl1);
    assertEquals(11, cl0.size());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, "a"), cl0);
  }

  @Test
  public void embed05() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = CrisprList.of("a");
    cl0.embed(0, cl1);
    assertEquals(11, cl0.size());
    assertEquals(List.of("a", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl0);
  }

  @Test
  public void embed06() {
    var cl0 = new CrisprList<Object>();
    var cl1 = CrisprList.of("a");
    cl0.embed(0, cl1);
    assertEquals(1, cl0.size());
    assertEquals(List.of("a"), cl0);
  }

  @Test
  public void embed07() {
    var cl0 = new CrisprList<Object>();
    var cl1 = CrisprList.of("a", "b");
    cl0.embed(0, cl1);
    assertEquals(2, cl0.size());
    assertEquals(List.of("a", "b"), cl0);
  }

  @Test
  public void embed08() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = new CrisprList<String>();
    cl0.embed(4, cl1);
    assertEquals(10, cl0.size());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl0);
  }

  @Test
  public void paste00() {
    var cl0 = CrisprList.of("a", "b");
    var cl1 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.paste(cl1, 9);
    assertEquals(0, cl0.size());
    assertEquals(12, cl1.size());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, "a", "b", 9), cl1);
  }

  @Test
  public void attach00() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = CrisprList.of("a", "b");
    cl0.attach(cl1);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, "a", "b"), cl0);
  }

  @Test
  public void transfer00() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = CrisprList.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j');
    cl0.embed(0, cl1, 0, 3);
    assertEquals(13, cl0.size());
    assertEquals(7, cl1.size());
    assertEquals(List.of('a', 'b', 'c', 0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl0);
    assertEquals(List.of('d', 'e', 'f', 'g', 'h', 'i', 'j'), cl1);
  }

  @Test
  public void transfer01() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = CrisprList.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j');
    cl0.embed(2, cl1, 0, 3);
    assertEquals(13, cl0.size());
    assertEquals(7, cl1.size());
    assertEquals(List.of(0, 1, 'a', 'b', 'c', 2, 3, 4, 5, 6, 7, 8, 9), cl0);
    assertEquals(List.of('d', 'e', 'f', 'g', 'h', 'i', 'j'), cl1);
  }

  @Test
  public void transfer02() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = CrisprList.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j');
    cl0.embed(0, cl1, 8, 10);
    assertEquals(12, cl0.size());
    assertEquals(8, cl1.size());
    assertEquals(List.of('i', 'j', 0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl0);
    assertEquals(List.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'), cl1);
  }

  @Test
  public void transfer03() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = CrisprList.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j');
    cl0.embed(4, cl1, 3, 9);
    assertEquals(16, cl0.size());
    assertEquals(4, cl1.size());
    assertEquals(List.of(0, 1, 2, 3, 'd', 'e', 'f', 'g', 'h', 'i', 4, 5, 6, 7, 8, 9),
        cl0);
    assertEquals(List.of('a', 'b', 'c', 'j'), cl1);
  }

  @Test
  public void transfer04() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = CrisprList.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j');
    cl0.embed(4, cl1, 2, 3);
    assertEquals(11, cl0.size());
    assertEquals(9, cl1.size());
    assertEquals(List.of(0, 1, 2, 3, 'c', 4, 5, 6, 7, 8, 9), cl0);
    assertEquals(List.of('a', 'b', 'd', 'e', 'f', 'g', 'h', 'i', 'j'), cl1);
  }

  @Test
  public void transfer05() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = CrisprList.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j');
    cl0.embed(0, cl1, 2, 3);
    assertEquals(11, cl0.size());
    assertEquals(9, cl1.size());
    assertEquals(List.of('c', 0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl0);
    assertEquals(List.of('a', 'b', 'd', 'e', 'f', 'g', 'h', 'i', 'j'), cl1);
  }

  @Test
  public void transfer06() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = CrisprList.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j');
    cl0.embed(10, cl1, 2, 3);
    assertEquals(11, cl0.size());
    assertEquals(9, cl1.size());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 'c'), cl0);
    assertEquals(List.of('a', 'b', 'd', 'e', 'f', 'g', 'h', 'i', 'j'), cl1);
  }

  @Test
  public void transfer07() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = CrisprList.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j');
    cl0.embed(9, cl1, 2, 3);
    assertEquals(11, cl0.size());
    assertEquals(9, cl1.size());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 'c', 9), cl0);
    assertEquals(List.of('a', 'b', 'd', 'e', 'f', 'g', 'h', 'i', 'j'), cl1);
  }

  @Test
  public void transfer08() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = CrisprList.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j');
    cl0.embed(9, cl1, 2, 2); // zero-length segment
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl0);

  }

  @Test
  public void iterator00() {
    var cl = new CrisprList<Integer>();
    var itr = cl.iterator();
    assertFalse(itr.hasNext());
  }

  @Test
  public void iterator01() {
    var cl = CrisprList.of(0);
    var itr = cl.iterator();
    assertTrue(itr.hasNext());
    assertEquals(0, (int) itr.next());
    assertFalse(itr.hasNext());
  }

  @Test
  public void iterator02() {
    var cl = CrisprList.of(0, 1, 2, 3);
    var itr = cl.iterator();
    assertTrue(itr.hasNext());
    assertEquals(0, (int) itr.next());
    assertEquals(1, (int) itr.next());
    assertEquals(2, (int) itr.next());
    assertEquals(3, (int) itr.next());
    assertFalse(itr.hasNext());
  }

  @Test
  public void reverseIterator00() {
    var cl0 = CrisprList.of(0, 1, 2);
    var itr = cl0.reverseIterator();
    assertTrue(itr.hasNext());
    assertEquals(2, (int) itr.next());
    assertTrue(itr.hasNext());
    assertEquals(1, (int) itr.next());
    assertTrue(itr.hasNext());
    assertEquals(0, (int) itr.next());
    assertFalse(itr.hasNext());
  }

  @Test
  public void reverseIterator01() {
    var cl0 = CrisprList.of();
    var itr = cl0.reverseIterator();
    assertFalse(itr.hasNext());
  }

  @Test
  public void toArray00() {
    var cl = CrisprList.of(0, 1, 2, 3);
    assertArrayEquals(pack(0, 1, 2, 3), cl.toArray(new Integer[0]));
    assertArrayEquals(pack(0, 1, 2, 3), cl.toArray(new Integer[4]));
    Integer[] ints = cl.toArray(new Integer[5]);
    assertNull(ints[4]);
  }

  @Test
  public void toArray01() {
    var cl = CrisprList.of(0, 1, 2, 3);
    assertArrayEquals(pack(0, 1, 2, 3), cl.toArray());
    assertSame(EMPTY_OBJECT_ARRAY, CrisprList.of().toArray());
    assertArrayEquals(EMPTY_OBJECT_ARRAY, CrisprList.of().toArray(Object[]::new));
  }

  @Test
  public void set00() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertEquals(2, (int) cl.set(2, 222));
    assertEquals(List.of(0, 1, 222, 3, 4, 5, 6, 7, 8, 9), cl);
    assertEquals(0, (int) cl.set(0, -546));
    assertEquals(List.of(-546, 1, 222, 3, 4, 5, 6, 7, 8, 9), cl);
    assertEquals(9, (int) cl.set(9, 999));
    assertEquals(List.of(-546, 1, 222, 3, 4, 5, 6, 7, 8, 999), cl);
  }

  @Test
  public void set01() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl.set(7, 77, 88, 99);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 77, 88, 99), cl);
  }

  @Test
  public void set02() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl.set(0, -11, 11);
    assertEquals(List.of(-11, 11, 2, 3, 4, 5, 6, 7, 8, 9), cl);
  }

  @Test
  public void set03() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl.set(0, -11, 11, 22);
    assertEquals(List.of(-11, 11, 22, 3, 4, 5, 6, 7, 8, 9), cl);
  }

  @Test
  public void set04() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl.set(2, 22, 33, 44, 55, 66, 77, 88, 99);
    assertEquals(List.of(0, 1, 22, 33, 44, 55, 66, 77, 88, 99), cl);
  }

  @Test(expected = IllegalArgumentException.class)
  public void set05() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl.set(2, 22, 33, 44, 55, 66, 77, 88, 99, 1010);
  }

  @Test
  public void clear00() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertTrue(cl.iterator().hasNext());
    assertTrue(cl.listIterator().hasNext());
    assertFalse(cl.listIterator().hasPrevious());
    assertTrue(cl.wiredIterator().hasNext());
    cl.clear();
    assertEquals(0, cl.size());
    assertFalse(cl.iterator().hasNext());
    assertFalse(cl.listIterator().hasNext());
    assertFalse(cl.listIterator().hasPrevious());
    assertFalse(cl.wiredIterator().hasNext());
    assertEquals(emptyIterator().getClass(), cl.iterator().getClass());
    assertEquals(emptyListIterator().getClass(), cl.listIterator().getClass());
  }

  @Test
  public void setIf00() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl.setIf(1, i -> i > 5, 10);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl);
    cl.setIf(6, i -> i > 5, 10);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 10, 7, 8, 9), cl);
  }

  @Test
  public void indexOf00() {
    var cl = CrisprList.of("00",
        "11",
        "22",
        "33",
        "44",
        "55",
        "66",
        "77",
        "33",
        "88");
    assertEquals(0, cl.indexOf("00"));
    assertEquals(3, cl.indexOf("33"));
    assertEquals(5, cl.indexOf("55"));
    assertEquals(9, cl.indexOf("88"));
    assertEquals(-1, cl.indexOf("99"));
  }

  @Test
  public void indexOf01() {
    var cl = CrisprList.of(0, 1, null, 3, 4, 5, 6, null, 8, 9);
    assertEquals(2, cl.indexOf(null));
  }

  @Test
  public void lastIndexOf00() {
    var cl = CrisprList.of("00",
        "11",
        "22",
        "33",
        "44",
        "55",
        "66",
        "77",
        "33",
        "88");
    assertEquals(0, cl.lastIndexOf("00"));
    assertEquals(8, cl.lastIndexOf("33"));
    assertEquals(5, cl.lastIndexOf("55"));
    assertEquals(9, cl.lastIndexOf("88"));
    assertEquals(-1, cl.lastIndexOf("99"));
  }

  @Test
  public void lastIndexOf01() {
    var cl = CrisprList.of(0, 1, null, 3, 4, 5, 6, null, 8, 9);
    assertEquals(7, cl.lastIndexOf(null));
  }

  @Test
  public void lchop00() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = cl0.lchop(i -> i != 5);
    assertEquals(List.of(5, 6, 7, 8, 9), cl0);
    assertEquals(List.of(0, 1, 2, 3, 4), cl1);
  }

  @Test
  public void lchop01() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = cl0.lchop(i -> i != 0);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl0);
    assertEquals(List.of(), cl1);
  }

  @Test
  public void lchop02() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = cl0.lchop(i -> i != 9);
    assertEquals(List.of(9), cl0);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8), cl1);
  }

  @Test
  public void lchop03() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = cl0.lchop(i -> i != 8);
    assertEquals(List.of(8, 9), cl0);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7), cl1);
  }

  @Test
  public void lchop04() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = cl0.lchop(i -> i == 666);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl0);
    assertTrue(cl1.isEmpty());
  }

  @Test
  public void lchop05() {
    CrisprList<Integer> cl0 = new CrisprList<>();
    var cl1 = cl0.lchop(i -> i == 666);
    assertTrue(cl0.isEmpty());
    assertTrue(cl1.isEmpty());
  }

  @Test
  public void lchop06() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = cl0.lchop(i -> i != 666);
    assertSame(cl0, cl1);
  }

  @Test
  public void rchop00() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = cl0.rchop(i -> i != 5);
    assertEquals(List.of(0, 1, 2, 3, 4, 5), cl0);
    assertEquals(List.of(6, 7, 8, 9), cl1);
  }

  @Test
  public void rchop01() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = cl0.rchop(i -> i != 9);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl0);
    assertEquals(List.of(), cl1);
  }

  @Test
  public void rchop02() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = cl0.rchop(i -> i != 0);
    assertEquals(List.of(0), cl0);
    assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9), cl1);
  }

  @Test
  public void rchop03() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = cl0.rchop(i -> i == 666);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl0);
    assertTrue(cl1.isEmpty());
  }

  @Test
  public void rchop04() {
    CrisprList<Integer> cl0 = new CrisprList<>();
    var cl1 = cl0.rchop(i -> i == 666);
    assertTrue(cl0.isEmpty());
    assertTrue(cl1.isEmpty());
  }

  @Test
  public void rchop06() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var cl1 = cl0.rchop(i -> i != 666);
    assertSame(cl0, cl1);
  }

  @Test
  public void reverse00() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.reverse();
    assertEquals(List.of(9, 8, 7, 6, 5, 4, 3, 2, 1, 0), cl0);
  }

  @Test
  public void reverse01() {
    var cl0 = CrisprList.of(0, 1, 2, 3);
    cl0.reverse();
    assertEquals(List.of(3, 2, 1, 0), cl0);
  }

  @Test
  public void reverse02() {
    var cl0 = CrisprList.of(0, 1, 2);
    cl0.reverse();
    assertEquals(List.of(2, 1, 0), cl0);
  }

  @Test
  public void reverse03() {
    var cl0 = CrisprList.of(0, 1);
    cl0.reverse();
    assertEquals(List.of(1, 0), cl0);
  }

  @Test
  public void reverse04() {
    var cl0 = CrisprList.of(0);
    cl0.reverse();
    assertEquals(List.of(0), cl0);
  }

  @Test
  public void reverse05() {
    var cl0 = CrisprList.of();
    cl0.reverse();
    assertEquals(List.of(), cl0);
  }

  @Test
  public void moveToTail00() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(0, 4, 1);
    assertEquals(List.of(4, 0, 1, 2, 3, 5, 6, 7, 8, 9), cl0);
  }

  @Test
  public void moveToTail01() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(0, 4, 2);
    assertEquals(List.of(4, 5, 0, 1, 2, 3, 6, 7, 8, 9), cl0);
  }

  @Test
  public void moveToTail02() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(0, 4, 3);
    assertEquals(List.of(4, 5, 6, 0, 1, 2, 3, 7, 8, 9), cl0);
  }

  @Test
  public void moveToTail03() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(0, 4, 4);
    assertEquals(List.of(4, 5, 6, 7, 0, 1, 2, 3, 8, 9), cl0);
  }

  @Test
  public void moveToTail04() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(0, 4, 5);
    assertEquals(List.of(4, 5, 6, 7, 8, 0, 1, 2, 3, 9), cl0);
  }

  @Test
  public void moveToTail05() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(0, 4, 6);
    assertEquals(List.of(4, 5, 6, 7, 8, 9, 0, 1, 2, 3), cl0);
  }

  @Test
  public void moveToTail06() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(1, 6, 2);
    assertEquals(List.of(0, 6, 1, 2, 3, 4, 5, 7, 8, 9), cl0);
  }

  @Test
  public void moveToTail7() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(1, 6, 3);
    assertEquals(List.of(0, 6, 7, 1, 2, 3, 4, 5, 8, 9), cl0);
  }

  @Test
  public void moveToTail08() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(1, 6, 4);
    assertEquals(List.of(0, 6, 7, 8, 1, 2, 3, 4, 5, 9), cl0);
  }

  @Test
  public void moveToTail09() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(1, 6, 5);
    assertEquals(List.of(0, 6, 7, 8, 9, 1, 2, 3, 4, 5), cl0);
  }

  @Test
  public void moveToTail10() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(0, 1, 4);
    assertEquals(List.of(1, 2, 3, 4, 0, 5, 6, 7, 8, 9), cl0);
  }

  @Test
  public void moveToTail11() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(7, 9, 8);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 9, 7, 8), cl0);
  }

  @Test
  public void moveToTail12() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(6, 8, 7);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 8, 6, 7, 9), cl0);
  }

  @Test
  public void moveToTail13() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(6, 9, 7);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 9, 6, 7, 8), cl0);
  }

  @Test
  public void moveToTail14() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(7, 8, 8);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 8, 7, 9), cl0);
  }

  @Test
  public void moveToTail15() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(0, 9, 1);
    assertEquals(List.of(9, 0, 1, 2, 3, 4, 5, 6, 7, 8), cl0);
  }

  @Test
  public void moveToHead00() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(6, 8, 4);
    assertEquals(List.of(0, 1, 2, 3, 6, 7, 4, 5, 8, 9), cl0);
  }

  @Test
  public void moveToHead01() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(8, 10, 6);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 8, 9, 6, 7), cl0);
  }

  @Test
  public void moveToHead02() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(8, 10, 6);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 8, 9, 6, 7), cl0);
  }

  @Test
  public void moveToHead03() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(2, 4, 0);
    assertEquals(List.of(2, 3, 0, 1, 4, 5, 6, 7, 8, 9), cl0);
  }

  @Test
  public void moveToHead04() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(1, 2, 0);
    assertEquals(List.of(1, 0, 2, 3, 4, 5, 6, 7, 8, 9), cl0);
  }

  @Test
  public void moveToHead05() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(9, 10, 8);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 9, 8), cl0);
  }

  @Test
  public void moveToHead07() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(3, 8, 1);
    assertEquals(List.of(0, 3, 4, 5, 6, 7, 1, 2, 8, 9), cl0);
  }

  @Test
  public void moveToHead08() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.move(1, 10, 0);
    assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 0), cl0);
  }

  @Test
  public void wiredIterator00() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var itr = cl0.wiredIterator();
    while (itr.hasNext()) {
      itr.next();
      itr.remove();
    }
    assertEquals(0, cl0.size());
  }

  @Test
  public void wiredIterator01() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var itr = cl0.wiredIterator();
    while (itr.hasNext()) {
      int i = itr.next();
      if (i % 2 == 0) {
        itr.remove();
      }
    }
    assertEquals(List.of(1, 3, 5, 7, 9), cl0);
  }

  @Test
  public void wiredIterator02() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var itr = cl0.wiredIterator();
    while (itr.hasNext()) {
      int i = itr.next();
      if (i++ % 2 != 0) {
        itr.remove();
      }
    }
    assertEquals(List.of(0, 2, 4, 6, 8), cl0);
  }

  @Test
  public void wiredIterator03() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var itr = cl0.wiredIterator();
    while (itr.hasNext()) {
      int i = itr.next();
      if (i++ % 3 != 0) {
        itr.remove();
      }
    }
    assertEquals(List.of(0, 3, 6, 9), cl0);
  }

  @Test
  public void wiredIterator04() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var itr = cl0.wiredIterator(true);
    while (itr.hasNext()) {
      itr.next();
      itr.remove();
    }
    assertEquals(0, cl0.size());
  }

  @Test
  public void wiredIterator05() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var itr = cl0.wiredIterator(true);
    while (itr.hasNext()) {
      int i = itr.next();
      if (i % 2 == 0) {
        itr.remove();
      }
    }
    assertEquals(List.of(1, 3, 5, 7, 9), cl0);
  }

  @Test
  public void wiredIterator06() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var itr = cl0.wiredIterator(true);
    while (itr.hasNext()) {
      int i = itr.next();
      if (i % 2 != 0) {
        itr.remove();
      }
    }
    assertEquals(List.of(0, 2, 4, 6, 8), cl0);
  }

  @Test
  public void wiredIterator07() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var itr = cl0.wiredIterator(true);
    while (itr.hasNext()) {
      int i = itr.next();
      if (i++ % 3 != 0) {
        itr.remove();
      }
    }
    assertEquals(List.of(0, 3, 6, 9), cl0);
  }

  @Test
  public void wiredIterator08() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var itr = cl0.wiredIterator();
    assertEquals(0, (int) itr.next());
    assertEquals(1, (int) itr.peek());
    assertEquals(1, (int) itr.next());
    assertEquals(2, (int) itr.peek());
    assertEquals(2, (int) itr.next());
    assertEquals(3, (int) itr.peek());
    itr = itr.turn();
    assertEquals(1, (int) itr.peek());
    assertEquals(1, (int) itr.next());
    assertEquals(0, (int) itr.peek());
    assertEquals(0, (int) itr.next());
    assertFalse(itr.hasNext());
  }

  @Test(expected = IllegalStateException.class)
  public void wiredIterator09() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var itr = cl0.wiredIterator();
    itr.turn();
  }

  @Test(expected = IllegalStateException.class)
  public void wiredIterator10() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var itr = cl0.wiredIterator(true);
    itr.turn();
  }

  @Test
  public void wiredIterator11() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var itr = cl0.wiredIterator(true);
    assertEquals(9, (int) itr.peek());
    assertEquals(9, (int) itr.next());
    assertEquals(8, (int) itr.peek());
    assertEquals(8, (int) itr.next());
    itr = itr.turn();
    assertEquals(9, (int) itr.peek());
    assertEquals(9, (int) itr.next());
    assertFalse(itr.hasNext());
  }

  @Test(expected = IllegalStateException.class)
  public void wiredIterator12() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var itr = cl0.wiredIterator(true);
    itr.set(666);
  }

  @Test(expected = IllegalStateException.class)
  public void wiredIterator13() {
    var cl0 = CrisprList.of(0);
    var itr = cl0.wiredIterator();
    itr.next();
    itr.remove();
    itr.turn();
  }

  @Test
  public void wiredIterator14() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var itr = cl0.wiredIterator(true);
    itr.next(); // 9
    itr.next(); // 8
    itr.next(); // 7
    itr.remove(); // 7 removed and back on 8
    assertEquals(8, (int) itr.value());
    itr = itr.turn();
    assertEquals(8, (int) itr.value());
    itr.next();
    assertEquals(9, (int) itr.value());
    assertFalse(itr.hasNext());
  }

  @Test
  public void wiredIterator15() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    var itr = cl0.wiredIterator();
    itr.next();
    itr.set(100);
    itr.next();
    itr.set(200);
    assertEquals(CrisprList.of(100, 200, 2, 3, 4, 5, 6, 7, 8, 9), cl0);
  }

  @Test
  public void equals00() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5);
    assertTrue(cl.equals(cl));
    assertTrue(cl.equals(List.of(0, 1, 2, 3, 4, 5)));
    assertFalse(cl.equals(List.of(0, 1, 2, 3, 4)));
    assertFalse(cl.equals(List.of(0, 1, 2, 3, 4, 5, 6)));
    assertFalse(cl.equals(List.of()));
    assertFalse(cl.equals(null));
    assertTrue(CrisprList.of().equals(List.of()));
  }

  @Test
  public void defragment00() {
    var cl = CrisprList.<Object>of(0,
        true,
        "Earth",
        1,
        "Jupiter",
        9.5F,
        2,
        'a',
        'b',
        77.23F,
        10.2F,
        "Neptune",
        true,
        3,
        false,
        .86F,
        'c');
    List<Predicate<? super Object>> filters = List.of(e -> e instanceof Float);
    cl.defragment(filters);
    assertEquals(List.<Object>of(9.5F,
        77.23F,
        10.2F,
        .86F,
        0,
        true,
        "Earth",
        1,
        "Jupiter",
        2,
        'a',
        'b',
        "Neptune",
        true,
        3,
        false,
        'c'), cl);
  }

  @Test
  public void defragment01() {
    var cl = CrisprList.<Object>of(0,
        true,
        "Earth",
        1,
        "Jupiter",
        9.5F,
        2,
        'a',
        'b',
        77.23F,
        10.2F,
        "Neptune",
        true,
        3,
        false,
        .86F,
        'c');
    List<Predicate<? super Object>> filters = List.of(e -> e instanceof Float,
        e -> e instanceof Boolean);
    cl.defragment(filters);
    assertEquals(List.<Object>of(9.5F,
        77.23F,
        10.2F,
        .86F,
        true,
        true,
        false,
        0,
        "Earth",
        1,
        "Jupiter",
        2,
        'a',
        'b',
        "Neptune",
        3,
        'c'), cl);
  }

  @Test
  public void defragment02() {
    var cl = CrisprList.<Object>of(0,
        true,
        "Earth",
        1,
        "Jupiter",
        9.5F,
        2,
        'a',
        'b',
        77.23F,
        10.2F,
        "Neptune",
        true,
        3,
        false,
        .86F,
        'c');
    List<Predicate<? super Object>> filters = List.of(e -> e instanceof Float,
        e -> e instanceof Boolean,
        e -> e instanceof String);
    cl.defragment(filters);
    assertEquals(List.<Object>of(9.5F,
        77.23F,
        10.2F,
        .86F,
        true,
        true,
        false,
        "Earth",
        "Jupiter",
        "Neptune",
        0,
        1,
        2,
        'a',
        'b',
        3,
        'c'), cl);
  }

  @Test
  public void defragment03() {
    var cl = CrisprList.<Object>of(0,
        true,
        "Earth",
        1,
        "Jupiter",
        9.5F,
        2,
        'a',
        'b',
        77.23F,
        10.2F,
        "Neptune",
        true,
        3,
        false,
        .86F,
        'c');
    List<Predicate<? super Object>> filters = List.of(e -> e instanceof Float,
        e -> e instanceof Boolean,
        e -> e instanceof String,
        e -> e instanceof Integer);
    cl.defragment(filters);
    assertEquals(List.<Object>of(9.5F,
        77.23F,
        10.2F,
        .86F,
        true,
        true,
        false,
        "Earth",
        "Jupiter",
        "Neptune",
        0,
        1,
        2,
        3,
        'a',
        'b',
        'c'), cl);
  }

  @Test
  public void defragment04() {
    var cl = CrisprList.<Object>of(0,
        true,
        "Earth",
        1,
        "Jupiter",
        9.5F,
        2,
        'a',
        'b',
        77.23F,
        10.2F,
        "Neptune",
        true,
        3,
        false,
        .86F,
        'c');
    // ALWAYS FALSE
    List<Predicate<? super Object>> filters = List.of(Objects::isNull);
    cl.defragment(filters);
    assertEquals(List.<Object>of(0,
        true,
        "Earth",
        1,
        "Jupiter",
        9.5F,
        2,
        'a',
        'b',
        77.23F,
        10.2F,
        "Neptune",
        true,
        3,
        false,
        .86F,
        'c'), cl);
  }

  @Test
  public void defragment05() {
    var cl = CrisprList.<Object>of(0,
        true,
        "Earth",
        1,
        "Jupiter",
        9.5F,
        2,
        'a',
        'b',
        77.23F,
        10.2F,
        "Neptune",
        true,
        3,
        false,
        .86F,
        'c');
    // ALWAYS TRUE
    List<Predicate<? super Object>> filters = List.of(Objects::nonNull);
    cl.defragment(filters);
    assertEquals(List.<Object>of(0,
        true,
        "Earth",
        1,
        "Jupiter",
        9.5F,
        2,
        'a',
        'b',
        77.23F,
        10.2F,
        "Neptune",
        true,
        3,
        false,
        .86F,
        'c'), cl);
  }

  @Test
  public void defragment06() {
    var cl = CrisprList.of("sss", "bbb", "bbb.a", "aaa", "xxx", "xxx.c");
    var cl2 = cl.defragment(List.of(
        s -> s.startsWith("bb"),
        s -> s.startsWith("xx")));
    assertEquals(List.of("bbb", "bbb.a", "xxx", "xxx.c", "sss", "aaa"), cl2);
  }

  @Test
  public void defragment07() {
    var cl = CrisprList.of("sss", "bbb", "bbb.a", "aaa", "xxx", "xxx.c");
    var cl2 = cl.defragment(false, List.of(
        s -> s.startsWith("bb"),
        s -> s.startsWith("xx")));
    assertEquals(List.of("bbb", "bbb.a", "xxx", "xxx.c"), cl2);
  }

  @Test
  public void group00() {
    var cl = CrisprList.<Object>of(0,
        true, "Earth", 1, "Jupiter", 9.5F, 2, 'a', 'b', 77.23F,
        10.2F, "Neptune", true, 3, false, .86F, 'c');
    List<CrisprList<Object>> groups = cl.group(List.of(e -> e instanceof Float));
    assertEquals(2, groups.size());
    assertEquals(List.of(9.5F, 77.23F, 10.2F, .86F), groups.get(0));
    List<? extends Serializable> expected = List.of(0, true, "Earth", 1, "Jupiter",
        2, 'a', 'b', "Neptune", true, 3, false, 'c');
    assertEquals(expected, groups.get(1));
  }

  @Test
  public void group01() {
    var cl = CrisprList.<Object>of(0,
        true, "Earth", 1, "Jupiter", 9.5F, 2, 'a', 'b', 77.23F,
        10.2F, "Neptune", true, 3, false, .86F, 'c');
    List<Predicate<? super Object>> filters = List.of(e -> e instanceof Float,
        e -> e instanceof Boolean,
        e -> e instanceof String,
        e -> e instanceof Integer);
    List<CrisprList<Object>> groups = cl.group(filters);
    assertEquals(5, groups.size());
    assertEquals(List.of(9.5F, 77.23F, 10.2F, .86F), groups.get(0));
    assertEquals(List.of(true, true, false), groups.get(1));
    assertEquals(List.of("Earth", "Jupiter", "Neptune"), groups.get(2));
    assertEquals(List.of(0, 1, 2, 3), groups.get(3));
    assertEquals(List.of('a', 'b', 'c'), groups.get(4));
  }

  @Test
  public void group02() {
    var cl = CrisprList.<Object>of(0,
        true, "Earth", 1, "Jupiter", 9.5F, 2, 'a', 'b', 77.23F,
        10.2F, "Neptune", true, 3, false, .86F, 'c');
    List<Predicate<? super Object>> filters = List.of(e -> e instanceof Float,
        e -> e instanceof Boolean,
        e -> e instanceof String,
        e -> e instanceof Integer);
    // Just to make sure we can use any combination we like
    List<List<Object>> groups0 = cl.group(filters);
    CrisprList<CrisprList<Object>> groups1 = cl.group(filters);
    List<CrisprList<Object>> groups2 = cl.group(filters);
    CrisprList<List<Object>> groups3 = cl.group(filters);
  }

  @Test
  public void removeIf00() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl.removeIf(i -> i % 2 == 0);
    assertEquals(List.of(1, 3, 5, 7, 9), cl);
  }

  @Test
  public void removeIf01() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl.removeIf(i -> i % 2 != 0);
    assertEquals(List.of(0, 2, 4, 6, 8), cl);
  }

  @Test
  public void removeIf02() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl.removeIf(Objects::isNull);
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl);
  }

  @Test
  public void removeIf03() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl.removeIf(Objects::nonNull);
    assertEquals(List.of(), cl);
  }

  @Test
  public void removeAll00() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertTrue(cl.removeAll(Set.of(3, 5, 7)));
    assertEquals(List.of(0, 1, 2, 4, 6, 8, 9), cl);
  }

  @Test
  public void removeAll01() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertFalse(cl.removeAll(Set.of(13, 17, 23)));
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl);
  }

  @Test
  public void retainAll00() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertTrue(cl.retainAll(Set.of(3, 5, 7)));
    assertEquals(List.of(3, 5, 7), cl);
  }

  @Test
  public void retainAll01() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertTrue(cl.retainAll(Set.of(13, 17, 23)));
    assertEquals(List.of(), cl);
  }

  @Test
  public void retainAll02() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertFalse(cl.retainAll(Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)));
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl);
  }

  @Test
  public void contains00() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertFalse(cl.contains(47));
    //assertTrue(cl.contains(6));
  }

  @Test
  public void containsAll00() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertFalse(cl.containsAll(Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
    assertTrue(cl.containsAll(Set.of(3, 4, 5, 6, 7, 8, 9)));
  }

  @Test
  public void partition00() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    List<CrisprList<Integer>> wls = cl.partition(5);
    assertEquals(2, wls.size());
    assertEquals(List.of(0, 1, 2, 3, 4), wls.get(0));
    assertEquals(List.of(5, 6, 7, 8, 9), wls.get(1));
    assertSame(cl, wls.get(1));
  }

  @Test
  public void partition01() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    List<CrisprList<Integer>> wls = cl.partition(3);
    assertEquals(4, wls.size());
    assertEquals(List.of(0, 1, 2), wls.get(0));
    assertEquals(List.of(3, 4, 5), wls.get(1));
    assertEquals(List.of(6, 7, 8), wls.get(2));
    assertEquals(List.of(9), wls.get(3));
    assertSame(cl, wls.get(3));
  }

  @Test
  public void partition02() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    List<CrisprList<Integer>> wls = cl.partition(1); // odd, but allowed
    assertEquals(10, wls.size());
    assertEquals(List.of(0), wls.get(0));
    assertEquals(List.of(4), wls.get(4));
    assertEquals(List.of(8), wls.get(8));
    assertEquals(List.of(9), wls.get(9));
    assertSame(cl, wls.get(9));
  }

  @Test
  public void partition03() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    List<CrisprList<Integer>> wls = cl.partition(100);
    assertEquals(1, wls.size());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), wls.get(0));
    assertSame(cl, wls.get(0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void partition04() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl.partition(0);
  }

  @Test
  public void split00() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    List<CrisprList<Integer>> wls = cl.split(2);
    assertEquals(2, wls.size());
    assertEquals(List.of(0, 1, 2, 3, 4), wls.get(0));
    assertEquals(List.of(5, 6, 7, 8, 9), wls.get(1));
    assertSame(cl, wls.get(1));
  }

  @Test
  public void split01() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    List<CrisprList<Integer>> wls = cl.split(1000);
    assertEquals(10, wls.size());
    assertEquals(List.of(0), wls.get(0));
    assertEquals(List.of(4), wls.get(4));
    assertEquals(List.of(8), wls.get(8));
    assertEquals(List.of(9), wls.get(9));
    assertSame(cl, wls.get(9));
  }

  @Test
  public void split02() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    List<CrisprList<Integer>> wls = cl.split(1);
    assertEquals(1, wls.size());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), wls.get(0));
    assertSame(cl, wls.get(0));
  }

  @Test
  public void copy00() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4);
    var cl1 = cl0.copy();
    assertEquals(cl0, cl1);
    cl1.set(2, 88);
    assertNotEquals(cl0, cl1);
  }

  @Test
  public void copy01() {
    var cl0 = CrisprList.of(0);
    var cl1 = cl0.copy();
    assertEquals(cl0, cl1);
  }

  @Test
  public void copy02() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4);
    var cl1 = cl0.copy(1, cl0.size());
    assertEquals(CrisprList.of(1, 2, 3, 4), cl1);
    cl1 = cl0.copy(1, 1);
    assertEquals(CrisprList.of(), cl1);
    cl1 = cl0.copy(5, 5);
    assertEquals(CrisprList.of(), cl1);
    cl1 = cl0.copy(0, 5);
    assertEquals(cl0, cl1);
    cl1 = cl0.copy(0, 3);
    assertEquals(CrisprList.of(0, 1, 2), cl1);
  }

  @Test
  public void copy03() {
    var cl0 = CrisprList.of();
    var cl1 = cl0.copy(0, 0);
    assertEquals(cl0, cl1);
  }

  @Test
  public void copy04() {
    var cl0 = CrisprList.of();
    var cl1 = cl0.copy();
    assertEquals(cl0, cl1);
  }

  @Test
  public void replaceAll00() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.replace(0, 4, List.of("a", "b", "c"));
    assertEquals(List.of("a", "b", "c", 4, 5, 6, 7, 8, 9), cl0);
  }

  @Test
  public void replaceAll01() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.replace(0, 10, List.of("a", "b", "c"));
    assertEquals(List.of("a", "b", "c"), cl0);
  }

  @Test
  public void replaceAll02() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.replace(4, 10, List.of("a", "b", "c"));
    assertEquals(List.of(0, 1, 2, 3, "a", "b", "c"), cl0);
  }

  @Test
  public void replaceAll04() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.replace(4, 8, List.of("a", "b", "c"));
    assertEquals(List.of(0, 1, 2, 3, "a", "b", "c", 8, 9), cl0);
  }

  @Test
  public void replaceAll05() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.replace(4, 8, List.of("a", "b", "c", "d"));
    assertEquals(List.of(0, 1, 2, 3, "a", "b", "c", "d", 8, 9), cl0);
  }

  @Test
  public void replaceAll06() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.replace(4, 4, List.of("a", "b", "c", "d"));
    assertEquals(List.of(0, 1, 2, 3, "a", "b", "c", "d", 4, 5, 6, 7, 8, 9), cl0);
  }

  @Test
  public void replaceAll07() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.replace(9, 10, List.of("a"));
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, "a"), cl0);
  }

  @Test
  public void replaceAll08() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.replace(9, 9, List.of());
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), cl0);
  }

  @Test
  public void replaceAll09() {
    var cl0 = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.replaceAll(i -> i * 2);
    assertEquals(List.of(0, 2, 4, 6, 8, 10, 12, 14, 16, 18), cl0);
  }

  @Test
  public void replaceAll10() {
    var cl0 = CrisprList.<Integer>of();
    cl0.replaceAll(i -> i * 2);
    assertEquals(List.of(), cl0);
  }

  @Test
  public void replaceSegment00() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.rewire(0, 4, CrisprList.of("a", "b", "c"));
    assertEquals(List.of("a", "b", "c", 4, 5, 6, 7, 8, 9), cl0);
  }

  @Test
  public void replaceSegment01() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.rewire(0, 10, CrisprList.of("a", "b", "c"));
    assertEquals(List.of("a", "b", "c"), cl0);
  }

  @Test
  public void replaceSegment02() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.rewire(4, 10, CrisprList.of("a", "b", "c"));
    assertEquals(List.of(0, 1, 2, 3, "a", "b", "c"), cl0);
  }

  @Test
  public void replaceSegment03() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl.rewire(0, 3, CrisprList.of());
    assertEquals(List.of(3, 4, 5, 6, 7, 8, 9), cl);
  }

  @Test
  public void replaceSegment04() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    cl0.rewire(4, 8, CrisprList.of("a", "b", "c"));
    assertEquals(List.of(0, 1, 2, 3, "a", "b", "c", 8, 9), cl0);
  }

  @Test
  public void toString00() {
    var cl0 = CrisprList.of(0, 1, 2, 3);
    assertEquals("[0, 1, 2, 3]", cl0.toString());
    cl0 = CrisprList.of();
    assertEquals("[]", cl0.toString());
  }

  @Test
  public void shrink00() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5);
    assertEquals(List.of(0, 1, 2, 3), cl.shrink(0, 4));
  }

  @Test
  public void shrink01() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5);
    assertEquals(List.of(3, 4, 5), cl.shrink(3, 6));
  }

  @Test
  public void shrink02() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5);
    assertEquals(List.of(1, 2, 3, 4), cl.shrink(1, 5));
  }

  @Test
  public void shrink03() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5);
    assertEquals(List.of(0, 1, 2, 3, 4, 5), cl.shrink(0, 6));
  }

  @Test
  public void shrink04() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5);
    assertEquals(List.of(2), cl.shrink(2, 3));
  }

  @Test
  public void shrink05() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5);
    assertEquals(List.of(), cl.shrink(2, 2));
  }

  @Test
  public void shrink06() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5);
    assertEquals(List.of(), cl.shrink(0, 0));
  }

  @Test
  public void shrink07() {
    var cl = CrisprList.of(0, 1, 2, 3, 4, 5);
    assertEquals(List.of(), cl.shrink(6, 6));
  }

  @Test
  public void exchange00() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5);
    var cl1 = CrisprList.<Object>of('0', '1', '2', '3', '4', '5');
    cl0.exchange(1, 3, cl1, 1, 3);
    assertEquals(List.of(0, '1', '2', 3, 4, 5), cl0);
    assertEquals(List.of('0', 1, 2, '3', '4', '5'), cl1);
  }

  @Test
  public void exchange01() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5);
    var cl1 = CrisprList.<Object>of('0', '1', '2', '3', '4', '5');
    cl0.exchange(0, 3, cl1, 1, 3);
    assertEquals(List.of('1', '2', 3, 4, 5), cl0);
    assertEquals(List.of('0', 0, 1, 2, '3', '4', '5'), cl1);
  }

  @Test
  public void exchange02() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5);
    var cl1 = CrisprList.<Object>of('0', '1', '2', '3', '4', '5');
    cl0.exchange(3, 6, cl1, 5, 6);
    assertEquals(List.of(0, 1, 2, '5'), cl0);
    assertEquals(List.of('0', '1', '2', '3', '4', 3, 4, 5), cl1);
  }

  @Test
  public void exchange03() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5);
    var cl1 = CrisprList.<Object>of('0', '1', '2', '3', '4', '5');
    cl0.exchange(0, 6, cl1, 0, 6);
    assertEquals(List.of('0', '1', '2', '3', '4', '5'), cl0);
    assertEquals(List.of(0, 1, 2, 3, 4, 5), cl1);
  }

  @Test
  public void exchange04() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5);
    var cl1 = CrisprList.<Object>of("00", "11", "22", "33", "44", "55");
    cl0.exchange(4, 4, cl1, 1, 5);
    assertEquals(List.of(0, 1, 2, 3, "11", "22", "33", "44", 4, 5), cl0);
    assertEquals(List.of("00", "55"), cl1);
  }

  @Test
  public void exchange05() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5);
    var cl1 = CrisprList.<Object>of("00", "11", "22", "33", "44", "55");
    cl0.exchange(0, 6, cl1, 1, 1);
    assertEquals(List.of(), cl0);
    assertEquals(List.of("00", 0, 1, 2, 3, 4, 5, "11", "22", "33", "44", "55"), cl1);
  }

  @Test
  public void exchange06() {
    var cl0 = CrisprList.<Object>of(0, 1, 2, 3, 4, 5);
    var cl1 = CrisprList.<Object>of("00", "11", "22", "33", "44", "55");
    cl0.exchange(5, 5, cl1, 1, 1);
    assertEquals(List.of(0, 1, 2, 3, 4, 5), cl0);
    assertEquals(List.of("00", "11", "22", "33", "44", "55"), cl1);
  }

}
