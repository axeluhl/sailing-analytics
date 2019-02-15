#!/bin/bash
# Call like this:    analyzeProduct.sh <my.product> <path-to-java/com.sap.sailing.selenium.test/pom.xml>
# Will patch the bundleStartLevel section in the pom.xml file so that all auto-start bundles
# configured in the product specification are also launched properly and with the correct start level
# for the Selenium tests.

# The product consists of the features and plugins it lists. Its <configuration> section overrides
# the startLevel and autoStart defaults (4 / false) for a subset of the bundle list obtained from
# the features and plugins lists. The features that our product consists of are partitioned into
# ".runtime" and non-".runtime" features. The ".runtime" features obtain their bundles from the
# target platform, whereas the non-".runtime" features take their features from the workspace.

# In order to derive the target/workspace bundles for a launch configuration, the bundles are
# extracted from the features. Those from the .runtime features are assigned to the target_bundles
# attribute, the others to the workspace_bundles attribute. Auto start and start level information
# will be set to default:default unless the bundle occurs in the <configuration> section of the
# .product definition in which case the start level and auto start values are copied from the
# .product's <configuration> section.

GIT_ROOT=`dirname $0`/../../..
echo "GIT ROOT: $GIT_ROOT"
features=$(cat "$1" | grep "feature id=" | sed -e 's/^.*feature id="\([^"]*\)".*$/\1/')
echo ' *** Features ***'
echo "$features"
feature_xml_files=$(for i in $features; do echo "${GIT_ROOT}/java/$i/feature.xml"; done)
echo "$feature_xml_files"
# Determine the bundles with a specific configuration in the .product file:
autostart_bundles=$(cat "$1" | grep "plugin id=.* startLevel=" | sed -e 's/^.* id="\(.*\)" autoStart="\(.*\)" startLevel="\([0-9-]*\)".*$/\1 \3 \2/')
echo ' *** Autostart Bundles ***'
echo "$autostart_bundles"
# These autostart bundles need to be updated accordingly in the .launch configuration
# so they have the correct startLevel and autostart settings. The autostart bundles
# may occur either in the target_bundles or the workspace_bundles attribute in the .launch
# configuration. All other entries in the .launch configuration are set to @default:default.
# Now collect the bundles from the feature.xml files, split by workspace vs. target:
bundleStartLevelSection=""
for feature in $features; do
    feature_xml_file=${GIT_ROOT}/java/$feature/feature.xml
    bundles_in_feature=$(cat "$feature_xml_file" | grep "id=\"" | grep -v "id=\"$feature\"" | sed -e 's/^.*id="\([^"]*\)".*$/\1/')
    echo ' *** Bundles in feature' $feature '***'
    echo "$bundles_in_feature"
    for bundle in $bundles_in_feature; do
        # find out whether we have a non-default configuration for the bundle
        echo "$autostart_bundles" | grep -v "^org.eclipse.osgi " | grep -q '^'${bundle}' [^ ]* true$'
        if [ "$?" == "0" ]; then
            # found a configuration for the bundle
            echo ' *** Found a non-default configuration for bundle '${bundle}' ***'
	     bundleStartLevelSection="${bundleStartLevelSection}
	        <bundle>
                  <id>${bundle}</id>
                  <level>$( echo "${autostart_bundles}" | grep "^${bundle} [0-9-]* true" | sed -e "s/^${bundle} \([0-9-]*\) true$/\1/" )</level>
                  <autoStart>true</autoStart>
                </bundle>"
        fi
    done 
done
export bundleStartLevelSection
newFileContents=$( cat "$2" | awk 'BEGIN {copy=1;}
/<bundleStartLevel>/ { copy=0; print "              <bundleStartLevel>" ENVIRON["bundleStartLevelSection"]; }
{ if (copy) print $0; }
/<\/bundleStartLevel>/ {copy=1; print $0; }' )
echo " *** Here go the new file contents ***"
echo "$newFileContents" >"$2"
