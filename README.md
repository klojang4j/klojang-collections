# Klojang Collections

Provides three additions to the Java Collections Framework:

1. **[TypeMap](/docs/1/api/org.klojang.collections/org/klojang/collections/TypeMap.html)**
   &#8212; an extension of the `Map` interface which maps Java types of values.
   Its `get()` and `containsKey()` methods leverage the structure of the Java type
   hierarchy to find a value for the requested key. If the requested key (a
   `Class` object) is not itself present in the map, but one of its supertypes is,
   then the value associated with the supertype is returned - or `true` in the case
   of `containsKey()`.
2. **[TypeSet](/docs/1/api/org.klojang.collections/org/klojang/collections/TypeSet.html)**
   &#8212; the `Set` counterpart to the `TypeMap` interface. Its `contains()`
   method behaves just like the `containsKey()` method of `TypeMap`.
3. **[WiredList](/docs/1/api/org.klojang.collections/org/klojang/collections/WiredList.html)**
   &#8212; a doubly-linked list much like the JDK's own `LinkedList` but exclusively
   focused on destructive edits like moving around (large) list segments - the one thing 
   where a doubly-linked list definitely has the edge over `ArrayList`.

## Getting Started

To use Klojang Collections, add the following dependency to your Maven POM file:

```xml
<dependency>
    <groupId>org.klojang</groupId>
    <artifactId>klojang-collections</artifactId>
    <version>1.0.0</version>
</dependency>
```

or Gradle build script:

```
implementation group: 'org.klojang', name: 'klojang-collections', version: '1.0.0'
```

## Documentation

The **Javadocs** for Klojang Check can be
found **[here](https://klojang4j.github.io/klojang-collections/1/api)**.

The latest **test coverage report** can be
found **[here](https://klojang4j.github.io/klojang-collections/1/coverage)**.

The latest **OWASP vulnerabilities report** can be
found **[here](https://klojang4j.github.io/klojang-collections/1/vulnerabilities/dependency-check-report.html)**
.


