# Java (De)Serialization and Circular Dependencies
## Context

When using java serialization one can define custom initialization behavior during deserialization using the readObject() method as described here: [Java Documentation](http://docs.oracle.com/javase/7/docs/api/java/io/ObjectInputStream.html)

The specs make you believe that the reference-graph of the underlying object is fully constructed after calling ois.defaultReadObject(). In most cases this is a correct assumption. There are exceptions and this wiki page should help identify these situations and find a solution for them.

## Scenario

We serialize an object with several references. The object graph contains instances of class A and instances of class B. Class A references Class B and Class B references Class A. These references may be indirect over other classes (e.g. B -> X -> Y -> A)

We implemented readObject in class A. In the method we access members of instance of B that A references. Our code needs these members to be initialized.

## Problem

If the structure in our object graph allows the deserializer to read the B object before the A object it will call the readObject method of A while other references of B are not yet available thus making the custom readObject code fail.

## Solution

If the cyclic dependency can be removed, it should be removed and the problem is solved. Most of the times this is not as easy as it sounds.

The deserializer needs to "meet" object A before meeting Object B. This can be forced by serializing and deserializing a collection of the instances of A before everything else.