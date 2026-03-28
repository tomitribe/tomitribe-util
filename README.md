# Tomitribe Util

A cornucopia of useful utility classes.

## Data Encoding

| Class | Description |
|-------|-------------|
| [Base32](src/main/java/org/tomitribe/util/Base32.java) | Case-insensitive Base32 encoding and decoding |
| [Base58](src/main/java/org/tomitribe/util/Base58.java) | Bitcoin-style Base58 encoding and decoding |
| [Base64](src/main/java/org/tomitribe/util/Base64.java) | RFC 2045 Base64 encoding and decoding |
| [Binary](src/main/java/org/tomitribe/util/Binary.java) | Binary string to byte array conversion |
| [Hex](src/main/java/org/tomitribe/util/Hex.java) | Hexadecimal string to byte array conversion |

## Units and Measurement

| Class | Description |
|-------|-------------|
| [Duration](src/main/java/org/tomitribe/util/Duration.java) | Parses and manipulates time durations like `3 hours and 12 minutes` |
| [Size](src/main/java/org/tomitribe/util/Size.java) | Parses and manipulates data sizes like `256mb` |
| [SizeUnit](src/main/java/org/tomitribe/util/SizeUnit.java) | Enum for data size unit conversion (bytes through terabytes) |
| [Bytes](src/main/java/org/tomitribe/util/Bytes.java) | Accumulates and compacts byte values across size units |
| [TimeUtils](src/main/java/org/tomitribe/util/TimeUtils.java) | Converts milliseconds to human-readable time strings |

## I/O and Filesystem

| Class | Description |
|-------|-------------|
| [IO](src/main/java/org/tomitribe/util/IO.java) | Read, write, and copy streams, files, and channels |
| [Files](src/main/java/org/tomitribe/util/Files.java) | File filtering, walking, deletion, and iteration |
| [Zips](src/main/java/org/tomitribe/util/Zips.java) | Extract ZIP and JAR files to a directory |
| [Archive](src/main/java/org/tomitribe/util/Archive.java) | In-memory JAR-like structure using lambdas for lazy byte loading |
| [Pipe](src/main/java/org/tomitribe/util/Pipe.java) | Pipes an InputStream to an OutputStream as a Runnable/Future |
| [JarLocation](src/main/java/org/tomitribe/util/JarLocation.java) | Finds the JAR file on disk that contains a given class |
| [Mvn](src/main/java/org/tomitribe/util/Mvn.java) | Locates Maven artifacts in the local repository by coordinates |

## Strongly-Typed Directory Proxies

Define an interface whose method names mirror a directory structure; get back a proxy that returns `File` or `Path` references with compile-time safety.

| Class | Description |
|-------|-------------|
| [Dir](src/main/java/org/tomitribe/util/dir/Dir.java) | `java.io.File`-based directory proxy |
| [Dir (paths)](src/main/java/org/tomitribe/util/paths/Dir.java) | `java.nio.file.Path`-based directory proxy |
| [Paths](src/main/java/org/tomitribe/util/paths/Paths.java) | Utilities for walking and filtering `Path` trees |

Supporting annotations: [@Filter](src/main/java/org/tomitribe/util/dir/Filter.java), [@Walk](src/main/java/org/tomitribe/util/dir/Walk.java), [@Mkdir](src/main/java/org/tomitribe/util/dir/Mkdir.java), [@Mkdirs](src/main/java/org/tomitribe/util/dir/Mkdirs.java), [@Name](src/main/java/org/tomitribe/util/dir/Name.java), [@Parent](src/main/java/org/tomitribe/util/dir/Parent.java)

## Collections and Iteration

| Class | Description |
|-------|-------------|
| [FilteredIterable](src/main/java/org/tomitribe/util/collect/FilteredIterable.java) | Iterable wrapper that filters elements with a predicate |
| [FilteredIterator](src/main/java/org/tomitribe/util/collect/FilteredIterator.java) | Iterator wrapper that filters elements with a predicate |
| [CompositeIterable](src/main/java/org/tomitribe/util/collect/CompositeIterable.java) | Chains multiple iterables into a single sequence |
| [CompositeIterator](src/main/java/org/tomitribe/util/collect/CompositeIterator.java) | Chains multiple iterators into a single sequence |
| [AbstractIterator](src/main/java/org/tomitribe/util/collect/AbstractIterator.java) | Base class for custom iterators with an `advance()` pattern |
| [Suppliers](src/main/java/org/tomitribe/util/collect/Suppliers.java) | Converts Suppliers into lazy Iterators and Streams |
| [ObjectMap](src/main/java/org/tomitribe/util/collect/ObjectMap.java) | Exposes an object's getters and fields as a `Map<String, Object>` |

