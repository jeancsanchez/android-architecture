# TODO-MVP-NAVIGATOR

Project contributors: [Nikita Kozlov](https://github.com/NikitaKozlov)

### Summary

This sample is the base for many of the variants. It showcases a simple
implementation of the Model-View-Presenter pattern with Navigator. 
It uses manual dependency injection to provide a repository with
local and remote data sources. Asynchronous tasks are handled with callbacks.

In comparison to the **TODO-MVP** example, all "navigation" methods are 
removed from the view to reach better separation of concerns. 

Note: in a MVP context, the term "view" is overloaded:

  * The class android.view.View will be referred to as "Android View"
  * The view that receives commands from a presenter in MVP, will be simply called
"view".

### Fragments

It uses fragments for two reasons:

  * The separation between Activity and Fragment fits nicely with this
implementation of MVP: the Activity is the overall controller that creates and
connects presenters with views and navigators.
  * Tablet layout or screens with multiple views take advantage of the Fragments
framework.

### Key concepts

There are four features in the app:

  * <code>Tasks</code>
  * <code>TaskDetail</code>
  * <code>AddEditTask</code>
  * <code>Statistics</code>

Each feature has:

  * A contract defining the view and the presenter
  * An Activity which is responsible for the creation of fragments and presenters
  * A Fragment which implements the view interface. 
  * A presenter which implements the presenter interface


Presenter contains business logic. View converts the presenter's commands to 
UI actions and listens to user actions, which are passed to the presenter. 
Navigator responsible for navigating to another screen.

Contracts are interfaces used to define the connection between presenters,
views and navigators.

### Dependencies

  * Common Android support libraries (<code>com.android.support.\*)</code>
  * Android Testing Support Library (Espresso, AndroidJUnitRunner…)
  * Mockito
  * Guava (null checking)

## Features

### Complexity - understandability

#### Use of architectural frameworks/libraries/tools: 

None 

#### Conceptual complexity 

Low, concept of Navigator is very simple so it adds nothing to the
complexity of MVP implementation.

### Testability

#### Unit testing

High, presenters are unit tested as well as repositories and data sources.

#### UI testing

High, injection of fake modules allow for testing with fake data

### Maintainability

#### Ease of amending or adding a feature

High. 

#### Learning cost

Low. Features are easy to find and the responsibilities are clear. Developers
don't need to be familiar with any external dependency to work on the project.

