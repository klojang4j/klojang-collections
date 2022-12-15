package org.klojang.collections;

import org.klojang.check.Check;
import org.klojang.check.Tag;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static java.util.Collections.singletonList;
import static org.klojang.check.CommonChecks.*;
import static org.klojang.check.CommonExceptions.noSuchElement;
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
 * {@code WiredList class} also features a {@link #reverseIterator0()} and a
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
 * in the list, but the underlying data structure itself. The {@code WiredList} class
 * is not thread-safe. Multiple threads mutating the same list can leave it in a
 * seriously corrupted state, with "dangling wires" between the list elements (i.e.
 * null pointers). While the iterators make a light-touch effort to detect and trap
 * concurrent modifications on the list, {@code WiredList} itself doesn't. Therefore,
 * synchronize carefully when multiple threads access the same list.
 *
 * @param <E> the type of the elements in the list
 * @author Ayco Holleman
 */
public final class WiredList<E> extends AbstractLinkedList<E> {

  //
  //
  //
  // ======================================================= //
  // ================ [ Iterator classes ]  ================ //
  // ======================================================= //
  //
  //
  //

  final class WFwdWiredIterator extends ForwardWiredIterator {

    private WFwdWiredIterator() {
      super();
    }

     private WFwdWiredIterator(Node<E> curr) {
      super(curr);
    }

    @Override
    public void remove() {
      Check.that(curr).isNot(sameAs(), beforeHead, callNextFirst());
      Check.that(sz).isNot(zero(), emptyList());
      if (curr == head) {
        destroy(curr);
        curr = beforeHead = justBeforeHead();
      } else {
        Check.that(curr = curr.prev)
            .is(notNull(), concurrentModification())
            .then(prev -> destroy(prev.next));
      }
    }

    @Override
    WiredIterator<E> getReverseWiredIterator(Node<E> curr) {
      return new WRevWiredIterator(curr);
    }

  }

  final class WRevWiredIterator extends ReverseWiredIterator {

    private WRevWiredIterator() {
      super();
    }

    private WRevWiredIterator(Node<E> curr) {
      super(curr);
    }

    @Override
    public void remove() {
      Check.that(curr).isNot(sameAs(), afterTail, callNextFirst());
      Check.that(sz).isNot(zero(), emptyList());
      if (curr == tail) {
        destroy(curr);
        curr = afterTail = justAfterTail();
      } else {
        Check.that(curr = curr.next)
            .is(notNull(), concurrentModification())
            .then(next -> destroy(next.prev));
      }
    }

    @Override
    WiredIterator<E> getForwardWiredIterator(Node<E> curr) {
      return new WFwdWiredIterator(curr);
    }

  }

  //
  //
  //
  // ======================================================= //
  // ==================== [ WiredList ] ==================== //
  // ======================================================= //
  //
  //
  //

  /**
   * Returns a new, empty {@code WiredList}. Note that, although the {@code of(..)}
   * methods look like the {@code List.of(...)} methods, they return ordinary,
   * mutable, {@code null}-accepting {@code WiredList} instances.
   *
   * @param <E> the type of the elements in the list
   * @return a new, empty {@code WiredList}
   */
  public static <E> WiredList<E> of() {
    return new WiredList<>();
  }

  /**
   * Returns a new {@code WiredList} containing the specified element.
   *
   * @param e the element
   * @param <E> the type of the elements in the list
   * @return a new {@code WiredList} containing the specified elements
   */
  public static <E> WiredList<E> of(E e) {
    return new WiredList<E>().append(e);
  }

  /**
   * Returns a new {@code WiredList} containing the specified elements.
   *
   * @param e0 the first element in the list
   * @param e1 the second element in the list
   * @param <E> the type of the elements in the list
   * @return a new {@code WiredList} containing the specified elements
   */
  public static <E> WiredList<E> of(E e0, E e1) {
    return new WiredList<E>().append(e0).append(e1);
  }

  /**
   * Returns a new {@code WiredList} containing the specified elements.
   *
   * @param e0 the first element in the list
   * @param e1 the second element in the list
   * @param e2 the third element in the list
   * @param moreElems more elements to include in the list
   * @param <E> the type of the elements in the list
   * @return a new {@code WiredList} containing the specified elements
   */
  @SafeVarargs
  public static <E> WiredList<E> of(E e0, E e1, E e2, E... moreElems) {
    Check.notNull(moreElems, Tag.ARRAY);
    var wl = new WiredList<E>();
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
   * Returns a new {@code WiredList} containing the specified elements.
   *
   * @param elements the elements to add to the list
   * @param <E> the type of the elements in the list
   * @return a new {@code WiredList} containing the specified elements
   */
  public static <E> WiredList<E> ofElements(E[] elements) {
    Check.notNull(elements, Tag.ARRAY);
    var wl = new WiredList<E>();
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
   * Concatenates the provided {@code WiredList} instances. This is a destructive
   * operation for the {@code WiredList} instances in the provided {@code List}. They
   * will be empty when the method returns.
   *
   * @see #attach(WiredList)
   * @see #group(List)
   * @param lists the {@code WiredList} instances to concatenate
   * @param <E> the type of the elements in the list
   * @return a new {@code WiredList} containing the elements in the individual
   *     {@code WiredList} instances
   */
  public static <E> WiredList<E> join(List<WiredList<E>> lists) {
    WiredList<E> wl = new WiredList<>();
    Check.notNull(lists).ok().forEach(wl::attach);
    return wl;
  }

  /**
   * Creates a new, empty {@code WiredList}.
   */
  public WiredList() {}

  /**
   * Creates a new {@code WiredList} containing the elements in the specified
   * {@code Collection}.
   *
   * @param c the collection whose elements to copy to this {@code WiredList}
   */
  public WiredList(Collection<? extends E> c) {
    addAll(0, c);
  }

  @SuppressWarnings({"unchecked"})
  private WiredList(Chain chain) {
    makeHead(chain.head);
    makeTail(chain.tail);
    sz = chain.length;
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
   * @return this instance
   */
  @SuppressWarnings("unchecked")
  public WiredList<E> set(int index, E e0, E e1, E... moreElems) {
    set0(index, e0, e1, moreElems);
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
    return setIf0(index, condition, value);
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
    return destroy(node(index));
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
          destroy(x);
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
    Check.notNull(filter, "filter");
    int size = sz;
    for (var x = head; x != null; ) {
      if (filter.test(x.val)) {
        var next = x.next;
        destroy(x);
        x = next;
      } else {
        x = x.next;
      }
    }
    return size != this.sz;
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
   * @return this instance
   */
  public WiredList<E> prepend(E value) {
    prepend0(value);
    return this;
  }

  /**
   * Inserts the specified collection at the start of the list, right-shifting the
   * original elements.
   *
   * @param values The values to prepend to the list
   * @return this instance
   */
  public WiredList<E> prependAll(Collection<? extends E> values) {
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
   * @return this instance
   */
  public WiredList<E> append(E value) {
    add(value);
    return this;
  }

  /**
   * Appends the specified collection to this {@code WiredList}.
   *
   * @param values The values to append to the list
   * @return this instance
   * @see #addAll(Collection)
   * @see #attach(WiredList)
   */
  public WiredList<E> appendAll(Collection<? extends E> values) {
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
   * @return this instance
   */
  public WiredList<E> insert(int index, E value) {
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
   * @return this instance
   * @see #addAll(int, Collection)
   */
  public WiredList<E> insertAll(int index, Collection<? extends E> values) {
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
   * @return this instance
   */
  public WiredList<E> deleteFirst() {
    Check.that(sz).isNot(zero(), noSuchElement());
    destroy(head);
    return this;
  }

  /**
   * Removes the last element from the list. A {@link NoSuchElementException} is
   * thrown if the list is empty.
   *
   * @return this instance
   */
  public WiredList<E> deleteLast() {
    Check.that(sz).isNot(zero(), noSuchElement());
    destroy(tail);
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
   * @return this instance
   * @see #replace(int, int, WiredList)
   */
  public WiredList<E> replaceAll(int fromIndex,
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
      cut(fromIndex, toIndex).clear();
      if (!values.isEmpty()) {
        insert(fromIndex, Chain.of(values));
      }
    }
    return this;
  }

  /**
   * Replaces the segment between {@code fromIndex} and {@code toIndex} with the
   * elements in the specified list. This method is functionally equivalent to
   * {@link #replaceAll(int, int, Collection) replace}, but more efficient. However,
   * it will leave the specified list empty. If you don't want this to happen, use
   * {@code replace}.
   *
   * @param fromIndex the start index (inclusive) of the segment to replace
   * @param toIndex the end index (exclusive) of
   * @param other the values to replace the segment with
   * @return this instance
   */
  public WiredList<E> replace(int fromIndex,
      int toIndex,
      WiredList<? extends E> other) {
    int len = Check.fromTo(this, fromIndex, toIndex);
    Check.notNull(other, className).isNot(sameAs(), this, autoEmbedNotAllowed());
    if (len != 0) {
      cut(fromIndex, toIndex).clear();
    }
    if (!other.isEmpty()) {
      insert(fromIndex, other.unlink(0, other.sz));
    }
    return this;
  }

  /**
   * Returns a copy of this {@code WiredList}. Changes made to the copy will not
   * propagate to this instance, and vice versa.
   *
   * @return a deep copy of this {@code WiredList}
   */
  public WiredList<E> copy() {
    return sz > 0 ? new WiredList<>(Chain.copyOf(head, sz)) : WiredList.of();
  }

  /**
   * Returns a copy of the specified segment. Changes made to the copy will not
   * propagate to this instance, and vice versa.
   *
   * @param fromIndex the start index (inclusive) of the segment
   * @param toIndex the end index (exclusive) of the segment
   * @return a deep copy of the specified segment
   */
  public WiredList<E> copy(int fromIndex, int toIndex) {
    int len = Check.fromTo(this, fromIndex, toIndex);
    return len > 0
        ? new WiredList<>(Chain.copyOf(nodeAt(fromIndex), len))
        : WiredList.of();
  }

  /**
   * Shrinks the list to between the specified boundaries. If {@code toIndex} is
   * equal to {@code fromIndex}, the list will, in effect, be
   * {@link #clear() cleared}.
   *
   * @param fromIndex the index (inclusive) of the new start of the list
   * @param toIndex the index (exclusive) of the new end of the list
   * @return this instance
   */
  public WiredList<E> shrink(int fromIndex, int toIndex) {
    int len = Check.fromTo(this, fromIndex, toIndex);
    if (len == 0) {
      clear();
    } else if (len != sz) {
      Node<E> curr = head;
      for (int i = 0; i < fromIndex; ++i) { // make gc happy
        var next = curr.next;
        curr.next = curr.prev = null;
        curr.val = null;
        curr = next;
      }
      head = curr;
      curr = tail;
      for (int i = toIndex; i < sz; ++i) {
        var prev = curr.prev;
        curr.next = curr.prev = null;
        curr.val = null;
        curr = prev;
      }
      tail = curr;
      sz -= len;
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
  public WiredList<E> cut(int fromIndex, int toIndex) {
    if (Check.fromTo(this, fromIndex, toIndex) > 0) {
      return new WiredList<>(unlink(fromIndex, toIndex));
    }
    return WiredList.of();
  }

  /**
   * Inserts this list into the specified list at the specified position. Equivalent
   * to {@link #embed(int, WiredList) into.embed(index, this)}. This list will be
   * empty afterwards. Note that this method does not return this <i>list</i> but the
   * paste-into list.
   *
   * @param into the list into which to insert this list
   * @param index the index at which to insert this list
   * @return the specified list
   */
  public WiredList<? super E> paste(WiredList<? super E> into, int index) {
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
   * @return this instance
   */
  public WiredList<E> embed(int index, WiredList<? extends E> other) {
    checkInclusive(index);
    Check.notNull(other, className).isNot(sameAs(), this, autoEmbedNotAllowed());
    if (!other.isEmpty()) {
      insert(index, new Chain(other.head, other.tail, other.sz));
      // Reset but don't clear the embedded list, because that
      // would nullify the nodes we just embedded in this list
      other.head = null;
      other.tail = null;
      other.sz = 0;
    }
    return this;
  }

  /**
   * Swaps the two list segments defined by the specified boundary indexes. In other
   * words, once this method returns, the first list segment will start where the
   * second list segment originally started, and vice versa. The list segments must
   * not overlap and they must both contain at least one element. They need not have
   * the same number of elements, though.
   *
   * @param from1 the from-index (inclusive) of the first segment
   * @param to1 the to-index (exclusive) of the first segment
   * @param from2 the from-index (inclusive) of the second segment
   * @param to2 the to-index (exclusive) of the second segment
   * @return this instance
   */
  public WiredList<E> swap(int from1, int to1, int from2, int to2) {
    swap0(from1, to1, from2, to2);
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
   * @return this instance
   */
  public WiredList<E> exchange(int myFromIndex,
      int myToIndex,
      WiredList<E> other,
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
   * @return this instance
   */
  public WiredList<E> embed(int myIndex,
      WiredList<? extends E> other,
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
   * @return this instance
   * @see #join(List)
   */
  public WiredList<E> attach(WiredList<? extends E> other) {
    Check.notNull(other).isNot(sameAs(), this, autoEmbedNotAllowed());
    if (other.sz != 0) {
      attach0(other);
    }
    return this;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void attach0(WiredList other) {
    if (sz == 0) {
      head = other.head;
    } else {
      join(tail, other.head);
    }
    tail = other.tail;
    sz += other.sz;
    other.head = null;
    other.tail = null;
    other.sz = 0;
  }

  /**
   * Reorders the elements according to the specified criteria. The elements
   * satisfying the first criterion (if any) will come first in the list, the
   * elements satisfying the second criterion (if any) will come second, etc. The
   * elements that did not satisfy any criterion will come last in the list.
   *
   * @param criteria the criteria used to group the elements
   * @return this instance
   */
  public WiredList<E> defragment(List<Predicate<? super E>> criteria) {
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
   * @return this instance
   */
  @SuppressWarnings({"rawtypes"})
  public WiredList<E> defragment(boolean keepRemainder,
      List<Predicate<? super E>> criteria) {
    Check.that(criteria).is(deepNotEmpty());
    List<WiredList<E>> groups = createGroups(criteria);
    Chain rest = new Chain(head, tail, sz);
    sz = 0;
    for (WiredList wl : groups) {
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
   * {@code WiredList} is left with all elements that did not satisfy any criterion,
   * and it will be the last element in the returned list-of-lists. In other words,
   * the size of the returned list-of-lists is the number of criteria plus one. You
   * can use the {@link #join(List) join} method to create a single "defragmented"
   * list again.
   * <p>
   * Elements will never be placed in more than one group. As soon as an element is
   * found to satisfy a criterion it is placed in the corresponding group and the
   * remaining criteria are skipped.
   * <p>
   * The runtime type of the returned list-of-lists {@code WiredList<WiredList<E>>}.
   * If you don't care about the exact type of the returned {@code List}, you can
   * simply write:
   *
   * <blockquote><pre>{@code
   * WiredList<String> wl = ...;
   * List<List<String>> groups = wl.group(...);
   * }</pre></blockquote>
   * <p>
   * Otherwise use any combination of {@code List} and {@code WiredList} that suits
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
    List<WiredList<E>> groups = createGroups(criteria);
    var result = new WiredList<>(groups);
    result.add(this);
    return (L1) result;
  }

  @SuppressWarnings({"rawtypes"})
  private WiredList<WiredList<E>> createGroups(List<Predicate<? super E>> criteria) {
    WiredList<WiredList<E>> groups = new WiredList<>();
    criteria.forEach(c -> groups.append(new WiredList<>()));
    for (Node<E> node = head; node != null; ) {
      var next = node.next;
      for (int i = 0; i < criteria.size(); ++i) {
        if (criteria.get(i).test(node.val)) {
          unlink(node);
          WiredList wl = groups.get(i);
          wl.insert(wl.size(), node);
          break;
        }
      }
      node = next;
    }
    return groups;
  }

  /**
   * Splits this {@code WiredList} into multiple {@code WiredList} instances of the
   * specified size. The partitions are chopped off from the {@code WiredList} and
   * then placed in a separate {@code WiredList}. The last element in the returned
   * list-of-lists is <i>this</i> {@code WiredList}, and it will now contain at most
   * {@code size} elements.
   * <p>
   * The runtime type of the return value is {@code WiredList<WiredList<E>>}. If you
   * don't care about the exact type of the returned {@code List}, you can simply
   * write:
   *
   * <blockquote><pre>{@code
   * WiredList<String> wl = ...;
   * List<List<String>> partitions = wl.partition(3);
   * }</pre></blockquote>
   * <p>
   * Otherwise use any combination of {@code List} and {@code WiredList} that suits
   * your needs.
   *
   * @param size The desired size of the partitions
   * @param <L0> the type of the lists within the returned list
   * @param <L1> the type of returned list
   * @return a list of {@code WiredList} instances of the specified size
   */
  @SuppressWarnings("unchecked")
  public <L0 extends List<E>, L1 extends List<L0>> L1 partition(int size) {
    Check.that(size).is(gt(), 0);
    WiredList<WiredList<E>> partitions = new WiredList<>();
    while (sz > size) {
      Chain chain = new Chain(head, nodeAt(size - 1), size);
      partitions.append(new WiredList<>(unlink(chain)));
    }
    partitions.append(this);
    return (L1) partitions;
  }

  /**
   * Splits this {@code WiredList} into the specified number of equally-sized
   * {@code WiredList} instances. The last element in the returned list-of-lists is
   * this {@code WiredList}, and it will contain the remainder of the elements after
   * dividing the list size by {@code numPartitions}. The runtime type of the return
   * value is {@code WiredList<WiredList<E>>}. If you don't care about the exact type
   * of the returned {@code List}, you can simply write:
   *
   * <blockquote><pre>{@code
   * WiredList<String> wl = ...;
   * List<List<String>> partitions = wl.split(3);
   * }</pre></blockquote>
   * <p>
   * Otherwise use any combination of {@code List} and {@code WiredList} that suits
   * your needs.
   *
   * @param numPartitions The number of {@code WiredList} instances to split this
   *     {@code WiredList} into
   * @param <L0> the type of the lists within the returned list
   * @param <L1> the type of returned list
   * @return a list containing the specified number of {@code WiredList} instances
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
   * @return a {@code WiredList} containing all elements preceding the first element
   *     that does not satisfy the condition
   */
  public WiredList<E> lchop(Predicate<? super E> criterion) {
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
    return new WiredList<>(unlink(new Chain(first, last, len)));
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
   * @return a {@code WiredList} containing all elements following the last element
   *     that does not satisfy the condition
   */
  public WiredList<E> rchop(Predicate<? super E> criterion) {
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
    return new WiredList<>(unlink(new Chain(first, last, len)));
  }

  /**
   * Reverses the order of the elements in this {@code WiredList}.
   *
   * @return this instance
   */
  public WiredList<E> reverse() {
    reverse0();
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
   * @return this instance
   */
  public WiredList<E> move(int fromIndex, int toIndex, int newFromIndex) {
    int len = Check.fromTo(this, fromIndex, toIndex);
    Check.that(newFromIndex, "target index").is(indexInclusiveOf(), this);
    if (len != 0) {
      if (newFromIndex > fromIndex) {
        moveRight(fromIndex, toIndex, newFromIndex);
      } else if (newFromIndex < fromIndex) {
        moveLeft(fromIndex, toIndex, newFromIndex);
      }
    }
    return this;
  }

  /**
   * Removes all elements from this list. The list will be empty after this call
   * returns.
   */
  @Override
  public void clear() {
    if (sz != 0) {
      var curr = head;
      do {
        var next = curr.next;
        curr.val = null;
        curr.prev = curr.next = null;
        curr = next;
      } while (curr != tail);
      head = tail = null;
      sz = 0;
    }
  }

  /**
   * Returns an {@code Iterator} that traverses the list from the last element to the
   * first. See also {@link #iterator()}.
   *
   * @return an {@code Iterator} that traverses the list from the last element to the
   *     first
   */
  public Iterator<E> reverseIterator() {
    return super.reverseIterator0();
  }

  /**
   * Returns a {@link WiredIterator} that traverses the list from the first element
   * to the last.
   *
   * @return a {@code WiredIterator} that traverses the list from the first element
   *     to the last
   */
  public WiredIterator<E> wiredIterator() {
    return new WFwdWiredIterator();
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
    return reverse ? new WRevWiredIterator() : new WFwdWiredIterator();
  }

  /**
   * Returns an array containing the elements within the specified region of this
   * list.
   *
   * @param fromIndex the start index (inclusive) of the region
   * @param toIndex the end index (exclusive) of the region
   * @return an array containing the elements within the specified region of this
   *     list
   */
  public Object[] regionToArray(int fromIndex, int toIndex) {
    return toArray0(fromIndex, toIndex);
  }

  /**
   * Copies the specified region within this list to the specified position within
   * the specified array. The array must be large enough to copy the entire region to
   * the specified position.
   *
   * @param fromIndex the start index (inclusive) of the region
   * @param toIndex the end index (exclusive) of the region
   * @param target the array to which to copy the elements
   * @param offset the offset within the array
   */
  public void regionToArray(int fromIndex,
      int toIndex,
      Object[] target,
      int offset) {
    toArray0(fromIndex, toIndex, target, offset);
  }

  /**
   * Throws an {@code UnsupportedOperationException}. The specification for this
   * method requires that non-structural changes in the returned list are reflected
   * in the original list (and vice versa). However, except for the
   * {@link #set(int, Object)} method, all changes to a {@code WiredList} <i>are</i>
   * structural changes. {@code WiredList} does provide a method that returns a
   * sublist ({@link #copy(int, int) copySegment}). It just has no relation to the
   * original list any longer.
   */
  @Override
  public List<E> subList(int fromIndex, int toIndex) {
    throw new UnsupportedOperationException();
  }

  // Make garbage collector happy
  private E destroy(Node<E> node) {
    E val = node.val;
    unlink(node);
    node.val = null;
    node.prev = node.next = null;
    return val;
  }

}