## Reflection

| Class | Description |
|-------|-------------|
| [Generics](src/main/java/org/tomitribe/util/reflect/Generics.java) | Extracts generic type parameters from fields, methods, and parameters |
| [Parameter](src/main/java/org/tomitribe/util/reflect/Parameter.java) | Represents a method parameter with its annotations and types |
| [Reflection](src/main/java/org/tomitribe/util/reflect/Reflection.java) | Iterates over methods and parameters |
| [Classes](src/main/java/org/tomitribe/util/reflect/Classes.java) | Maps between primitive types and their wrapper classes |
| [SetAccessible](src/main/java/org/tomitribe/util/reflect/SetAccessible.java) | PrivilegedAction for setting accessibility on reflected members |
| [StackTraceElements](src/main/java/org/tomitribe/util/reflect/StackTraceElements.java) | Utilities for working with stack traces |

## Type Conversion

| Class | Description |
|-------|-------------|
| [Converter](src/main/java/org/tomitribe/util/editor/Converter.java) | Converts between Java types using PropertyEditors, constructors, and static factory methods |
| [AbstractConverter](src/main/java/org/tomitribe/util/editor/AbstractConverter.java) | Base class for PropertyEditor implementations |
| [Editors](src/main/java/org/tomitribe/util/editor/Editors.java) | Finds and registers PropertyEditor instances by type |
| [DateEditor](src/main/java/org/tomitribe/util/editor/DateEditor.java) | PropertyEditor for parsing strings into Date objects |
| [PathEditor](src/main/java/org/tomitribe/util/editor/PathEditor.java) | PropertyEditor for `java.nio.file.Path` |
| [CharacterEditor](src/main/java/org/tomitribe/util/editor/CharacterEditor.java) | PropertyEditor for Character |

## Strings and Formatting

| Class | Description |
|-------|-------------|
| [Join](src/main/java/org/tomitribe/util/Join.java) | Joins collections and arrays with a delimiter |
| [StringTemplate](src/main/java/org/tomitribe/util/StringTemplate.java) | `{placeholder}` string replacement with customizable delimiters |
| [Strings](src/main/java/org/tomitribe/util/Strings.java) | Case conversion utilities |
| [Escapes](src/main/java/org/tomitribe/util/Escapes.java) | Unescapes backslash sequences like `\n` and `\t` |
| [Formats](src/main/java/org/tomitribe/util/Formats.java) | Helpers for difficult-to-remember Formatter conversions |
| [PrintString](src/main/java/org/tomitribe/util/PrintString.java) | PrintStream that writes to an internal string buffer |

## Numeric and Low-Level

| Class | Description |
|-------|-------------|
| [Ints](src/main/java/org/tomitribe/util/Ints.java) | Convert integers to and from byte arrays and hex |
| [Longs](src/main/java/org/tomitribe/util/Longs.java) | Convert longs to and from byte arrays |

## Hashing

| Class | Description |
|-------|-------------|
| [XxHash64](src/main/java/org/tomitribe/util/hash/XxHash64.java) | 64-bit xxHash non-cryptographic hash |
| [XxHash32](src/main/java/org/tomitribe/util/hash/XxHash32.java) | 32-bit xxHash non-cryptographic hash |
| [Slice](src/main/java/org/tomitribe/util/hash/Slice.java) | Low-level byte buffer for efficient memory access |
| [Slices](src/main/java/org/tomitribe/util/hash/Slices.java) | Slice allocation and resizing |
| [SizeOf](src/main/java/org/tomitribe/util/hash/SizeOf.java) | Calculates memory size of Java objects using Unsafe |

## Configuration

| Class | Description |
|-------|-------------|
| [Options](src/main/java/org/tomitribe/util/Options.java) | Strongly-typed Properties wrapper with enum and collection support |
| [SuperProperties](src/main/java/org/tomitribe/util/SuperProperties.java) | Extended Properties with comments, attributes, and case-insensitive lookup |

## Other

| Class | Description |
|-------|-------------|
| [Version](src/main/java/org/tomitribe/util/Version.java) | Comparable version number parsing |
| [Futures](src/main/java/org/tomitribe/util/Futures.java) | Combines multiple Futures into a single `Future<List>` |
