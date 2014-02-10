********************************************
********* TracAPI 3.0.0-SNAPSHOT ***********
********************************************

This is an SNAPSHOT version that means that is a version that has not been released (is under development).

1) Content

This zip package contains 2 folders:

 - lib -> contains the Trac-API compiled library
 - src -> contains some code examples.
 
The documentation can be retrieved online from http://tracdev.dk/maven-sites-clients/3.0.0-SNAPSHOT/maven-java-parent/.

It contains also some files:

 - test.sh -> script that compiles the code in the src folder, creates the test.jar library and execute the code of the example.
 - Manifest.txt -> manifest used to create the test.jar file

- Fixing a bug reading MTB files. In some scenarios when the positions of one boat arrive with a big delay, 
they can be discarded by the library. The positions are in the MTB file and it is not necessary to 
regenerate the files again. It is just a bug in the reading process. 


********************************************
******* TracTracClientModule 2.0.11 ********
********************************************

This release only contains changes in its implementation (it keeps the previous API). 

1) New features
 

