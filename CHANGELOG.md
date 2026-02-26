# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [1.2.1]

### Changed

- `GetResourceBundleRegistrar.registerCallbackForModule()`: replaced `synchronized(map)`
  with `ConcurrentHashMap.putIfAbsent()` to avoid defeating the lock-free design
- `LocalizationDelegate`: replaced `synchronized(lock)` with `ReentrantReadWriteLock`
  so read-heavy methods (`getBundleLocale()`, `getResourceBundle()`) no longer contend
  with writes
- `LocalizableLogger.getAvailableLocales()`: made the locale array `static final` and
  removed the defensive `clone()`, eliminating an allocation on every call
- `NestedResourceBundle`: replaced `Enumeration`-based iteration in `getKeys()` and
  `handleKeySet()` with bulk `keySet()` / `addAll()` operations; added `keySet()`
  override to include superBundle keys
- `AttributeCollectionResourceBundle.getKeys()`: replaced `Enumeration`-based iteration
  with `props.keySet()` / `parent.keySet()` and bulk `addAll()`
- `AttributeCollectionResourceBundle.constructAttributeCollectionObject()`: cached
  validated `Constructor` instances in a `ConcurrentHashMap` to avoid repeated
  reflection; extracted `resolveConstructor()` method
- `XMLResourceBundle.PropertiesDtdResolver`: refactored to follow HTTP redirects across
  hosts/protocols when fetching the Sun properties DTD; falls back to local DTD on
  network failure instead of throwing
- `JsonResourceBundle`: improved error messages for invalid JSON structure (non-object
  root, missing type field)

### Fixed

- `XMLResourceBundle.PropertiesDtdResolver`: unrecognized DTD system IDs are now blocked
  to prevent XXE attacks (previously only null system IDs were blocked)
- `README.md`: fixed typo in API Reference table (`AssociateResourceBundleLocator` →
  `AssociativeResourceBundleLocator`)

## [1.2]

### Changed

- Rewrote `JsonResourceBundle` to use Jackson's tree model (`ObjectMapper.readTree()`)
  instead of the streaming token parser, reducing ~260 lines to ~60
- Rewrote `XMLResourceBundle` to use the standard DOM API (`DocumentBuilder`)
  instead of Jackson XML's streaming parser, eliminating the `jackson-dataformat-xml`
  dependency entirely
- `XMLResourceBundle` constructor now throws only `IOException` (previously also
  threw `XMLStreamException`); XML parse errors are wrapped in `IOException`
- Adapted `PropertiesDtdResolver` from `XMLResolver` to `EntityResolver` for use
  with the DOM API
- Restored Java 17 as the minimum compiler target (removed use of
  `StackWalker.Option.DROP_METHOD_INFO` which required Java 22, and
  `Locale.of()` which required Java 19)

### Removed

- `jackson-dataformat-xml` dependency — XML bundles now use the standard
  `javax.xml.parsers` DOM API included in `java.xml`
- `jackson-core` explicit dependency (transitive via `jackson-databind`)
- `XMLStreamException` from the public API of `XMLResourceBundle`,
  `AssociativeResourceBundleLocator`, and `AssociativeResourceBundleControl`

## [1.1]

### Added

- `ResourcefulDelegate` delegation helper for `Resourceful` behavior

### Changed

- `LocalizableLogger` constructor visibility from `private` to `protected` to
  allow subclassing from other modules
- Upgraded Jackson from 2.21.0 (`com.fasterxml.jackson`) to 3.0.4 (`tools.jackson`)
- Upgraded JUnit from 5.13.4 / 1.13.4 to 6.0.3
- Upgraded maven-surefire-plugin from 3.5.4 to 3.5.5
- Minimum Java version is now 17 (required by Jackson 3 and JUnit 6)

## [1.0.0]

### Added

- `Localizable` and `Resourceful` interfaces for marking locale-sensitive
  classes and resource-dependent objects
- `LocalizableImpl` base class implementing `Localizable`
- `LocalizationDelegate` for polymorphic resource bundle resolution that
  mirrors class inheritance
- `Resource` for encapsulating a `Localizable` source and string key for lookup
- `JsonResourceBundle` for loading typed resources from JSON files
- `XMLResourceBundle` for loading typed resources from XML files
- `NestedResourceBundle` for composing resource bundles with parent fallback
- `AttributeCollection` interface for typed objects in JSON/XML bundles
- `AttributeCollectionResourceBundle` for serving `AttributeCollection`
  objects from resource bundles
- `LocalizableLogger` for logging with localized messages
- `AssociativeResourceBundleLocator` for multi-format bundle location
- `AssociativeResourceBundleControl` and `AssociativeResourceBundleControlProvider`
  for classpath resource bundle loading
- `AssociativeResourceBundleProvider` for module-path resource bundle loading
- `GetResourceBundleCallback` and `GetResourceBundleRegistrar` for
  cross-module bundle loading
- `ModuleResourceBundleCallback` default `GetResourceBundleCallback` implementation
- `ResourceStreamLoader` helper for loading resources via Modules or ClassLoaders
- `NoCallbackRegisteredForModuleException` for missing module callback errors
- Classpath and module-path test profiles
- Release profile with javadoc and source jar generation
