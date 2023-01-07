# Powerlifting-meet-tracker
A GUI application used to conduct a powerlifting meet by tracking lift attempts, categorizing competitors and displaying unique judging utilities.

## Overview
Using concepts learned throughout my first semester in computer science (CSE205), I built this application using the JavaFX library.

To run this program, simply install the Java Runtime Environment version 8.0 (for compatibility with JavaFX) and compile each of the files included in the src folder. Then, execute the App.java file.

This application allows users to create lifter profiles via a dialog box which may then be tracked and updated through the GUI.

Some unique features of the program include a responsive timer, to time athletes on the platform, as well as a plate loading diagram in order to simplify the process of efficiently loading a powerlifting bar. The system allows users to seamlessly switch between mass metrics (KGs and LBs) in order to accomodate a variety of equipment and for ease of use. Within the judging tab, an individual's lifts may be updated and projections can be made for upcoming lifts. 

By entering the Standings tab, the focus shifts from the individual lifter to the competition as a whole, allowing the judges and audience members to see a direct comparison of the lifters in a specified weight class and age division. Various methods of comparison can be used to sort lifters by useful statistics and display their overall rankings. Finally, a convenient meet report can be compiled and written to a text file for later use. 

## Upcoming additions

The next feature that I am working on is a means of serializing the athlete roster to achieve object persistence with lifter objects.


## Takeaways

As I completed this project, I utilized a UML class diagram to organize my thoughts and design a functional system. As I built each section of the program, I had the opportunity to explore many new libaries and understand classes including Event Handlers, Change Listeners, bindings and constraints. As always, I practiced writing reusable code and emphasized concepts of object oriented programming such as inheritance, encapsulation and aggregation.

I made frequent use of inner classes to further organize my code, a structure that had not previously understood but will continue to utilize.
