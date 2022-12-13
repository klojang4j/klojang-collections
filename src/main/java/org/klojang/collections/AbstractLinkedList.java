package org.klojang.collections;

import org.klojang.check.Check;
import org.klojang.check.Tag;
import org.klojang.util.CollectionMethods;
import org.klojang.util.InvokeMethods;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyListIterator;
import static org.klojang.check.CommonChecks.*;
import static org.klojang.check.CommonExceptions.indexOutOfBounds;
import static org.klojang.check.CommonExceptions.noSuchElement;
import static org.klojang.check.CommonProperties.length;
import static org.klojang.util.ArrayMethods.EMPTY_OBJECT_ARRAY;

abstract sealed class AbstractLinkedList<E> implements List<E>
    permits WiredList, CrisprList {

  static Supplier<IllegalArgumentException> autoEmbedNotAllowed() {
    return () -> new IllegalArgumentException("list cannot be embedded within itself");
  }

  static Supplier<IllegalStateException> callNextFirst() {
    return () -> new IllegalStateException("Iterator.next() must be called first");
  }

  static Supplier<IllegalStateException> emptyList() {
    return () -> new IllegalStateException("illegal operation on empty list");
  }

  static Function<String, IllegalArgumentException> emptySegment() {
    return s -> new IllegalArgumentException("zero-length segment not allowed");
  }

  static Supplier<ConcurrentModificationException> concurrentModification() {
    return ConcurrentModificationException::new;
  }

  static Function<String, IllegalArgumentException> overlapNotAllowed() {
    return s -> new IllegalArgumentException("list segments must not overlap");
  }

  //
  //
  //
  // ======================================================= //
  // ======================= [ Node ] ====================== //
  // ======================================================= //
  //
  //
  //

  static final class Node<V> {

    V val;
    Node<V> prev;
    Node<V> next;

    Node(V val) {this.val = val;}

    Node(Node<V> prev, V val) {
      this.prev = prev;
      this.val = val;
      prev.next = this;
    }

    V value() {
      return val;
    }

    public String toString() {
      return String.valueOf(val);
    }

  }

  //
  //
  //
  // ======================================================= //
  // ====================== [ Chain ] ====================== //
  // ======================================================= //
  //
  //
  //

  @SuppressWarnings({"unchecked", "rawtypes"})
  static final class Chain {

    // must (and will) only be called if values.size() > 0
    static <V> Chain of(Collection<V> values) {
      if (values instanceof AbstractLinkedList l) {
        return copyOf(l.head, l.size());
      }
      Iterator<V> itr = values.iterator();
      var head = new Node<>(itr.next());
      var tail = head;
      while (itr.hasNext()) {
        tail = new Node<>(tail, itr.next());
      }
      return new Chain(head, tail, values.size());
    }

    static Chain copyOf(Node node, int len) {
      var head = new Node(node.val);
      var tail = head;
      for (int i = 1; i < len; ++i) {
        tail = new Node(tail, (node = node.next).val);
      }
      return new Chain(head, tail, len);
    }

    final Node head;
    final Node tail;
    final int length;

    Chain(Node head, Node tail, int length) {
      this.head = head;
      this.tail = tail;
      this.length = length;
    }

  }

  /*
   * NB The asymmetry between next/hasNext and previous/hasPrevious is no code
   * sloth. It is due to the specification of the ListIterator interface and the
   * List.listIterator(index) method.
   */
  class ListItr implements ListIterator<E> {

    Node<E> curr;
    Boolean forward;
    int idx;

    ListItr() {
      curr = head;
      idx = 0;
    }

    ListItr(int index) {
      if ((idx = index) == sz) {
        curr = null;
      } else {
        curr = nodeAt(index);
      }
    }

    @Override
    public boolean hasNext() {
      return (forward == null && sz != 0) || curr != tail;
    }

    @Override
    public E next() {
      if (forward == TRUE) {
        Check.that(curr).isNot(sameAs(), tail, noSuchElement());
        Check.that(++idx).is(lt(), sz, concurrentModification());
        return Check.that(curr = curr.next)
            .is(notNull(), concurrentModification())
            .ok(Node::value);
      }
      forward = TRUE;
      return curr.val;
    }

    @Override
    public boolean hasPrevious() {
      return curr != head;
    }

    @Override
    public E previous() {
      E val;
      if (idx == sz) {
        Check.that(--idx).is(gte(), 0, concurrentModification());
        val = Check.that(curr = tail)
            .is(notNull(), concurrentModification())
            .ok(Node::value);
      } else if (forward != TRUE) {
        Check.that(curr).isNot(sameAs(), head, noSuchElement());
        Check.that(--idx).is(gte(), 0, concurrentModification());
        val = Check.that(curr = curr.prev)
            .is(notNull(), concurrentModification())
            .ok(Node::value);
      } else {
        val = curr.val;
      }
      forward = FALSE;
      return val;
    }

    @Override
    public int nextIndex() {
      return forward == TRUE ? idx + 1 : idx;
    }

    @Override
    public int previousIndex() {
      return forward == TRUE ? idx : idx - 1;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings({"unused"})
    public void set(E value) {
      throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings({"unused"})
    public void add(E value) {
      throw new UnsupportedOperationException();
    }

  }

  //
  //
  //
  // ======================================================= //
  // ================ [ AbstractLinkedList ] =============== //
  // ======================================================= //
  //
  //
  //

  final String className = getClass().getSimpleName();

  Node<E> head;
  Node<E> tail;
  int sz;

  /**
   * Returns the index of the first occurrence of the specified element in this list,
   * or -1 if this list does not contain the element. More formally, returns the
   * lowest index {@code i} such that {@code Objects.equals(o, get(i))}, or -1 if
   * there is no such index.
   *
   * @param o element to search for
   * @return the index of the first occurrence of the specified element in this list,
   *     or -1 if this list does not contain the element
   */
  @Override
  public int indexOf(Object o) {
    var n = head;
    if (o == null) {
      for (int i = 0; i < sz; ++i) {
        if (n.val == null) {
          return i;
        }
        n = n.next;
      }
    } else {
      for (int i = 0; i < sz; ++i) {
        if (o.equals(n.val)) {
          return i;
        }
        n = n.next;
      }
    }
    return -1;
  }

  /**
   * Returns the index of the last occurrence of the specified element in this list,
   * or -1 if this list does not contain the element. More formally, returns the
   * highest index {@code i} such that {@code Objects.equals(o, get(i))}, or -1 if
   * there is no such index.
   *
   * @param o element to search for
   * @return the index of the last occurrence of the specified element in this list,
   *     or -1 if this list does not contain the element
   */
  @Override
  public int lastIndexOf(Object o) {
    var n = tail;
    if (o == null) {
      for (int i = sz - 1; i >= 0; --i) {
        if (n.val == null) {
          return i;
        }
        n = n.prev;
      }
    } else {
      for (int i = sz - 1; i >= 0; --i) {
        if (o.equals(n.val)) {
          return i;
        }
        n = n.prev;
      }
    }
    return -1;
  }

  /**
   * Returns the element at the specified position in this list.
   *
   * @param index index of the element to return
   * @return the element at the specified position in this list
   * @throws IndexOutOfBoundsException if the index is out of range
   *     ({@code index < 0 || index >= size()})
   */
  @Override
  public E get(int index) {
    return node(index).val;
  }

  /**
   * Replaces the element at the specified position in this list with the specified
   * element.
   *
   * @param index index of the element to replace
   * @param element element to be stored at the specified position
   * @return the element previously at the specified position
   */
  @Override
  public E set(int index, E element) {
    var node = node(index);
    E old = node.val;
    node.val = element;
    return old;
  }

  /**
   * Appends the specified element to the end of this list.
   *
   * @param e element to be appended to this list
   * @return {@code true} (as specified by {@link Collection#add})
   */
  @Override
  public boolean add(E e) {
    Node<E> n = new Node<>(e);
    if (sz == 0) {
      head = tail = n;
    } else {
      join(tail, n);
      tail = n;
    }
    ++sz;
    return true;
  }

  /**
   * Inserts the specified element at the specified position in this list. Shifts the
   * element currently at that position (if any) and any subsequent elements to the
   * right (adds one to their indices).
   *
   * @param index index at which the specified element is to be inserted
   * @param element element to be inserted
   */
  @Override
  public void add(int index, E element) {
    checkInclusive(index);
    insert(index, new Node<>(element));
  }

  /**
   * Appends all elements in the specified collection to the end of this list, in the
   * order that they are returned by the specified collection's iterator.
   *
   * @param c collection containing elements to be added to this list
   * @return {@code true} if this list changed as a result of the call
   * @see #add(Object)
   */
  @Override
  public boolean addAll(Collection<? extends E> c) {
    return addAll(sz, c);
  }

  /**
   * Inserts all elements in the specified collection into this list at the specified
   * position (optional operation). Shifts the element currently at that position (if
   * any) and any subsequent elements to the right (increases their indices).
   *
   * @param index index at which to insert the first element from the specified
   *     collection
   * @param c collection containing elements to be added to this list
   * @return {@code true} if this list changed as a result of the call
   */
  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    checkInclusive(index);
    Check.notNull(c, Tag.COLLECTION);
    if (!c.isEmpty()) {
      insert(index, Chain.of(c));
    }
    return !c.isEmpty();
  }

  /**
   * Returns the number of elements in this list. If this list contains more than
   * {@code Integer.MAX_VALUE} elements, returns {@code Integer.MAX_VALUE}.
   *
   * @return the number of elements in this list
   */
  @Override
  public int size() {
    return sz;
  }

  /**
   * Returns {@code true} if this list contains no elements.
   *
   * @return {@code true} if this list contains no elements
   */
  @Override
  public boolean isEmpty() {
    return sz == 0;
  }

  /**
   * Returns {@code true} if this list contains the specified element. More formally,
   * returns {@code true} if and only if this list contains at least one element
   * {@code e} such that {@code Objects.equals(o, e)}.
   *
   * @param o element whose presence in this list is to be tested
   * @return {@code true} if this list contains the specified element
   */
  @Override
  public boolean contains(Object o) {
    return indexOf(o) != -1;
  }

  /**
   * Returns {@code true} if this list contains all of the elements of the specified
   * collection.
   *
   * @param c collection to be checked for containment in this list
   * @return {@code true} if this list contains all of the elements of the specified
   *     collection
   * @see #contains(Object)
   */
  @Override
  public boolean containsAll(Collection<?> c) {
    Check.notNull(c);
    return new HashSet<>(this).containsAll(c);
  }

  /**
   * Returns an {@code Iterator} that traverses the list from the first element to
   * the last.
   *
   * @return an {@code Iterator} that traverses the list's elements from first to the
   *     last
   */
  @Override
  public Iterator<E> iterator() {
    return sz == 0 ? emptyIterator() : new Iterator<>() {

      private Node<E> curr = justBeforeHead();

      @Override
      public boolean hasNext() {
        return curr != tail && curr.next != null;
      }

      @Override
      public E next() {
        Check.that(curr).isNot(sameAs(), tail, noSuchElement());
        return Check.that(curr = curr.next)
            .is(notNull(), concurrentModification())
            .ok(Node::value);
      }
    };
  }

  Iterator<E> reverseIterator0() {
    return sz == 0 ? emptyIterator() : new Iterator<>() {

      private Node<E> curr = justAfterTail();

      @Override
      public boolean hasNext() {
        return curr != head;
      }

      @Override
      public E next() {
        Check.that(curr).isNot(sameAs(), head, noSuchElement());
        return Check.that(curr = curr.prev)
            .is(notNull(), concurrentModification())
            .ok(Node::value);
      }
    };
  }

  /**
   * Returns a list iterator over the elements in this list (in proper sequence).
   *
   * @return a list iterator over the elements in this list (in proper sequence)
   */
  @Override
  public ListIterator<E> listIterator() {
    return isEmpty() ? emptyListIterator() : new ListItr();
  }

  /**
   * Returns a list iterator over the elements in this list (in proper sequence),
   * starting at the specified position in the list. The specified index indicates
   * the first element that would be returned by an initial call to
   * {@link ListIterator#next next}. An initial call to
   * {@link ListIterator#previous previous} would return the element with the
   * specified index minus one.
   *
   * @param index index of the first element to be returned from the list
   *     iterator (by a call to {@link ListIterator#next next})
   * @return a list iterator over the elements in this list (in proper sequence),
   *     starting at the specified position in the list
   * @throws IndexOutOfBoundsException if the index is out of range
   *     ({@code index < 0 || index > size()})
   */
  @Override
  public ListIterator<E> listIterator(int index) {
    checkInclusive(index);
    if (isEmpty()) {
      return emptyListIterator();
    }
    return new ListItr(index);
  }

  /**
   * Compares the specified object with this list for equality.  Returns {@code true}
   * if and only if the specified object is also a list, both lists have the same
   * size, and all corresponding pairs of elements in the two lists are <i>equal</i>.
   * (Two elements {@code e1} and {@code e2} are <i>equal</i> if
   * {@code Objects.equals(e1, e2)}.) In other words, two lists are defined to be
   * equal if they contain the same elements in the same order.  This definition
   * ensures that the equals method works properly across different implementations
   * of the {@code List} interface.
   *
   * @param o the object to be compared for equality with this list
   * @return {@code true} if the specified object is equal to this list
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o instanceof List<?> l) {
      if (sz == 0) {
        return l.size() == 0;
      } else if (sz == l.size()) {
        var x = head;
        for (Object obj : l) {
          if (!Objects.equals(obj, x.val)) {
            return false;
          }
          x = x.next;
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the hash code value for this list.
   *
   * @return the hash code value for this list
   * @see Object#equals(Object)
   * @see #equals(Object)
   */
  @Override
  public int hashCode() {
    int hash = 1;
    for (E val : this) {
      hash = 31 * hash + Objects.hashCode(val);
    }
    return hash;
  }

  /**
   * Returns a {@code String} representation of this list.
   *
   * @return a {@code String} representation of this list
   */
  @Override
  public String toString() {
    return '[' + CollectionMethods.implode(this) + ']';
  }

  @SafeVarargs
  final void set0(int index, E e0, E e1, E... moreElems) {
    Check.that(index).is(indexInclusiveOf(), this, indexOutOfBounds(index));
    Check.notNull(moreElems, Tag.VARARGS).has(length(), lte(), sz - index - 2);
    var node = nodeAt(index);
    node.val = e0;
    node = node.next;
    node.val = e1;
    if (moreElems.length != 0) {
      node = node.next;
      for (E e : moreElems) {
        node.val = e;
        node = node.next;
      }
    }
  }

  final E setIf0(int index, Predicate<? super E> condition, E value) {
    Check.notNull(condition, Tag.TEST);
    var node = node(index);
    E old = node.val;
    if (condition.test(old)) {
      node.val = value;
    }
    return old;
  }

  final void prepend0(E value) {
    Node<E> n = new Node<>(value);
    if (sz == 0) {
      head = tail = n;
    } else {
      join(n, head);
      head = n;
    }
    ++sz;
  }

  final void replace0(int fromIndex, int toIndex, Collection<? extends E> values) {
    int len = Check.fromTo(this, fromIndex, toIndex);
    Check.notNull(values, Tag.COLLECTION);
    if (len == 0) {
      if (!values.isEmpty()) {
        insert(fromIndex, Chain.of(values));
      }
    } else if (len == values.size()) {
      var node = nodeAt(fromIndex);
      for (E e : values) {
        node.val = e;
        node = node.next;
      }
    } else {
      unlink(fromIndex, toIndex);
      if (!values.isEmpty()) {
        insert(fromIndex, Chain.of(values));
      }
    }
  }

  final void reverse0() {
    if (sz > 1) {
      var x = head;
      var y = tail;
      for (int i = 0; i < sz / 2; ++i) {
        E tmp = x.val;
        x.val = y.val;
        y.val = tmp;
        x = x.next;
        y = y.prev;
      }
    }
  }

  final void swap0(int from1, int to1, int from2, int to2) {
    int len1 = Check.fromTo(this, from1, to1);
    int len2 = Check.fromTo(this, from2, to2);
    Check.on(emptySegment(), len1).is(ne(), 0).and(len2).is(ne(), 0);

    int x0, x1, y0, y1;
    if (from1 < from2) {
      x0 = from1;
      x1 = to1;
      y0 = from2;
      y1 = to2;
    } else {
      x0 = from2;
      x1 = to2;
      y0 = from1;
      y1 = to1;
    }

    Check.on(overlapNotAllowed(), x1).is(lte(), y0);

    var seg1L = nodeAt(x0);
    var seg1R = nodeAfter(seg1L, x0, x1 - 1);
    var seg2L = nodeAfter(seg1R, x1 - 1, y0);
    var seg2R = nodeAfter(seg2L, y0, y1 - 1);

    if (x1 == y0) {
      if (seg2R == tail) {
        makeTail(seg1R);
      } else {
        join(seg1R, seg2R.next);
      }
      if (seg1L == head) {
        makeHead(seg2L);
      } else {
        join(seg1L.prev, seg2L);
      }
      join(seg2R, seg1L);
    } else {
      if (seg1L == head) {
        head = seg2L;
      } else {
        seg1L.prev.next = seg2L;
      }
      if (seg2R == tail) {
        tail = seg1R;
      } else {
        seg2R.next.prev = seg1R;
      }
      seg1R.next.prev = seg2R;
      seg2L.prev.next = seg1L;
      var tmp = seg1L.prev;
      seg1L.prev = seg2L.prev;
      seg2L.prev = tmp;
      tmp = seg1R.next;
      seg1R.next = seg2R.next;
      seg2R.next = tmp;
    }
  }

  void moveRight(int from, int to, int newFrom) {
    int indexOfLast = to - 1;
    int steps = newFrom - from;
    Node<E> first = nodeAt(from);
    Node<E> last = nodeAfter(first, from, indexOfLast);
    Node<E> insertAfter = nodeAfter(last, indexOfLast, indexOfLast + steps);
    if (first == head) {
      makeHead(last.next);
    } else {
      join(first.prev, last.next);
    }
    if (insertAfter == tail) {
      join(insertAfter, first);
      makeTail(last);
    } else {
      join(last, insertAfter.next);
      join(insertAfter, first);
    }
  }

  void moveLeft(int from, int to, int newFrom) {
    Node<E> first = nodeAt(from);
    Node<E> last = nodeAfter(first, from, to - 1);
    Node<E> insertBefore = nodeBefore(first, from, newFrom);
    if (last == tail) {
      makeTail(first.prev);
    } else {
      join(first.prev, last.next);
    }
    if (insertBefore == head) {
      join(last, insertBefore);
      makeHead(first);
    } else {
      join(insertBefore.prev, first);
      join(last, insertBefore);
    }
  }

  /**
   * Returns an array containing all elements in this list in proper sequence (from
   * first to last element).
   *
   * @return an array containing all elements in this list in proper sequence
   * @see Arrays#asList(Object[])
   */
  @Override
  public Object[] toArray() {
    if (sz == 0) {
      return EMPTY_OBJECT_ARRAY;
    }
    Object[] result = new Object[sz];
    int i = 0;
    for (E val : this) {
      result[i++] = val;
    }
    return result;
  }

  /**
   * Returns an array containing all elements in this list in proper sequence (from
   * first to last element); the runtime type of the returned array is that of the
   * specified array.
   *
   * @param a the array into which the elements of this list are to be stored, if
   *     it is big enough; otherwise, a new array of the same runtime type is
   *     allocated for this purpose.
   * @return an array containing the elements of this list
   * @throws ArrayStoreException if the runtime type of the specified array is
   *     not a supertype of the runtime type of every element in this list
   * @throws NullPointerException if the specified array is null
   */
  @Override
  @SuppressWarnings({"unchecked"})
  public <T> T[] toArray(T[] a) {
    Check.notNull(a);
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

  final Object[] toArray0(int fromIndex, int toIndex) {
    int len = Check.fromTo(this, fromIndex, toIndex);
    if (len == 0) {
      return EMPTY_OBJECT_ARRAY;
    }
    Object[] result = new Object[len];
    var node = nodeAt(fromIndex);
    for (int i = 0; i < len; ++i) {
      result[i] = node.val;
      node = node.next;
    }
    return result;
  }

  final void toArray0(int fromIndex, int toIndex, Object[] target, int offset) {
    int len = Check.fromTo(this, fromIndex, toIndex);
    Check.notNull(target, "target").has(length(), gte(), len + offset);
    Check.that(offset, Tag.OFFSET).is(gte(), 0);
    if (len != 0) {
      var node = nodeAt(fromIndex);
      for (int i = 0; i < len; ++i) {
        target[offset++] = node.val;
        node = node.next;
      }
    }
  }

  ////////////////////////////////////////////////////////////////
  // IMPORTANT: If you want to reuse nodes or chains, the order of
  // the operations matter. Always first unlink the node or chain,
  // and then insert it.
  ////////////////////////////////////////////////////////////////

  @SuppressWarnings({"unchecked", "rawtypes"})
  void insert(int index, Node node) {
    if (sz == 0) {
      makeHead(node);
      makeTail(node);
    } else if (index == 0) {
      join(node, head);
      makeHead(node);
    } else if (index == sz) {
      join(tail, node);
      makeTail(node);
    } else {
      var x = nodeAt(index);
      join(x.prev, node);
      join(node, x);
    }
    ++sz;
  }

  @SuppressWarnings("unchecked")
  void insert(int index, Chain chain) {
    if (sz == 0) {
      makeHead(chain.head);
      makeTail(chain.tail);
    } else if (index == 0) {
      join(chain.tail, head);
      makeHead(chain.head);
    } else if (index == sz) {
      join(tail, chain.head);
      makeTail(chain.tail);
    } else {
      var node = nodeAt(index);
      join(node.prev, chain.head);
      if (chain.length == 1) {
        join(chain.head, node);
      } else {
        join(chain.tail, node);
      }
    }
    sz += chain.length;
  }

  void unlink(Node<E> node) {
    if (sz == 1) {
      head = tail = null;
    } else if (node == head) {
      makeHead(node.next);
    } else if (node == tail) {
      makeTail(node.prev);
    } else {
      join(node.prev, node.next);
    }
    --sz;
  }

  Chain unlink(int from, int to) {
    var first = nodeAt(from);
    var last = nodeAfter(first, from, to - 1);
    int len = to - from;
    return unlink(new Chain(first, last, len));
  }

  @SuppressWarnings("unchecked")
  Chain unlink(Chain chain) {
    if (chain.length == sz) {
      head = tail = null;
    } else if (chain.head == head) {
      makeHead(chain.tail.next);
    } else if (chain.tail == tail) {
      makeTail(chain.head.prev);
    } else {
      join(chain.head.prev, chain.tail.next);
    }
    sz -= chain.length;
    return chain;
  }

  /**
   * Validates the index and then returns {@link #nodeAt(int)}.
   */
  Node<E> node(int index) {
    return Check.that(index)
        .is(indexInclusiveOf(), this, indexOutOfBounds(index))
        .mapToObj(this::nodeAt);
  }

  /**
   * Returns the node at the specified position.
   */
  // @VisibleForTesting
  Node<E> nodeAt(int index) {
    if (index < (sz >> 1)) {
      Node<E> n = head;
      for (int i = 0; i < index; ++i) {
        n = n.next;
      }
      return n;
    } else {
      Node<E> n = tail;
      for (int i = sz - 1; i > index; --i) {
        n = n.prev;
      }
      return n;
    }
  }

  /**
   * Returns a node following another node. Used to minimize the amount of pointers
   * we need to chase, given that we already have a node in our hands. The startIndex
   * argument is the index of the node we already have. The index argument is the
   * index of the node we are interested in. Note that if index is a to-index
   * (exclusive), this method may return null (namely when index equals sz).
   */
  // @VisibleForTesting
  Node<E> nodeAfter(Node<E> startNode, int startIndex, int index) {
    Node<E> n;
    if (index < ((sz + startIndex) >> 1)) {
      for (n = startNode; startIndex++ < index; n = n.next)
        ;
    } else {
      for (n = tail; ++index < sz; n = n.prev)
        ;
    }
    return n;
  }

  // @VisibleForTesting
  Node<E> nodeBefore(Node<E> startNode, int startIndex, int index) {
    Node<E> n;
    if (index < (startIndex >> 1)) {
      for (n = head; index-- > 0; n = n.next)
        ;
    } else {
      for (n = startNode; index++ < startIndex; n = n.prev)
        ;
    }
    return n;
  }

  void makeHead(Node<E> node) {
    node.prev = null;
    head = node;
  }

  void makeTail(Node<E> node) {
    node.next = null;
    tail = node;
  }

  static <T> void join(Node<T> prev, Node<T> next) {
    prev.next = next;
    next.prev = prev;
  }

  Node<E> justBeforeHead() {
    Node<E> x = new Node<>(null);
    x.next = head;
    return x;
  }

  Node<E> justAfterTail() {
    Node<E> x = new Node<>(null);
    x.prev = tail;
    return x;
  }

  void checkInclusive(int index) {
    Check.that(index).is(indexInclusiveOf(), this, indexOutOfBounds(index));
  }

}
