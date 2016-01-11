# Fortify Security Scans

During the SAP-internal "I2M" (Idea-to-Market) process all software that will be shipped needs to undergo security validation. A point of contact for security validation is Frank Koehntopp (frank.koehntopp@sap.com). An essential step for security validation is a scan report created using the HP Fortify tool suite.

To perform a scan of the Android apps, install the Fortify SCA Eclipse plugin. [https://fortify1.wdf.sap.corp](https://fortify1.wdf.sap.corp) is the link to the Fortify Software Security Center. Installation and configuration is explained here: 
[https://jam4.sapjam.com/wiki/show/e3aMSS4m2c078KSre2WfTn#pluginInstallation](https://jam4.sapjam.com/wiki/show/e3aMSS4m2c078KSre2WfTn#pluginInstallation)

Then, open a Java file from the workspace in Eclipse (yes, Fortify is very sensitive to what is currently selected and opened, and the "HP Fortify" menu shows very different options based on this context which can be highly confusing). Then select "Advanced Scan..." from the "HP Fortify" menu. In the dialog, exclude all bin/, build/, gen/, target/ and tests-* folders in the app projects. Those folder icons should change from yellow to white color, indicating the exclusion rule. Then from the tree select the apps and the ``com.sap.sailing.android.shared`` folders, choose JDK 1.7 from the drop-down and click "Next." In case of repeated scans, select "Merge with previous scans."

On the next page, say "No" to "Is this a J2EE Web application?" and select "No" for "Does this program run with escalated privileges...?" and click the "Scan" button.

Be patient, the scan of the apps can last an hour or more. Progress indication is unreliable. When done, the Fortify plug-in will change to the Fortify perspective and show the scan results. Those can be uploaded to the Fortify Software Security Center and may be stored locally as a ``.fpr`` file.

The project in the Fortify Software Security Center should use the "SAP 3.0" template so that the categories match with the Fortify Eclipse plugin's view. We need to audit all findings in the "Corporate Security Requirements" category, all from "Audit All" and one from each category in the "Spot Checks" tab.

When done, a report of type "SAP Q-Gate Report" can be generated in the Fortify Software Security Center which, when "green," will be the basis for security validation.