# Creation of the Flags

In this folder is a script createFlags.sh that can (automatically) create most flags contained in the webfrontend
<br>
All flags that cannot be created are named in such a matter, that they should not create any conflicts when copying a new version of the generated files into that folder.

## Useage
The flags can all be obtained using the FlagImageResolver <br>
They are stored in the folder ./git/java/com.sap.sailing.gwt.ui/src/main/resources/com/sap/sailing/gwt/ui/client/images/flags/

After cloneing the git, it is necessary to copy the overlays provided into the overlay folder. 
This will replace the shiny overlays for 64bit with a 1px black border overlay

##Execution

Executing the script with the sh interpreter should <br>
1. download https://github.com/gosquared/flags.git to /tmp
2. cropping the whitespace around all png files in flags-iso/flat/64 
3. running pngcrush with -brute (tries all possible compressions png supports, chooses the smallest result) and -rem alla (removes all ) mode

The results are in /tmp/flags/out/ if the script executed without issues.

## Requirements
sh/bash <br>
git <br>
pngcrush <br>
imagemagick <br>
