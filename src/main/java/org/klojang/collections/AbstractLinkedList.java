package org.klojang.collections;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

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

}
