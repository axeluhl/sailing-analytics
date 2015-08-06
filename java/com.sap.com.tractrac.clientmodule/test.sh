#!/bin/bash
rm test.jar
rm -fr bin/*
mkdir bin
javac -cp src:lib/* -d bin src/com/tractrac/subscription/app/tracapi/*.java
cd bin
jar -cvfm ../test.jar ../Manifest.txt com/tractrac/subscription/app/tracapi/*.class
cd ..
java -jar test.jar "http://germanmaster.traclive.dk/events/event_20120928_Lidingpet/clientparams.php?event=event_20120928_Lidingpet&race=beed03b4-0a3a-11e2-a8d1-10bf48d758ce&ci=&minimize="

