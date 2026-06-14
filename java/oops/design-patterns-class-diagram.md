# Design Patterns Class Diagram

## Observer Pattern
- defines a one-to-many dependency between objects so that when the object changes state, all of its dependents are notified and updated automatically.

```mermaid
---
title: Observer Pattern
---
classDiagram
    Subject <|-- ConcreteSubject
    Observer <|-- ConcreteObserver
    Subject --> Observer
    
    class Subject {
        <<interface>>
        + registerObserver(Observer o)
        + removeObserver(Observer o)
        + notifyObservers()
    }
    
    class ConcreteSubject {
        + registerObserver(Observer o)
        + removeObserver(Observer o)
        + notifyObservers()
        
        + getState()
        + setState()
    }
    
    class Observer {
        <<interface>>
        + update()
    }
    
    class ConcreteObserver {
        + update()
        // other methods()
    }
```

## Decorator Pattern
- attach additional responsibilities to an object dynamically. Decorators provide a flexible alternative to subclassing for extending functionalities.

```mermaid
---
title: Decorator Pattern
---
classDiagram
    Component <|-- ConcreteComponent
    Component <|-- Decorator
    Decorator <|-- ConcreteDecoratorA
    Decorator <|-- ConcreteDecoratorB
    
    class Component {
        <<interface>>
        + methodA()
        + methodB()
    }
    
    class ConcreteComponent {
        + methodA()
        + methodB()
    }
    
    class Decorator {
        <<abstract>>
        Component component
        + methodA()
        + methodB()
    }

    class ConcreteDecoratorA {
        + methodA()
        + methodB()
    }
    
    class ConcreteDecoratorB {
        + methodA()
        + methodB()
    }
    
```

## Factory Method
- defines an interface for creating an object, but let subclass decide which class to instantiate. Factory method lets a class defer instantiation to the subclasses.

```mermaid
classDiagram
    Creator <|-- ConcreteCreator
    Product <|-- ConcreteProduct
    ConcreteCreator --> ConcreteProduct
    
    class Creator {
        <<abstract>>
        factoryMethod()
        operation()
    }
    
    class ConcreteCreator {
        factoryMethod()
    }
    
    class Product {
        <<interface>>
    }
    
    class ConcreteProduct {
        
    }
```

## Factory Method
- provides an interface for creating families of related or dependent objects without specifying their concrete classes

```mermaid
classDiagram
    AbstractFactory <|-- ConcreteFactory1
    AbstractFactory <|-- ConcreteFactory2
    
    ConcreteFactory1 --> ProductA1
    ConcreteFactory1 --> ProductB1
    
    ConcreteFactory2 --> ProductB1
    ConcreteFactory2 --> ProductB2
    
    AbstractProductA <|-- ProductA1
    AbstractProductA <|-- ProductA2
    AbstractProductB <|-- ProductB1
    AbstractProductB <|-- ProductB2
    
    class AbstractFactory {
        <<interface>>
        createProductA()
        createProductB()
    }
    
    class ConcreteFactory1 {
        createProductA()
        createProductB()
    }

    class ConcreteFactory2 {
        createProductA()
        createProductB()
    }
    
    class AbstractProductA {
        <<interface>>
    }
    
    class AbstractProductB {
        <<interface>>
    }
    
    class ProductA1 {
        
    }
    
    class ProductB1 {
        
    }
    
    class ProductA2 {
        
    }
    
    class ProductB2 {
        
    }
```

## Singleton Pattern
- ensures that a class has only one instance, and provides a global point of access to it.

```mermaid
classDiagram
    class Singleton {
        static uniqueInstance
        
        static getInstance()
    }
```