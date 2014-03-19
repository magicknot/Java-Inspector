Inspector for Java
==================

Implementation of an *inspector* of Java objects. The inspector can be started from any point of a Java program, accepting as argument an object that should be inspected.

The inspector must be started using the following form:
```java
new ist.meic.pa.Inspector().inspect(object)
```

This means that the class `ist.meic.pa.Inspector` has a construtor that does not
accept arguments. The class also provides a method with the following signature: ` public void inspect(Object object); `

As a result of calling the previous method, the inspector then presents all the relevant features of the object, namely:
 * The class of the object.
 * The name, type, and current value of each of the fields of the object.
 * Other features that you feel are important.

The inspector then enters a read-eval-print loop where it accepts commands from the user, executes the corresponding actions and prints the results. All interactions (both printing of the object features and reading of inspection commands) are done on `System.err`.

Important notes
--------------

Our code runs on **Java 6**.


Important dates
--------------

 * Code & Slides - **19:00 of March, 28**.
