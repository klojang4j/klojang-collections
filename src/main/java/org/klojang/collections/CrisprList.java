package org.klojang.collections;

import org.klojang.check.Check;
import org.klojang.check.Tag;
import org.klojang.util.CollectionMethods;
import org.klojang.util.InvokeMethods;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.*;
import static org.klojang.check.CommonChecks.*;
import static org.klojang.check.CommonExceptions.indexOutOfBounds;
import static org.klojang.check.CommonExceptions.noSuchElement;
import static org.klojang.check.CommonProperties.length;
import static org.klojang.util.ArrayMethods.EMPTY_OBJECT_ARRAY;
import static org.klojang.util.MathMethods.divUp;

/**
 * A doubly-linked list, much like the JDK's {@link LinkedList}, but exclusively
 * focused on list manipulation while disregarding the queue-like aspects of linked
 * lists. As with any doubly-linked list, index-based retrieval is relatively costly
 * compared to {@link ArrayList}. It is very efficient, however, at inserting,
 * deleting and moving around chunks of list elements (i.e. structural changes). The
 * larger the chunks, the bigger the gain - again compared to {@code ArrayList}.
 *
 * <h2>Iteration</h2>
 * <p>
 * You should always use an {@code Iterator} to iterate over the elements in the list
 * (which you implicitly would when executing a {@code forEach} loop). Using an
 * index/get loop is possible, but performs poorly. The {@link Iterator} and
 * {@link ListIterator} implementations prescribed by the {@code List} interface are
 * no-frills iterators that throw an {@code UnsupportedOperationException} from all
 * methods designated as optional by the specification. In addition, the
 * {@code CrisprList class} also features a {@link #reverseIterator()} and a
 * {@link #wiredIterator()} method. The latter returns an instance of the
 * {@link WiredIterator} interface. Unlike a {@link ListIterator}, this is a
 * one-way-only iterator, but it still provides the same functionality, and it
 * <i>does</i> implement the methods that are optional in the {@code ListIterator}
 * interface.
 *
 * <h2>Fluent API</h2>
 * <p>
 * Many of the methods that go beyond those prescribed by the {@code List} interface
 * form part of a fluent API. This causes some overlap in functionality between
 * {@code List} methods and methods taking part in the fluent API. They are
 * implemented exactly alike, so there is intrinsic reason to prefer one over the
 * other.
 *
 * <h2>Thread safety</h2>
 * <p>
 * List edits are always destructive, and nearly always don't just change the values
 * in the list, but the underlying data structure itself. The {@code CrisprList}
 * class is not thread-safe. Multiple threads mutating the same list can leave it in
 * a seriously corrupted state, with "dangling wires" between the list elements (i.e.
 * null pointers). While the iterators make a light-touch effort to detect and trap
 * concurrent modifications on the list, {@code CrisprList} itself doesn't.
 * Therefore, synchronize carefully when multiple threads access the same list.
 *
 * @param <E> the type of the elements in the list
 * @author Ayco Holleman
 */
public final class CrisprList<E> implements List<E> {

  // Ubiquitous parameter names within this class
  private static final String WIRED_LIST = "CrisprList";

  private static Supplier<IllegalArgumentException> autoEmbedNotAllowed() {
    return () -> new IllegalArgumentException("list cannot be embedded within itself");
  }

  private static Supplier<IllegalStateException> callNextFirst() {
    return () -> new IllegalStateException("Iterator.next() must be called first");
  }

  private static Supplier<IllegalStateException> emptyList() {
    return () -> new IllegalStateException("illegal operation on empty list");
  }

