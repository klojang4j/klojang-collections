package org.klojang.collections;

import org.klojang.util.ArrayMethods;

import java.util.Arrays;
import java.util.Comparator;

import static org.klojang.util.ClassMethods.*;

final class PrettyTypeComparator implements Comparator<Class<?>> {

  @Override
  public int compare(Class<?> c1, Class<?> c2) {
    if (c1 == c2) {
      return 0;
    }
    if (c1.isPrimitive()) {
      return -1;
    }
    if (c2.isPrimitive()) {
      return 1;
    }
    if (isWrapper(c1)) {
      return -1;
    }
    if (isWrapper(c2)) {
      return 1;
    }
    if (c1.isEnum()) {
      return -1;
    }
    if (c2.isEnum()) {
      return 1;
    }
    if (c1 == Object.class) {
      return 1;
    }
    if (c2 == Object.class) {
      return -1;
    }
    if (c1.isArray()) {
      if (c2.isArray()) {
        return compare(c1.getComponentType(), c2.getComponentType());
      }
      return 1;
    }
    if (c2.isArray()) {
      return -1;
    }
    if (c1.isInterface()) {
      if (c2.isInterface()) {
        if (getAllInterfaces(c1).size() < getAllInterfaces(c2).size()) {
          return 1;
        }
        if (getAllInterfaces(c1).size() > getAllInterfaces(c2).size()) {
          return -1;
        }
      }
      return 1;
    }
    if (c2.isInterface()) {
      return -1;
    }
    if (countAncestors(c1) < countAncestors(c2)) {
      return 1;
    }
    if (countAncestors(c1) > countAncestors(c2)) {
      return -1;
    }
    // Compare the number of implemented interfaces for regular
    // classes. Thus, classes not implementing any interface are
    // regarded as more primitive and should come first
    if (getAllInterfaces(c1).size() < getAllInterfaces(c2).size()) {
      return 1;
    }
    if (getAllInterfaces(c1).size() > getAllInterfaces(c2).size()) {
      return -1;
    }
    // compare by inverse class name
    String[] s1 = ArrayMethods.reverse(c1.getName().split("\\."));
    String[] s2 = ArrayMethods.reverse(c2.getName().split("\\."));
    return Arrays.compare(s1, s2);
  }

}
