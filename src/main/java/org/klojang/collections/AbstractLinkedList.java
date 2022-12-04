package org.klojang.collections;

import org.klojang.check.Check;

import java.util.*;
import java.util.function.Supplier;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.klojang.check.CommonChecks.*;
import static org.klojang.check.CommonChecks.notNull;
import static org.klojang.check.CommonExceptions.indexOutOfBounds;
import static org.klojang.check.CommonExceptions.noSuchElement;

abstract class AbstractLinkedList<E> implements List<E> {

  static Supplier<IllegalArgumentException> autoEmbedNotAllowed() {
    return () -> new IllegalArgumentException("list cannot be embedded within itself");
  }

  static Supplier<IllegalStateException> callNextFirst() {
    return () -> new IllegalStateException("Iterator.next() must be called first");
  }

  static Supplier<IllegalStateException> emptyList() {
    return () -> new IllegalStateException("illegal operation on empty list");
  }

  static Supplier<ConcurrentModificationException> concurrentModification() {
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
  static class Chain {

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


  Node<E> head;
  Node<E> tail;
  int sz;


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
