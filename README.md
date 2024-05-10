# Cradle
Android app for students finding a place to rent within GCH.

- Written in Java
- Database - Firebase Firestore

## App Features
- Create seller/buyer accounts
- Filter listings
- Post new listings
- Realtime online database syncing
- Call button for contacting owner
- Image uploading

## SDG #11 - Sustainable Cities and Communities

## Application of OOP Principles
### Encapsulation
- To preserve the integrity of data, encapsulation used to prevent a user from directly altering the data members or variables of a class.
  
In our model objects such as `ForRent`, `Apartment`, and `Bedspace`, we hide the properties by setting them to private then we expose public setter and getter methods.

### Inheritance
- Applied to different classes that share characteristics. It enables us to decrease code duplication and boost reusability.

Since the two main types used in Cradle (Apartment and Bedspace) have many things in common, we decided to write a parent class we call `ForRent` which contains all properties shared by `Apartment` and `Bedspace`. By having these two classes extend from `ForRent`, we avoided code duplication and increases the code reusability.

### Abstraction
- is the process of concealing unnecessary internal characteristics of an object. An object's structure and behavior can be kept separate and made easier to understand by abstracting the data associated with it.

Used mostly in the Cradle's database operations. Cloud database operations involve a lot of other technical processes such as authentication and request handling other than the actual data writing and reading. However, we mostly do not need to know each and every underlying process, hence they are abstracted. By only exposing public API functions, we can focus on how we want to use the database rather than needing to understand the internal workings of the database.

### Polymorphism
- enables the creation of many methods with the same name but different implementations. This facilitates the development of adaptable systems that are simpler to comprehend and manage.

The `Fragment` object in Android is a great example of this concept. Cradle has 'Fragments' for different screens. They are all technically `Fragment` objects with the same methods but they have different implementations.

## Suggestions
- Allow multiple images
- Add in-app chat feature
- Search bar
- Redesign UI (Dark mode)
- Google Maps integration
- Feedback/rate feature
