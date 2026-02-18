# i18n-core

A Java internationalization framework that:

- Enables developers to designate Locale-sensitive objects via the Localizable
  and Resourceful interfaces
- Extends the standard `ResourceBundle` API with JSON and XML resource bundles
  to serve complex, typed objects without code or compilation
- Supports polymorphic resource inheritance to enable re-use of localized content
- Provides locale change events for dynamic Locale selection
- Supports ResourceBundle deployment in either classpath or module mode

## Design Philosophy

### Get it right the first time

Too often, software is built for a local market first, then
reworked to support internationalization, then localized for
additional markets. That middle step is rework, and rework
often produces a lower quality result at higher cost than
getting it right the first time. An internationalization
framework should make it natural to build global software from
the start, so that internationalization is part of the design
rather than a retrofit.

### Capture the designer's intent

Designers need a vocabulary for expressing what is
locale-sensitive and what is not. The `Localizable` and
`Resourceful` interfaces provide that vocabulary. Marking a
class as `Localizable` declares that it has locale-dependent
behavior and can serve as a source of localized resources.
Marking an object as `Resourceful` declares that it depends on
an external source for its localized content. These distinctions
are visible in the design, not buried in implementation details.

### Leverage class context for correct translations

A single English word may require different translations
depending on context. The word "cancel" on a button that
dismisses a dialog has a different meaning than "cancel" on a
radio button that voids a financial instrument. Translators
need context to choose the right term, and the class hierarchy
provides that context naturally.

Polymorphic properties allow resource bundles to follow the same
inheritance rules as the classes they serve. A `ResourceBundle`
for class `Foo extends Bar` first searches `Foo`'s resources,
then falls back to `Bar`'s resources, mirroring method
resolution. This enables appropriate reuse: shared translations
are defined once in a base class, while subclasses override only
what their specific context requires.

### Maximize reuse without sacrificing quality

Reusing localized resources saves time and money, but reusing
them out of context results in poor translations. The
polymorphic property mechanism strikes the right balance.
Resources are inherited by default, so common translations are
never duplicated. But each class can override any resource to
provide a translation that fits its specific context. If two
classes send the same message, that may be an opportunity for
further abstraction in the design.

### Localize complex types, without code or compilation

`PropertyResourceBundle` supports only strings.
`ListResourceBundle` supports arbitrary objects but requires
Java code and a compilation step for every locale. JSON and XML
resource bundles eliminate this trade-off: they can represent
complex typed objects (images, structured settings, grouped
properties) while remaining editable by translators and
localizers without a development environment. Adding a new
locale or changing a tooltip is an edit to a resource file, not
a code change.

### Support dynamic locale selection

Applications, particularly multilingual web applications, may
need to change locale at runtime. The `Localizable` interface
includes a `LocaleEvent` mechanism so that when one object
changes its locale, all interested listeners can update
themselves automatically.

### Deploy ResourceBundles on the classpath as well as via JPMS

Deployment of ResourceBundles in modular applications requires
the use of the ResourceBundleProvider service API. This can
cause rework when changing the deployment model. The framework
supports use of ResourceBundles in either deployment model, so
long as the additional module declarations and service
configuration are provided for module deployment.

## Requirements

- Java 16 or later
- Jackson Databind and Jackson Dataformat XML
  (provided as transitive dependencies)

## Installation

### Maven

```xml
<dependency>
    <groupId>dev.javai18n</groupId>
    <artifactId>i18n-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Module Declaration

```java
module my.module
{
    requires dev.javai18n.core;
}
```

## Quick Start

### 1. Define a Localizable class

```java
public class MyComponent extends LocalizableImpl
{
    static
    {
        // Required once per module — registers a callback
        // so the library can load resource bundles from
        // this module's context.
        // The callback argument must specify a module singleton
        // implementation of the GetResourceBundleCallback
        // interface.
        GetResourceBundleRegistrar
            .registerGetResourceBundleCallback(callback);
    }

    public String getGreeting()
    {
        return getResourceBundle().getString("greeting");
    }
}
```

### 2. Create a resource bundle

By convention, the library appends `Bundle` to the class name
when searching for resources. For `com.example.MyComponent`, it
searches for `com/example/MyComponentBundle` in these formats
(in order):

1. Java class
2. JSON (`.json`)
3. XML (`.xml`)
4. Properties (`.properties`)

**MyComponentBundle.properties:**
```properties
greeting=Hello!
```

**MyComponentBundle_fr.properties:**
```properties
greeting=Bonjour!
```

### 3. Use it

```java
MyComponent comp = new MyComponent();

comp.setBundleLocale(Locale.ENGLISH);
comp.getGreeting();  // "Hello!"