  private static Supplier<ConcurrentModificationException> concurrentModification() {
    return ConcurrentModificationException::new;
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

  // @VisibleForTesting
  private static final class Node<V> {

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
  private record Chain(Node head, Node tail, int length) {

    // must (and will) only be called if values.size() > 0
    static <V> Chain of(Collection<V> values) {
      if (values instanceof CrisprList wl) {
        return copyOf(wl.head, wl.size());
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

  }

  //
  //
  //
  // ======================================================= //
  // ================ [ Iterator classes ]  ================ //
  // ======================================================= //
  //
  //
  //

  final class ForwardWiredIterator implements WiredIterator<E> {

    private Node<E> beforeHead;
    private Node<E> curr;

    private ForwardWiredIterator() {
      curr = beforeHead = justBeforeHead();
    }

    private ForwardWiredIterator(Node<E> curr) {
      this.curr = curr;
    }

    @Override
    public boolean hasNext() {
      return sz != 0 && curr != tail;
    }

    @Override
    public E value() {
      Check.that(curr).isNot(sameAs(), beforeHead, callNextFirst());
      Check.that(sz).isNot(zero(), emptyList());
      return curr.val;
    }

    @Override
    public E peek() {
      Check.that(curr).isNot(sameAs(), tail, noSuchElement());
      Check.that(sz).isNot(zero(), noSuchElement());
      return Check.that(curr.next)
          .is(notNull(), concurrentModification())
          .ok(Node::value);
    }

    @Override
    public E next() {
      Check.that(curr).isNot(sameAs(), tail, noSuchElement());
      Check.that(sz).isNot(zero(), noSuchElement());
      return Check.that(curr = curr.next)
          .is(notNull(), concurrentModification())
          .ok(Node::value);
    }

    @Override
    public void set(E newVal) {
      Check.that(curr).isNot(sameAs(), beforeHead, callNextFirst());
      Check.that(sz).isNot(zero(), emptyList());
      curr.val = newVal;
    }

    @Override
    public void insertBefore(E value) {
      Check.that(curr).isNot(sameAs(), beforeHead, callNextFirst());
      Check.that(sz).isNot(zero(), emptyList());
      Node<E> node = new Node<>(value);
      if (curr == head) {
        join(head = node, curr);
      } else {
        Check.that(curr.prev).is(notNull(), concurrentModification());
        join(curr.prev, node);
        join(node, curr);
      }
      ++sz;
    }

    @Override
    public void insertAfter(E value) {
      Check.that(curr).isNot(sameAs(), beforeHead, callNextFirst());
      Check.that(sz).isNot(zero(), emptyList());
      Node<E> node = new Node<>(value);
      if (curr == tail) {
        join(curr, tail = node);
      } else {
        Check.that(curr.next).is(notNull(), concurrentModification());
        join(node, curr.next);
        join(curr, node);
      }
      ++sz;
    }

    @Override
    public void remove() {
      Check.that(curr).isNot(sameAs(), beforeHead, callNextFirst());
      Check.that(sz).isNot(zero(), emptyList());
      if (curr == head) {
        unlink(curr);
        curr = beforeHead = justBeforeHead();
      } else {
        Check.that(curr = curr.prev)
            .is(notNull(), concurrentModification())
            .then(prev -> unlink(prev.next));
      }
    }

    @Override
    public int index() {
      Check.that(curr).isNot(sameAs(), beforeHead, callNextFirst());
      Check.that(sz).isNot(zero(), emptyList());
      int idx = 0;
      for (var node = head; ; ++idx) {
        if (node == curr) {
          return idx;
        }
        node = Check.that(node)
            .is(notNull(), concurrentModification())
            .isNot(sameAs(), tail, concurrentModification())
            .ok(x -> x.next);
      }
    }

    @Override
    public WiredIterator<E> turn() {
      Check.that(curr).isNot(sameAs(), beforeHead, callNextFirst());
      Check.that(sz).isNot(zero(), emptyList());
      return new ReverseWiredIterator(curr);
    }

  }

  final class ReverseWiredIterator implements WiredIterator<E> {

    private Node<E> afterTail;
    private Node<E> curr;

    private ReverseWiredIterator() {
      curr = afterTail = justAfterTail();
    }

    private ReverseWiredIterator(Node<E> curr) {
      this.curr = curr;
    }

    @Override
    public boolean hasNext() {
      return sz != 0 && curr != head;
    }

    @Override
    public E value() {
      Check.that(curr).isNot(sameAs(), afterTail, callNextFirst());
      Check.that(sz).isNot(zero(), emptyList());
      return curr.val;
    }

    @Override
    public E peek() {
      Check.that(curr).isNot(sameAs(), head, noSuchElement());
      Check.that(sz).isNot(zero(), noSuchElement());
      return Check.that(curr.prev)
          .is(notNull(), concurrentModification())
          .ok(Node::value);
    }

    @Override
    public E next() {
      Check.that(curr).isNot(sameAs(), head, noSuchElement());
      Check.that(sz).isNot(zero(), noSuchElement());
      return Check.that(curr = curr.prev)
          .is(notNull(), concurrentModification())
          .ok(Node::value);
    }

    @Override
    public void set(E newVal) {
      Check.that(curr).isNot(sameAs(), afterTail, callNextFirst());
      Check.that(sz).isNot(zero(), emptyList());
      curr.val = newVal;
    }

    @Override
    public void insertBefore(E value) {
      Check.that(curr).isNot(sameAs(), afterTail, callNextFirst());
      Check.that(sz).isNot(zero(), emptyList());
      Node<E> node = new Node<>(value);
      if (curr == tail) {
        join(curr, tail = node);
      } else {
        Check.that(curr.next).is(notNull(), concurrentModification());
        join(node, curr.next);
        join(curr, node);
      }
      ++sz;
    }

    @Override
    public void insertAfter(E value) {
      Check.that(curr).isNot(sameAs(), afterTail, callNextFirst());
      Check.that(sz).isNot(zero(), emptyList());
      Node<E> node = new Node<>(value);
      if (curr == head) {
        join(head = node, curr);
      } else {
        Check.that(curr.prev).is(notNull(), concurrentModification());
        join(curr.prev, node);
        join(node, curr);
      }
      ++sz;
    }

    @Override
    public void remove() {
      Check.that(curr).isNot(sameAs(), afterTail, callNextFirst());
      Check.that(sz).isNot(zero(), emptyList());
      if (curr == tail) {
        unlink(curr);
        curr = afterTail = justAfterTail();
      } else {
        Check.that(curr = curr.next)
            .is(notNull(), concurrentModification())
            .then(next -> unlink(next.prev));
      }
    }

    @Override
    public int index() {
      Check.that(curr).isNot(sameAs(), afterTail, callNextFirst());
      Check.that(sz).isNot(zero(), emptyList());
      int idx = sz - 1;
      for (var node = tail; ; --idx) {
        if (node == curr) {
          return idx;
        }
        node = Check.that(node)
            .is(notNull(), concurrentModification())
            .isNot(sameAs(), head, concurrentModification())
            .ok(x -> x.prev);
      }
    }

    @Override
    public WiredIterator<E> turn() {
      Check.that(curr).isNot(sameAs(), afterTail, callNextFirst());
      Check.that(sz).isNot(zero(), emptyList());
      return new ForwardWiredIterator(curr);
    }

  }

  /*
   * NB The asymmetry between next/hasNext and previous/hasPrevious is no code
   * sloth. It is due to the specification of the ListIterator interface and the
   * List.listIterator(index) method.
   */
  private class ListItr implements ListIterator<E> {

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
  // ==================== [ CrisprList ] ==================== //
  // ======================================================= //
  //
  //
  //

  /**
   * Returns a new, empty {@code CrisprList}. Note that, although the {@code of(..)}
   * methods look like the {@code List.of(...)} methods, they return ordinary,
   * mutable, {@code null}-accepting {@code CrisprList} instances.
   *
   * @param <E> the type of the elements in the list
   * @return a new, empty {@code CrisprList}
   */
  public static <E> CrisprList<E> of() {
    return new CrisprList<>();
  }

  /**
   * Returns a new {@code CrisprList} containing the specified element.
   *
   * @param e the element
   * @param <E> the type of the elements in the list
   * @return a new {@code CrisprList} containing the specified elements
   */
  public static <E> CrisprList<E> of(E e) {
    return new CrisprList<E>().append(e);
  }

  /**
   * Returns a new {@code CrisprList} containing the specified elements.
   *
   * @param e0 the first element in the list
   * @param e1 the second element in the list
   * @param <E> the type of the elements in the list
   * @return a new {@code CrisprList} containing the specified elements
   */
  public static <E> CrisprList<E> of(E e0, E e1) {
    return new CrisprList<E>().append(e0).append(e1);
  }

  /**
   * Returns a new {@code CrisprList} containing the specified elements.
   *
   * @param e0 the first element in the list
   * @param e1 the second element in the list
   * @param e2 the third element in the list
   * @param moreElems more elements to include in the list
   * @param <E> the type of the elements in the list
   * @return a new {@code CrisprList} containing the specified elements
   */
  @SafeVarargs
  public static <E> CrisprList<E> of(E e0, E e1, E e2, E... moreElems) {
    Check.notNull(moreElems, Tag.ARRAY);
    var wl = new CrisprList<E>();
    Node<E> head = new Node<>(e0);
    Node<E> tail = head;
    tail = new Node<>(tail, e1);
    tail = new Node<>(tail, e2);
    for (E e : moreElems) {
      tail = new Node<>(tail, e);
    }
    wl.head = head;
    wl.tail = tail;
    wl.sz = moreElems.length + 3;
    return wl;
  }

  /**
   * Returns a new {@code CrisprList} containing the specified elements.
   *
   * @param elements the elements to add to the list
   * @param <E> the type of the elements in the list
   * @return a new {@code CrisprList} containing the specified elements
   */
  @SafeVarargs
  public static <E> CrisprList<E> ofElements(E... elements) {
    Check.notNull(elements, Tag.ARRAY);
    var wl = new CrisprList<E>();
    if (elements.length != 0) {
      Node<E> head = new Node<>(elements[0]);
      Node<E> tail = head;
      for (int i = 1; i < elements.length; ++i) {
        tail = new Node<>(tail, elements[i]);
      }
      wl.head = head;
      wl.tail = tail;
      wl.sz = elements.length;
    }
    return wl;
  }

  /**
   * Concatenates the provided {@code CrisprList} instances. This is a destructive
   * operation for the {@code CrisprList} instances in the provided {@code List}.
   * They will be empty when the method returns. See {@link #attach(CrisprList)}.
   *
   * @param lists the {@code CrisprList} instances to concatenate
   * @param <E> the type of the elements in the list
   * @return a new {@code CrisprList} containing the elements in the individual
   *     {@code CrisprList} instances
   */
  public static <E> CrisprList<E> join(List<CrisprList<E>> lists) {
    CrisprList<E> wl = new CrisprList<>();
    Check.notNull(lists).ok().forEach(wl::attach);
    return wl;
  }

  private Node<E> head;
  private Node<E> tail;
  private int sz;

  /**
   * Creates a new, empty {@code CrisprList}.
   */
  public CrisprList() {}

  /**
   * Creates a new {@code CrisprList} containing the elements in the specified
   * {@code Collection}.
   *
   * @param c the collection whose elements to copy to this {@code CrisprList}
   */
  public CrisprList(Collection<? extends E> c) {
    addAll(0, c);
  }

  @SuppressWarnings({"unchecked"})
  private CrisprList(Chain chain) {
    makeHead(chain.head);
    makeTail(chain.tail);
    sz = chain.length;
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
   * Overwrites the elements at, and following the specified index with the provided
   * values. For linked lists this is more efficient than setting each of the
   * elements individually, especially if the elements are somewhere in the middle of
   * the lists. The number of values must not exceed {@code list.size() - index}.
   *
   * @param index the index of the first element the set
   * @param e0 the first value to write
   * @param e1 the second value to write
   * @param moreElems more values to write
   * @return this {@code CrisprList}
   */
  @SuppressWarnings("unchecked")
  public CrisprList<E> set(int index, E e0, E e1, E... moreElems) {
    Check.that(index).is(indexInclusiveOf(), this, indexOutOfBounds(index));
    Check.notNull(moreElems, Tag.VARARGS).has(length(), lte(), sz - index - 2);
    Node<E> node = nodeAt(index);
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
    return this;
  }

  /**
   * Sets the element at the specified index to the specified value <i>if</i> the
   * original value passes the specified test. This method mitigates the relatively
   * large cost of index-based retrieval with linked lists, which would double if you
   * had to execute a get-compare-set sequence.
   *
   * @param index the index of the element to set
   * @param condition The test that the original value has to pass in order to be
   *     replaced with the new value. The original value is passed to the predicate's
   *     {@code test} method.
   * @param value The value to set
   * @return The original value
   */
  public E setIf(int index, Predicate<? super E> condition, E value) {
    Check.notNull(condition, Tag.TEST);
    var node = node(index);
    E old = node.val;
    if (condition.test(old)) {
      node.val = value;
    }
    return old;
  }

  /**
   * Appends the specified element to the end of this list.
   *
   * @param e element to be appended to this list
   * @return {@code true} (as specified by {@link Collection#add})
   * @see #append(Object)
   */
  @Override
  public boolean add(E e) {
    append(e);
    return true;
  }

  /**
   * Inserts the specified element at the specified position in this list. Shifts the
   * element currently at that position (if any) and any subsequent elements to the
   * right (adds one to their indices).
   *
   * @param index index at which the specified element is to be inserted
   * @param element element to be inserted
   * @see #insert(int, Object)
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
   * @see #appendAll(Collection)
   * @see #attach(CrisprList)
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
   * @see #insertAll(int, Collection)
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
   * Removes the element at the specified position in this list.
   *
   * @param index the index of the element to be removed
   * @return the element previously at the specified position
   * @throws IndexOutOfBoundsException if the index is out of range
   *     ({@code index < 0 || index >= size()})
   */
  @Override
  public E remove(int index) {
    var x = node(index);
    E val = x.val;
    unlink(x);
    return val;
  }

  /**
   * Removes the first occurrence of the specified element from this list, if it is
   * present.
   *
   * @param o element to be removed from this list, if present
   * @return {@code true} if this list contained the specified element
   */
  @Override
  public boolean remove(Object o) {
    if (o == null) {
      for (var x = head; x != null; ) {
        if (x.val == null) {
          unlink(x);
          return true;
        }
        x = x.next;
      }
    } else {
      for (var x = head; x != null; ) {
        if (o.equals(x.val)) {
          unlink(x);
          return true;
        }
        x = x.next;
      }
    }
    return false;
  }

  /**
   * Removes all elements of this collection that satisfy the given predicate.
   *
   * @param filter a predicate which returns {@code true} for elements to be
   *     removed
   * @return {@code true} if any elements were removed
   */
  @Override
  public boolean removeIf(Predicate<? super E> filter) {
    Check.notNull(filter, Tag.TEST);
    int size = sz;
    for (var x = head; x != null; ) {
      if (filter.test(x.val)) {
        var next = x.next;
        unlink(x);
        x = next;
      } else {
        x = x.next;
      }
    }
    return size != this.sz;
  }

  /**
   * Removes all elements from this list that are also present in the specified
   * collection.
   *
   * @param c collection containing elements to be removed from this list
   * @return {@code true} if this list changed as a result of the call
   * @see #remove(Object)
   * @see #contains(Object)
   */
  @Override
  public boolean removeAll(Collection<?> c) {
    Check.notNull(c, Tag.COLLECTION);
    int size = this.sz;
    removeIf(c::contains);
    return size != this.sz;
  }

  /**
   * Removes all elements from this list that are not present in the specified
   * collection.
   *
   * @param c collection containing elements to be retained in this list
   * @return {@code true} if this list changed as a result of the call
   * @see #remove(Object)
   * @see #contains(Object)
   */
  @Override
  public boolean retainAll(Collection<?> c) {
    Check.notNull(c, Tag.COLLECTION);
    int sz = this.sz;
    removeIf(e -> !c.contains(e));
    return sz != this.sz;
  }

  /**
   * Returns the first element of the list. A {@link NoSuchElementException} is
   * thrown if the list is empty.
   *
   * @return the first element of the list
   */
  public E first() {
    Check.that(sz).isNot(zero(), noSuchElement());
    return head.val;
  }

  /**
   * Returns the last element of the list. A {@link NoSuchElementException} is thrown
   * if the list is empty.
   *
   * @return the last element of the list
   */
  public E last() {
    Check.that(sz).isNot(zero(), noSuchElement());
    return tail.val;
  }

  /**
   * Inserts the specified value at the start of the list, right-shifting the
   * original elements.
   *
   * @param value The value to insert
   * @return this {@code CrisprList}
   */
  public CrisprList<E> prepend(E value) {
    Node<E> n = new Node<>(value);
    if (sz == 0) {
      head = tail = n;
    } else {
      join(n, head);
      head = n;
    }
    ++sz;
    return this;
  }

  /**
   * Inserts the specified collection at the start of the list, right-shifting the
   * original elements.
   *
   * @param values The values to prepend to the list
   * @return this {@code CrisprList}
   */
  public CrisprList<E> prependAll(Collection<? extends E> values) {
    Check.notNull(values, Tag.COLLECTION);
    if (!values.isEmpty()) {
      insert(0, Chain.of(values));
    }
    return this;
  }

  /**
   * Appends the specified value to the end of the list. Equivalent to
   * {@link #add(Object) add(value)}.
   *
   * @param value The value to append to the list
   * @return this {@code CrisprList}
   */
  public CrisprList<E> append(E value) {
    Node<E> n = new Node<>(value);
    if (sz == 0) {
      head = tail = n;
    } else {
      join(tail, n);
      tail = n;
    }
    ++sz;
    return this;
  }

  /**
   * Appends the specified collection to this {@code CrisprList}.
   *
   * @param values The values to append to the list
   * @return this {@code CrisprList}
   * @see #addAll(Collection)
   * @see #attach(CrisprList)
   */
  public CrisprList<E> appendAll(Collection<? extends E> values) {
    Check.notNull(values, Tag.COLLECTION);
    if (!values.isEmpty()) {
      insert(sz, Chain.of(values));
    }
    return this;
  }

  /**
   * Inserts a value into the list. All elements <i>at and following</i> the
   * specified index will be right-shifted. The index value must be {@code >= 0} and
   * {@code <= list.size()}. Specifying 0 (zero) is equivalent to
   * {@link #prepend(Object) prepend(value)}. Specifying {@code list.size()} is
   * equivalent to {@link #append(Object) append(value} and
   * {@link #add(Object) add(value)}.
   *
   * @param index the index at which to insert the value
   * @param value the value
   * @return this {@code CrisprList}
   */
  public CrisprList<E> insert(int index, E value) {
    checkInclusive(index);
    insert(index, new Node<>(value));
    return this;
  }

  /**
   * Inserts the specified collection at the specified index, right-shifting the
   * elements at and following the index.
   *
   * @param index the index at which to insert the collection
   * @param values The collection to insert into the list
   * @return this {@code CrisprList}
   * @see #addAll(int, Collection)
   */
  public CrisprList<E> insertAll(int index, Collection<? extends E> values) {
    checkInclusive(index);
    Check.notNull(values, Tag.COLLECTION);
    if (!values.isEmpty()) {
      insert(index, Chain.of(values));
    }
    return this;
  }

  /**
   * Removes the first element from the list, left-shifting the remaining elements. A
   * {@link NoSuchElementException} is thrown if the list is empty.
   *
   * @return this {@code CrisprList}
   */
  public CrisprList<E> deleteFirst() {
    Check.that(sz).isNot(zero(), noSuchElement());
    unlink(head);
    return this;
  }

  /**
   * Removes the last element from the list. A {@link NoSuchElementException} is
   * thrown if the list is empty.
   *
   * @return this {@code CrisprList}
   */
  public CrisprList<E> deleteLast() {
    Check.that(sz).isNot(zero(), noSuchElement());
    unlink(tail);
    return this;
  }

  /**
   * Replaces each element of this list with the result of applying the operator to
   * that element.  Errors or runtime exceptions thrown by the operator are relayed
   * to the caller.
   *
   * @param operator the operator to apply to each element
   * @throws UnsupportedOperationException if this list is unmodifiable.
   *     Implementations may throw this exception if an element cannot be replaced or
   *     if, in general, modification is not supported
   */
  @Override
  public void replaceAll(UnaryOperator<E> operator) {
    if (sz != 0) {
      for (var x = head; ; x = x.next) {
        x.val = operator.apply(x.val);
        if (x == tail) {
          break;
        }
      }
    }
  }

  /**
   * Replaces the segment between {@code fromIndex} and {@code toIndex} with the
   * elements in the specified collection.
   *
   * @param fromIndex the start index (inclusive) of the segment to replace
   * @param toIndex the end index (exclusive) of
   * @param values The values to replace the segment with
   * @return this {@code CrisprList}
   * @see #rewire(int, int, CrisprList)
   */
  public CrisprList<E> replace(int fromIndex,
      int toIndex,
      Collection<? extends E> values) {
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
    return this;
  }

  /**
   * Replaces the segment between {@code fromIndex} and {@code toIndex} with the
   * elements in the specified list. This method is functionally equivalent to
   * {@link #replace(int, int, Collection) replace}, but more efficient. However, it
   * will leave the specified list empty. If you don't want this to happen, use
   * {@code replace}.
   *
   * @param fromIndex the start index (inclusive) of the segment to replace
   * @param toIndex the end index (exclusive) of
   * @param other the values to replace the segment with
   * @return this {@code CrisprList}
   */
  public CrisprList<E> rewire(int fromIndex,
      int toIndex,
      CrisprList<? extends E> other) {
    int len = Check.fromTo(this, fromIndex, toIndex);
    Check.notNull(other, WIRED_LIST).isNot(sameAs(), this, autoEmbedNotAllowed());
    if (len != 0) {
      unlink(fromIndex, toIndex);
    }
    if (!other.isEmpty()) {
      insert(fromIndex, other.unlink(0, other.sz));
    }
    return this;
  }

  /**
   * Returns a copy of this {@code CrisprList}. Changes made to the copy will not
   * propagate to this instance, and vice versa.
   *
   * @return a deep copy of this {@code CrisprList}
   */
  public CrisprList<E> copy() {
    return sz > 0 ? new CrisprList<>(Chain.copyOf(head, sz)) : CrisprList.of();
  }

  /**
   * Returns a copy of the specified segment. Changes made to the copy will not
   * propagate to this instance, and vice versa.
   *
   * @param fromIndex the start index (inclusive) of the segment
   * @param toIndex the end index (exclusive) of the segment
   * @return a deep copy of the specified segment
   */
  public CrisprList<E> copy(int fromIndex, int toIndex) {
    int len = Check.fromTo(this, fromIndex, toIndex);
    return len > 0
        ? new CrisprList<>(Chain.copyOf(nodeAt(fromIndex), len))
        : CrisprList.of();
  }

  /**
   * Shrinks the list to between the specified boundaries. If {@code toIndex} is
   * equal to {@code fromIndex}, the list will, in effect, be
   * {@link #clear() cleared}.
   *
   * @param fromIndex the index (inclusive) of the new start of the list
   * @param toIndex the index (exclusive) of the new end of the list
   * @return this {@code CrisprList}
   */
  public CrisprList<E> shrink(int fromIndex, int toIndex) {
    int len = Check.fromTo(this, fromIndex, toIndex);
    if (len == 0) {
      clear();
    } else if (len != sz) {
      Node<E> x = nodeAt(fromIndex);
      Node<E> y = nodeAfter(x, fromIndex, toIndex - 1);
      makeHead(x);
      makeTail(y);
    }
    return this;
  }

  /**
   * Removes and returns a segment from the list.
   *
   * @param fromIndex the start index (inclusive) of the segment to delete
   * @param toIndex the end index (exclusive) of the segment to delete
   * @return the deleted segment
   */
  public CrisprList<E> cut(int fromIndex, int toIndex) {
    if (Check.fromTo(this, fromIndex, toIndex) > 0) {
      return new CrisprList<>(unlink(fromIndex, toIndex));
    }
    return CrisprList.of();
  }

  /**
   * Inserts this list into the specified list at the specified position. Equivalent
   * to {@link #embed(int, CrisprList) into.embed(index, this)}. This list will be
   * empty afterwards. Note that this method does not return this list but the
   * paste-into list.
   *
   * @param into the list into which to insert this list
   * @param index the index at which to insert this list
   * @return the target list
   */
  public CrisprList<? super E> paste(CrisprList<? super E> into, int index) {
    return into.embed(index, this);
  }

  /**
   * Embeds the specified list in this list. This method is functionally equivalent
   * to {@link #insertAll(int, Collection) insertAll} and
   * {@link #addAll(int, Collection) addAll}, but more efficient. However, it is a
   * destructive operation for the provided list. It will be empty afterwards. If you
   * don't want this to happen, use {@code insertAll} or {@code addAll}.
   *
   * @param index the index at which to embed the list
   * @param other the list to embed
   * @return this {@code CrisprList}
   */
  public CrisprList<E> embed(int index, CrisprList<? extends E> other) {
    checkInclusive(index);
    Check.notNull(other, WIRED_LIST).isNot(sameAs(), this, autoEmbedNotAllowed());
    if (!other.isEmpty()) {
      insert(index, new Chain(other.head, other.tail, other.sz));
      other.clear();
    }
    return this;
  }

  /**
   * Exchanges list segments between this list and the specified list.
   *
   * @param myFromIndex the start index (inclusive) of the segment within this
   *     list
   * @param myToIndex the end index (exclusive) of the segment within this list
   * @param other the list to exchange segments with
   * @param itsFromIndex the start index (inclusive) of the segment within the
   *     other list
   * @param itsToIndex the end index (exclusive) of the segment within the other
   *     list
   * @return this {@code CrisprList}
   */
  public CrisprList<E> exchange(int myFromIndex,
      int myToIndex,
      CrisprList<E> other,
      int itsFromIndex,
      int itsToIndex) {
    int len0 = Check.fromTo(this, myFromIndex, myToIndex);
    int len1 = Check.fromTo(other, itsFromIndex, itsToIndex);
    Check.that(other).isNot(sameAs(), this, autoEmbedNotAllowed());
    if (len0 == 0) {
      if (len1 != 0) {
        insert(myFromIndex, other.unlink(itsFromIndex, itsToIndex));
      }
      return this;
    } else if (len1 == 0) {
      other.insert(itsFromIndex, unlink(myFromIndex, myToIndex));
      return this;
    }

    // segment boundary nodes
    var seg0L = nodeAt(myFromIndex);
    var seg0R = nodeAfter(seg0L, myFromIndex, myToIndex - 1);
    var seg1L = other.nodeAt(itsFromIndex);
    var seg1R = other.nodeAfter(seg1L, itsFromIndex, itsToIndex - 1);

    // attach-to nodes
    var att0L = seg0L.prev;
    var att0R = seg0R.next;
    var att1L = seg1L.prev;
    var att1R = seg1R.next;

    if (att0L == null) {
      makeHead(seg1L);
    } else {
      join(att0L, seg1L);
    }
    if (att0R == null) {
      makeTail(seg1R);
    } else {
      join(seg1R, att0R);
    }

    if (att1L == null) {
      other.makeHead(seg0L);
    } else {
      join(att1L, seg0L);
    }
    if (att1R == null) {
      other.makeTail(seg0R);
    } else {
      join(seg0R, att1R);
    }

    sz = sz - len0 + len1;
    other.sz = other.sz - len1 + len0;

    return this;
  }

  /**
   * Removes a segment from the specified list and embeds it in this list.
   *
   * @param myIndex the index at which to insert segment
   * @param other the list to remove the segment from
   * @param itsFromIndex the start index of the segment (inclusive)
   * @param itsToIndex the end index of the segment (exclusive)
   * @return this {@code CrisprList}
   */
  public CrisprList<E> embed(int myIndex,
      CrisprList<? extends E> other,
      int itsFromIndex,
      int itsToIndex) {
    checkInclusive(myIndex);
    int len = Check.fromTo(other, itsFromIndex, itsToIndex);
    Check.that(other).isNot(sameAs(), this, autoEmbedNotAllowed());
    if (len > 0) {
      insert(myIndex, other.unlink(itsFromIndex, itsToIndex));
    }
    return this;
  }

  /**
   * Appends the specified list to this list. This method is functionally equivalent
   * {@link #appendAll(Collection) appendAll} and {@link #addAll(Collection) addAll},
   * but more efficient. However, it will leave the specified list empty. If you
   * don't want this to happen, use {@code appendAll} or {@code addAll}.
   *
   * @param other the list to embed
   * @return this {@code CrisprList}
   * @see #join(List)
   */
  public CrisprList<E> attach(CrisprList<? extends E> other) {
    Check.notNull(other).isNot(sameAs(), this, autoEmbedNotAllowed());
    if (other.sz != 0) {
      attach0(other);
    }
    return this;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void attach0(CrisprList other) {
    if (sz == 0) {
      head = other.head;
    } else {
      join(tail, other.head);
    }
    tail = other.tail;
    sz += other.sz;
    other.clear();
  }

  /**
   * Reorders the elements according to the specified criteria. The elements
   * satisfying the first criterion (if any) will come first in the list, the
   * elements satisfying the second criterion (if any) will come second, etc. The
   * elements that did not satisfy any criterion will come last in the list.
   *
   * @param criteria the criteria used to group the elements
   * @return this {@code CrisprList}
   */
  public CrisprList<E> defragment(List<Predicate<? super E>> criteria) {
    return defragment(true, criteria);
  }

  /**
   * Reorders the elements according to the specified criteria. The elements
   * satisfying the first criterion (if any) will come first in the list, the
   * elements satisfying the second criterion (if any) will come second, etc.
   *
   * @param keepRemainder whether to keep the elements that did not satisfy any
   *     criterion, and move them to the end of the list
   * @param criteria the criteria used to group the elements
   * @return this {@code CrisprList}
   */
  @SuppressWarnings({"rawtypes"})
  public CrisprList<E> defragment(boolean keepRemainder,
      List<Predicate<? super E>> criteria) {
    Check.that(criteria).is(deepNotEmpty());
    List<CrisprList<E>> groups = createGroups(criteria);
    Chain rest = new Chain(head, tail, sz);
    sz = 0;
    for (CrisprList wl : groups) {
      if (!wl.isEmpty()) {
        attach0(wl);
      }
    }
    if (keepRemainder && rest.length != 0) {
      insert(sz, rest);
    }
    return this;
  }

  /**
   * Groups the elements in those that do, and elements that do not satisfy the
   * specified criterion.
   *
   * @param criterion the test to submit the list elements to
   * @param <L0> the type of the lists within the returned list
   * @param <L1> the type of returned list
   * @return a list containing two lists representing the two groups
   * @see #group(List)
   */
  public <L0 extends List<E>, L1 extends List<L0>> L1 group(Predicate<? super E> criterion) {
    return group(singletonList(criterion));
  }

  /**
   * <p>
   * Groups the elements according to the provided criteria. The return value is a
   * list-of-lists where each inner {@code List} constitutes a group. <i>This</i>
   * {@code CrisprList} is left with all elements that did not satisfy any criterion,
   * and it will be the last element in the returned list-of-lists. In other words,
   * the size of the returned list-of-lists is the number of criteria plus one. You
   * can use the {@link #join(List) join} method to create a single "defragmented"
   * list again.
   * <p>
   * Elements will never be placed in more than one group. As soon as an element is
   * found to satisfy a criterion it is placed in the corresponding group and the
   * remaining criteria are skipped.
   * <p>
   * The runtime type of the returned list-of-lists
   * {@code CrisprList<CrisprList<E>>}. If you don't care about the exact type of the
   * returned {@code List}, you can simply write:
   *
   * <blockquote><pre>{@code
   * CrisprList<String> wl = ...;
   * List<List<String>> groups = wl.group(...);
   * }</pre></blockquote>
   * <p>
   * Otherwise use any combination of {@code List} and {@code CrisprList} that suits
   * your needs.
   *
   * @param criteria the criteria used to group the elements
   * @param <L0> the type of the lists within the returned list
   * @param <L1> the type of returned list
   * @return a list of element groups
   */
  @SuppressWarnings({"unchecked"})
  public <L0 extends List<E>, L1 extends List<L0>> L1 group(List<Predicate<? super E>> criteria) {
    Check.that(criteria).is(deepNotEmpty());
    List<CrisprList<E>> groups = createGroups(criteria);
    var result = new CrisprList<>(groups);
    result.add(this);
    return (L1) result;
  }

  @SuppressWarnings({"rawtypes"})
  private CrisprList<CrisprList<E>> createGroups(List<Predicate<? super E>> criteria) {
    CrisprList<CrisprList<E>> groups = new CrisprList<>();
    criteria.forEach(c -> groups.append(new CrisprList<>()));
    for (Node<E> node = head; node != null; ) {
      var next = node.next;
      for (int i = 0; i < criteria.size(); ++i) {
        if (criteria.get(i).test(node.val)) {
          unlink(node);
          CrisprList wl = groups.get(i);
          wl.insert(wl.size(), node);
          break;
        }
      }
      node = next;
    }
    return groups;
  }

  /**
   * Splits this {@code CrisprList} into multiple {@code CrisprList} instances of the
   * specified size. The partitions are chopped off from the {@code CrisprList} and
   * then placed in a separate {@code CrisprList}. The last element in the returned
   * list-of-lists is <i>this</i> {@code CrisprList}, and it will now contain at most
   * {@code size} elements.
   * <p>
   * The runtime type of the return value is {@code CrisprList<CrisprList<E>>}. If
   * you don't care about the exact type of the returned {@code List}, you can simply
   * write:
   *
   * <blockquote><pre>{@code
   * CrisprList<String> wl = ...;
   * List<List<String>> partitions = wl.partition(3);
   * }</pre></blockquote>
   * <p>
   * Otherwise use any combination of {@code List} and {@code CrisprList} that suits
   * your needs.
   *
   * @param size The desired size of the partitions
   * @param <L0> the type of the lists within the returned list
   * @param <L1> the type of returned list
   * @return a list of {@code CrisprList} instances of the specified size
   */
  @SuppressWarnings("unchecked")
  public <L0 extends List<E>, L1 extends List<L0>> L1 partition(int size) {
    Check.that(size).is(gt(), 0);
    CrisprList<CrisprList<E>> partitions = new CrisprList<>();
    while (sz > size) {
      Chain chain = new Chain(head, nodeAt(size - 1), size);
      partitions.append(new CrisprList<>(unlink(chain)));
    }
    partitions.append(this);
    return (L1) partitions;
  }

  /**
   * Splits this {@code CrisprList} into the specified number of equally-sized
   * {@code CrisprList} instances. The last element in the returned list-of-lists is
   * this {@code CrisprList}, and it will contain the remainder of the elements after
   * dividing the list size by {@code numPartitions}. The runtime type of the return
   * value is {@code CrisprList<CrisprList<E>>}. If you don't care about the exact
   * type of the returned {@code List}, you can simply write:
   *
   * <blockquote><pre>{@code
   * CrisprList<String> wl = ...;
   * List<List<String>> partitions = wl.split(3);
   * }</pre></blockquote>
   * <p>
   * Otherwise use any combination of {@code List} and {@code CrisprList} that suits
   * your needs.
   *
   * @param numPartitions The number of {@code CrisprList} instances to split
   *     this {@code CrisprList} into
   * @param <L0> the type of the lists within the returned list
   * @param <L1> the type of returned list
   * @return a list containing the specified number of {@code CrisprList} instances
   */
  public <L0 extends List<E>, L1 extends List<L0>> L1 split(int numPartitions) {
    Check.that(numPartitions).is(gt(), 0);
    return partition(divUp(sz, numPartitions));
  }

  /**
   * Removes and returns a segment from the start of the list. The segment includes
   * all elements up to (and not including) the first element that does not satisfy
   * the specified condition. In other words, all elements in the returned list
   * <i>will</i> satisfy the condition. If the condition is never satisfied, this
   * list remains unchanged and an empty list is returned. If <i>all</i> elements
   * satisfy the condition, the list remains unchanged and is itself returned.
   *
   * @param criterion the criterion that the elements in the returned segment
   *     will satisfy
   * @return a {@code CrisprList} containing all elements preceding the first element
   *     that does not satisfy the condition
   */
  public CrisprList<E> lchop(Predicate<? super E> criterion) {
    Check.notNull(criterion);
    if (sz == 0) {
      return this;
    }
    Node<E> first = head;
    Node<E> last = justBeforeHead();
    int len = 0;
    for (; criterion.test(last.next.val) && ++len != sz; last = last.next)
      ;
    if (len == sz) {
      return this;
    }
    return new CrisprList<>(unlink(new Chain(first, last, len)));
  }

  /**
   * Removes and returns a segment from the end of the list. The segment includes all
   * elements following the <i>last</i> element that does <i>not</i> satisfy the
   * specified condition. In other words, all elements in the returned list
   * <i>will</i> satisfy the condition. If the condition is never satisfied, the
   * list remains unchanged and an empty list is returned. If <i>all</i> elements
   * satisfy the condition, the list remains unchanged and is itself returned.
   *
   * @param criterion the criterion that the elements in the returned segment
   *     will satisfy
   * @return a {@code CrisprList} containing all elements following the last element
   *     that does not satisfy the condition
   */
  public CrisprList<E> rchop(Predicate<? super E> criterion) {
    Check.notNull(criterion);
    if (sz == 0) {
      return this;
    }
    Node<E> last = tail;
    Node<E> first = justAfterTail();
    int len = 0;
    for (; criterion.test(first.prev.val) && ++len != sz; first = first.prev)
      ;
    if (len == sz) {
      return this;
    }
    return new CrisprList<>(unlink(new Chain(first, last, len)));
  }

  /**
   * Reverses the order of the elements in this {@code CrisprList}.
   *
   * @return this {@code CrisprList}
   */
  public CrisprList<E> reverse() {
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
    return this;
  }

  /**
   * Moves a list segment forwards or backwards through the list.
   *
   * @param fromIndex the start index of the segment (inclusive)
   * @param toIndex the end index of the segment (exclusive)
   * @param newFromIndex the index to which to move the segment. To move the
   *     segment to the very start of the list, specify 0 (zero). To move the segment
   *     to the very end of the list specify the {@link #size() size} of the list
   * @return this {@code CrisprList}
   */
  public CrisprList<E> move(int fromIndex, int toIndex, int newFromIndex) {
    int len = Check.fromTo(this, fromIndex, toIndex);
    Check.that(newFromIndex, "target index").is(indexInclusiveOf(), this);
    if (len != 0) {
      if (newFromIndex > fromIndex) {
        moveToTail(fromIndex, toIndex, newFromIndex);
      } else if (newFromIndex < fromIndex) {
        moveToHead(fromIndex, toIndex, newFromIndex);
      }
    }
    return this;
  }

  private void moveToTail(int from, int to, int newFrom) {
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

  private void moveToHead(int from, int to, int newFrom) {
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

  /**
   * Removes all elements from this list. The list will be empty after this call
   * returns.
   */
  @Override
  public void clear() {
    head = tail = null;
    sz = 0;
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
        return curr != tail;
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

  /**
   * Returns an {@code Iterator} that traverses the list from the last element to the
   * first. See also {@link #iterator()}.
   *
   * @return an {@code Iterator} that traverses the list from the last element to the
   *     first
   */
  public Iterator<E> reverseIterator() {
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
   * Returns a {@link WiredIterator} that traverses the list from the first element
   * to the last.
   *
   * @return a {@code WiredIterator} that traverses the list from the first element
   *     to the last
   */
  public WiredIterator<E> wiredIterator() {
    return new ForwardWiredIterator();
  }

  /**
   * Returns a {@link WiredIterator} that traverses the list from the first element
   * to the last, or the other way round, depending on the value of the argument
   *
   * @param reverse Whether to iterate from the first to the last
   *     ({@code false}), or from the last to the first ({@code true})
   * @return a {@code WiredIterator} that traverses the list from the first element
   *     to the last, or the other way round
   */
  public WiredIterator<E> wiredIterator(boolean reverse) {
    return reverse ? new ReverseWiredIterator() : new ForwardWiredIterator();
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

  /**
   * Throws an {@code UnsupportedOperationException}. The specification for this
   * method requires that non-structural changes in the returned list are reflected
   * in the original list (and vice versa). However, except for the
   * {@link #set(int, Object)} method, all changes to a {@code CrisprList} <i>are</i>
   * structural changes. {@code CrisprList} does provide a method that returns a
   * sublist ({@link #copy(int, int) copySegment}). It just has no relation to the
   * original list any longer.
   */
  @Override
  public List<E> subList(int fromIndex, int toIndex) {
    throw new UnsupportedOperationException();
  }

  ////////////////////////////////////////////////////////////////
  // IMPORTANT: If you want to reuse nodes or chains, the order of
  // the operations matter. Always first unlink the node or chain,
  // and then insert it.
  ////////////////////////////////////////////////////////////////

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void insert(int index, Node node) {
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
  private void insert(int index, Chain chain) {
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

  // Make garbage collector happy
  private E destroy(Node<E> node) {
    E val = node.val;
    unlink(node);
    return val;
  }

  private void unlink(Node<E> node) {
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

  private Chain unlink(int from, int to) {
    var first = nodeAt(from);
    var last = nodeAfter(first, from, to - 1);
    int len = to - from;
    return unlink(new Chain(first, last, len));
  }

  @SuppressWarnings("unchecked")
  private Chain unlink(Chain chain) {
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
  private Node<E> node(int index) {
    return Check.that(index)
        .is(indexInclusiveOf(), this, indexOutOfBounds(index))
        .mapToObj(this::nodeAt);
  }

  /**
   * Returns the node at the specified position.
   */
  // @VisibleForTesting
  private Node<E> nodeAt(int index) {
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
  private Node<E> nodeAfter(Node<E> startNode, int startIndex, int index) {
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
  private Node<E> nodeBefore(Node<E> startNode, int startIndex, int index) {
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

  private void makeHead(Node<E> node) {
    node.prev = null;
    head = node;
  }

  private void makeTail(Node<E> node) {
    node.next = null;
    tail = node;
  }

  private static <T> void join(Node<T> prev, Node<T> next) {
    prev.next = next;
    next.prev = prev;
  }

  private Node<E> justBeforeHead() {
    Node<E> x = new Node<>(null);
    x.next = head;
    return x;
  }

  private Node<E> justAfterTail() {
    Node<E> x = new Node<>(null);
    x.prev = tail;
    return x;
  }

  private void checkInclusive(int index) {
    Check.that(index).is(indexInclusiveOf(), this, indexOutOfBounds(index));
  }

}
