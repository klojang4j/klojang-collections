package org.klojang.collections;

import org.klojang.util.ExceptionMethods;

import java.lang.invoke.MethodHandle;

import static java.lang.invoke.MethodHandles.arrayConstructor;

final class InvokeUtils {

  private InvokeUtils() {
    throw new UnsupportedOperationException();
  }

  static Object newArray(Class<?> clazz, int length) {
    MethodHandle mh = arrayConstructor(clazz);
    try {
      return mh.invoke(length);
    } catch (Throwable t) {
      throw ExceptionMethods.uncheck(t);
    }
  }

}
