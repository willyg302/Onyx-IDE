# Onyx IDE

Authors: William Gaul  
Version: 1.0

---

## About

[Onyx IDE](http://willyg302.wordpress.com/onyx-ide/) is an IDE I created for use with UnrealScript and UDK. Because it is built with pure Java, it can run on any machine that has a JVM installed -- this means Windows AND Macs (not to mention probably Linux too, although I've never tested it). Although UDK-specific functions are only supported on Windows, Onyx IDE allows developers to continue coding in a familiar environment even when they're on a different platform.

## Open Source?

Because I have no more time to maintain Onyx IDE, I have decided to release its sources for the UDK community to study and improve. Initially all I wanted was an IDE I felt comfortable with for personal use, but if anyone out there wants to make it better, they now have the chance.

## Disclaimer

__You can download, modify, and redistribute this code in any way you see fit, as long as:__
- __you give proper attribution, either by saying "code by William Gaul" or linking to my blog or something like that__
- __you retain any attributions to third parties that are already in the code__
- __you do not sell this code or otherwise profit directly from it__
- __you inform everyone that you distribute this code to about the above clauses__

I am also not responsible if your computer explodes, or anything like that :)

## Packages

- [UnrealEditor](https://github.com/willyg302/Onyx-IDE/tree/master/UnrealEditor): contains a snippet of the NetBeans source folder, including all sources, libraries, assets, and build files
- [Distributables](https://github.com/willyg302/Onyx-IDE/tree/master/Distributables): contains all documentation and files necessary for distributing Onyx

## More Info

Onyx IDE is built with Java 7.0 in NetBeans. The sources should compile pretty much as-is, but to compile the platform specific versions you will need the following:
- __Windows__: launch4j (or a similar program of your choice)
- __Mac__: JarBundler (this one is included as a step in the ANT build process, so if you have it already it will automatically compile a Mac application)

You can recompile a version of `UScriptTokenMaker.java` by using Flex and the `uscript.xml` file (this part is hard, so don't feel bad if it doesn't work).

## Credits

- __Robert Futrell__: RSyntaxTextArea and AutoComplete libraries
- __Michael Hagen__: Beautiful JTattoo Swing themes and various icons
- __David Ekholm__: JTextPad structure, used for core components
- __Girish Chavan__: Embedded searchable JTree
- __JAlbum__: Portions of core code

This project was pretty much hacked together from 5000 different sources (mostly Stack Overflow). If I missed anyone, please let me know and I will gladly include them.