comp.setBundleLocale(Locale.FRENCH);
comp.getGreeting();  // "Bonjour!"
```

## Resource Bundle Formats

### Properties

Standard Java `.properties` files with string key-value pairs.

### JSON

JSON resource bundles support strings, numbers, booleans,
arrays, and custom typed objects:

```json
{
    "greeting": "Hello!",
    "count": 42,
    "colors": ["red", "green", "blue"],
    "settings":
    {
        "type": "com.example.AppSettings",
        "theme": "dark",
        "fontSize": 14
    }
}
```

### XML

XML resource bundles use a superset of the standard Java
properties DTD:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "properties.dtd">
<properties>
    <entry key="greeting">Hello!</entry>
    <entry key="colors">
        <array>
            <item>red</item>
            <item>green</item>
            <item>blue</item>
        </array>
    </entry>
    <entry key="settings">
        <object type="com.example.AppSettings">
            <entry key="theme">dark</entry>
            <entry key="fontSize">14</entry>
        </object>
    </entry>
</properties>
```

### Custom Objects via AttributeCollection

To use typed objects in JSON or XML bundles, implement the
`AttributeCollection` interface and register the package:

```java
public class AppSettings
        implements AttributeCollection
{

    private String theme;
    private int fontSize;

    // public no-arg constructor required
    public AppSettings() {}

    @Override
    public void setAttribute(
            String name, Object value)
    {
        switch (name)
        {
            case "theme" ->
                this.theme = (String) value;
            case "fontSize" ->
                this.fontSize = (Integer) value;
        }
    }

    public String getTheme() { return theme; }
    public int getFontSize() { return fontSize; }
}

// Register before loading any resource bundles
// that reference this package
AttributeCollectionResourceBundle.registerAttributeCollectionPackage("com.example");
```

## Core Concepts

### Localizable and Resourceful

The `Localizable` interface provides `getBundleLocale()`,
`setBundleLocale()`, `getResourceBundle()`, and fires
`LocaleEvent`s when the object's locale changes. Use `LocalizableImpl`
or `LocalizationDelegate` as base implementations.

The `Resourceful` interface marks objects that receive their
localized resources from an external `Resource`. A `Resource`
encapsulates a `Localizable` source and a string key, providing
`getString()`, `getObject()`, and `getStringArray()` accessors.
This is useful for reusable components like buttons and labels
that appear in many contexts and cannot provide their own
localized resources.

Some objects may be both Localizable and Resourceful. For
example, a menu that manages its own locale but also needs
external resources for its label.

### NestedResourceBundle

A `NestedResourceBundle` wraps a standard `ResourceBundle`
(the *delegate*) and links to an optional *superBundle*
representing the next level up in a nesting hierarchy. When
a key is looked up:

1. The delegate is searched first.
2. If not found, the locale fallback parent chain is walked,
   searching each parent's delegate.
3. If still not found, the lookup continues into the
   superBundle, repeating from step 1 at the next level.

This is a general-purpose nesting mechanism. It does not
dictate what the hierarchy represents; it simply provides
a multi-level fallback search across linked ResourceBundles.

### Polymorphic Resource Inheritance

`LocalizationDelegate` uses `NestedResourceBundle` to build
a resource hierarchy that mirrors the class hierarchy. When
`getNestedResourceBundle()` is called, it walks from the
concrete class up through each superclass that implements
`Localizable`, loads a `ResourceBundle` for each class (via
the registered `GetResourceBundleCallback`), and links them
together as nested levels. The most derived class is at the
top; the base class is at the bottom.

For class `Foo extends Bar`, the resulting structure looks
like this:

```
FooBundle_fr_CA  ->  FooBundle_fr  ->  FooBundle
       |                                   |
BarBundle_fr_CA  ->  BarBundle_fr  ->  BarBundle
```

Horizontal arrows represent locale fallback (the standard
`ResourceBundle` parent chain). Vertical arrows represent
the superBundle link between nesting levels. A key lookup
starts at `FooBundle_fr_CA`, walks across to `FooBundle`,
then drops down to `BarBundle_fr_CA` and walks across to
`BarBundle`.

This means subclasses inherit all parent resources by
default and can override any of them simply by defining the
same key in their own bundle.

### Locale Change Events

```java
public class MyListener implements LocaleEventListener
{
    public void processLocaleEvent(LocaleEvent event)
    {
        // Update UI, refresh data, etc.
    }
}

MyListener listener = new MyListener();

myLocalizable.addLocaleEventListener(listener);

// fires event and invokes MyListener.processLocaleEvent()
myLocalizable.setBundleLocale(Locale.JAPANESE);
```

### Module System Integration

For non-modular applications, the AssociativeResourceBundleControl
class provides support for loading ResourceBundles from JSON and XML
files (as well as java class and .properties files). The
AssociativeResourceBundleControlProvider class implements the
ResourceBundleControlProvider interface and is referenced in the
META-INF/services/java.util.spi.ResourceBundleControlProvider file so
that this behavior is the default for non-modular applications.

JPMS applications will use the AssociativeResourceBundleProvider class,
which extends the JDK's AbstractResourceBundleProvider class. The
following steps are required in order to make a ResourceBundle
(say org/example/MyClassBundle.json - a ResourceBundle associated
with the org.example.MyClass class) available in a JPMS application:

**1.** Declare that the module uses and provides an spi interface for the bundle:

