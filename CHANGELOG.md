# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added

- `Localizable` and `Resourceful` interfaces for marking locale-sensitive
  classes and resource-dependent objects
- `LocalizationDelegate` for polymorphic resource bundle resolution that
  mirrors class inheritance
- `JsonResourceBundle` for loading typed resources from JSON files
- `XMLResourceBundle` for loading typed resources from XML files
- `NestedResourceBundle` for composing resource bundles with parent fallback
- `AttributeCollectionResourceBundle` for serving `AttributeCollection`
  objects from resource bundles
- `LocalizableLogger` for logging with localized messages
- `AssociativeResourceBundleControl` and `AssociativeResourceBundleProvider`
  for classpath and module-path resource bundle loading
- `GetResourceBundleCallback` event mechanism for dynamic locale selection
- Classpath and module-path test profiles
- Release profile with javadoc and source jar generation
