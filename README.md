# ParcelGen

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-ParcelGen-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/2644)

This project combines Java Annotation Processing & AST modification to generate `Parcelable` boilerplate code before compilation. The main aim of this project was to explore undocumented Java AST modification.

This library does not bring any runtimes to your project - all code is generated before compilation. Changes occur only in annotated object.

## Setup
Add dependencies to you project:
```gradle
compile 'ru.noties:parcel-gen:1.1.0'
apt 'ru.noties:parcel-compiler:1.1.0'
```
Annotate a class that should be `Parcelable` with `@ru.noties.parcelable.ParcelGen`.

### Supported types
* byte
* int
* long
* float
* double
* boolean
* String
* Enum (**no array support**)
* Serializable (**no array support**)
* Parcelable (**with** array & List or ArrayList support)
* CharSequence
* List & ArrayList

### Changelog v1.1.0
* Added calls to super if parent is annotated with `@PacelGen`
* Added more supported types (CharSequence, Lists)

### Drawbacks
As long as IDE would not know that an object will *magically* become a `Parcelable` after compilation it will not let you use it as it's one. There are two possibilities:
* Simply cast to `Parcelable` when it's needed (`Bundle.putParcelable("key", (Parcelable) myAnnotatedWithParcelGenObject);`
* Implement `Parcelable` in your object, add valid `describeContents` & `writeToParcel(Parcel, int)` methods & leave them there. ParcelGen compiler will replace them with fully functional ones.

### How to improve library
* Add support for arrays of Enum & Serializable ?
* Add more of supported by `Parcel` types ?
* Call super.writeToParcel() & from constructor ?

### Sources of inspiration
* http://habrahabr.ru/company/e-Legion/blog/206208/ (in Russian)
* http://scg.unibe.ch/archive/projects/Erni08b.pdf

## License

```
  Copyright 2015 Dimitry Ivanov (mail@dimitryivanov.ru)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
```