In module-info.java for the JPMS application:

```java
uses org.example.spi.MyClassProvider;
provides org.example.spi.MyClassProvider with dev.javai18n.core.test.spi.ModuleProviderImpl;
```

**2.** Provide an interface for the provider spi:

In org/example/spi/MyClassProvider.java:

```java
import java.util.spi.ResourceBundleProvider;

/**
 * The service provider interface for the MyClass bundle.
 */
public interface MyClassProvider extends ResourceBundleProvider {}
```

**3.** Provide an implementation for the provider spi, extending AssociativeResourceBundleProvider:

In org/example/spi/ModuleProviderImpl.java:

```java
import dev.javai18n.core.AssociativeResourceBundleProvider;

/**
 * An AssociativeResourceBundleProvider that implements the ResourceBundleProvider interfaces defined in this module.
 */
public class ModuleProviderImpl extends AssociativeResourceBundleProvider
    implements MyClassProvider {}
```

NOTE: Additional spi implementations in the package can be added to the 'implements' clause,
rather than creating an implementation for each interface.

**4.** Create a file named org.example.spi.MyClassProvider in META-INF/services with contents:

```java
dev.javai18n.core.test.spi.ModuleProviderImpl
```

NOTE: Additional spi implementations will each require their own file, all will have the same content.

The AssociativeResourceBundleProvider and AssociativeResourceBundleControl both make use of the
AssociativeResourceBundleLocator class to locate and load ResourceBundles.

In a standard JPMS application, ResourceBundle lookups are restricted to the resources
deployed with the module. Since this framework supports polymorphic resource inheritance
and classes may extend classes that are defined in a different module, each module must register a
`GetResourceBundleCallback` so the library can load bundles from the correct module context:

```java
module my.app
{
    requires dev.javai18n.core;
}

// In a static initializer or module bootstrap:
GetResourceBundleRegistrar.registerGetResourceBundleCallback(callback);
```
Where callback implements the GetResourceBundleCallback interface. Note that only one callback
object may be defined per module.

Editorial comment: The ResourceBundleProvider spi doesn't strike one as elegant design, given
                   the deployment steps required to use it...

## API Reference

| Class / Interface | Description |
|---|---|
| `Localizable` | Interface for locale-aware objects |
| `Localizable.LocaleEvent` | Event fired when a `Localizable` object changes its locale |
| `Localizable.LocaleEventListener` | Listener interface for `LocaleEvent`s |
| `LocalizableImpl` | Base class implementing `Localizable` |
| `LocalizableLogger` | A `System.Logger` that is `Localizable` |
| `LocalizationDelegate` | Delegation helper for bundles and polymorphic inheritance support |
| `Resourceful` | Interface for objects with a `Resource` |
| `Resource` | Encapsulates source and key for lookup |
| `NestedResourceBundle` | ResourceBundle hierarchy support |
| `JsonResourceBundle` | Bundle loaded from JSON |
| `XMLResourceBundle` | Bundle loaded from XML |
| `AttributeCollection` | Interface for typed objects from JSON/XML entries |
| `AttributeCollectionResourceBundle` | Base for JSON/XML bundles |
| `AssociativeResourceBundleLocator` | Multi-format bundle locator |
| `AssociativeResourceBundleControl` | `ResourceBundle.Control` using `AssociateResourceBundleLocator` |
| `AssociativeResourceBundleControlProvider` | `ResourceBundleControlProvider` using `AssociativeResourceBundleControl` |
| `AssociativeResourceBundleProvider` | A `ResourceBundleProvider` implementation for modular environments |
| `GetResourceBundleCallback` | Interface for cross-module bundle loading |
| `GetResourceBundleRegistrar` | Registry for module callbacks |
| `ModuleResourceBundleCallback` | Default `GetResourceBundleCallback` implementation |
| `ResourceStreamLoader` | Helper for loading resources via Modules or ClassLoaders |
| `NoCallbackRegisteredForModuleException` | An exception generated when no `ResourceBundle.getBundle()` callback has been registered for a module |

## Building

```bash
mvn clean package
```

To build with sources JAR, javadoc JAR, and GPG signing for
release:

```bash
mvn -Prelease clean package
```

## Testing

To execute unit tests under JPMS:

```bash
mvn clean test -Ptest-modulepath
```

To execute unit test on the classpath:

```bash
mvn clean test
```

You will see some messages on std out that confirm whether the tests are running under JPMS or the classpath:

```bash
[INFO]      [exec] === MODULE DEBUG INFO ===
[INFO]      [exec] Module isNamed: true
[INFO]      [exec] Module name: dev.javai18n.core.test
[INFO]      [exec] Module descriptor: module ...
[INFO]      [exec] =========================
[INFO]      [exec] jdk.module.path:...
[INFO]      [exec] java.class.path:
[INFO]      [exec] =========================
[INFO]      [exec]  Running in MODULE mode
[INFO]      [exec]  Module name: dev.javai18n.core.test
```

## License

This project is licensed under the
[Apache License, Version 2.0](LICENSE).
