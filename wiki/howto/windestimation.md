# Training of internal Wind Estimation models

This document describes the generation process of Machine Learning (ML) models which are used internally by wind estimation. It is highly recommended to proceed this howto step by step considering the order of sections.

## Overview
In total, there are the following three categories of ML models used by wind estimation:
1. **Maneuver Classifiers**
2. **Regressors** of TWD delta standard deviation for the dimension **duration**
3. **Regressors** of TWD delta standard deviation for the dimension **distance**

Each of the model categories are composed of multiple models where each model targets a specific context. A context for a maneuver classifier is determined by the following attributes:
* Maneuver features
	* Polar features enabled: yes/no
	* Mark features enabled: yes/no
	* Scaled speed features enabled: yes/no
* Boat class filtering for the data on which the classifier is trained, such as a specific boat class, or with all boat classes included

The context of regressor models is represented by its assigned input interval responsibility, e.g. [0 seconds; 62 seconds) for duration, or [80 meters; 1368 meters) for distance.

Each of the ML model categories must be trained individually. The common workflow looks as follows:
1. Get the training data from REST API of sapsailing.com
2. Preprocess data
3. Train the model category

For each of the steps, appropriate Java classes must be executed per *Run with...->Java Application*. All referenced classes are located in *com.sap.sailing.windestimation.lab* Java project. Each class execution must finish without uncaught exceptions before proceeding to next instructions. After model training, all trained models can be collected in *./trained_wind_estimation_models*, which is normally */path/to/workspace/com.sap.sailing.windestimation/trained_wind_estimation_models* if you start the training classes in Eclipse per *Run with...->Java Application*.

The details of the training process for each model category are described in the following sections.

## Prerequisites
To complete the training process successfully, you need to make sure that you have the following stuff:
* A complete onboarding setup for SAP Sailing Analytics development
* MongoDB (**3.4 or higher!**) is up and running (same MongoDB instance as required in onboarding howto)
* At least 100 GB free space on the partition, where MongoDB is operating
* Installed graphical MongoDB client such as MongoDB Compass (Community version)

## Get the training data from sapsailing.com
The following steps import all the data required from sapsailing.com into the local MongoDB. These steps constitute a preprequisite for training of all ML model categories:
1. Run *com.sap.sailing.windestimation.data.importer.ManeuverAndWindImporter*
2. Run *com.sap.sailing.windestimation.data.importer.PolarDataImporter*

## Maneuver classifiers training
1. Run *com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifierTrainer*. Within the this step, the maneuver data is preprocessed and all maneuver classifiers are trained for each supported context
2. Optionally run *com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifierScoring* to print the performance of the trained classifiers and to verify maneuver classification scoring

## Duration-based TWD delta standard deviation regressor

1. Run *com.sap.sailing.windestimation.data.importer.DurationBasedTwdTransitionImporter*
2. Run *com.sap.sailing.windestimation.data.importer.AggregatedDurationBasedTwdTransitionImporter*
3. Run *com.sap.sailing.windestimation.datavisualization.AggregatedDurationDimensionPlot* to visualize the wind data. A Swing-based GUI-Window must open with two charts, one XY-chart where the x-axis represents **seconds**, and the y-axis represents TWD delta-based series measures (e.g. standard deviation or mean). Below the chart, a histogram for data points of the XY-Chart is provided. You can zoom-in and zoom-out in each of the chart by mouse dragging. Be aware that currently the zoom level of both charts is not synchronized
4. Open your graphical MongoDB client and connect to *windEstimation* database hosted by your local MongoDB. Open the collection with name *aggregatedDurationTwdTransition*. Within the collection you will see all the instances/data points visualized in the previous step. The total number of the points must not exceed 100.
5. Delete all the instances within the collection which do not make sense. For this, use the data visualization tool from step 3 to identify such instances. Pay a special attention to the instances in the beginnning and end. Some of the instances are not representative due to small number of supporting instances which is visualized in the histogram. Restart the data visualization tool as often as need to visualize the changed data.
6. Open the source code of the class *com.sap.sailing.windestimation.model.regressor.twdtransition.DurationBasedTwdTransitionRegressorModelMetadata*. It is recommended to read JavaDoc of the class. Scroll down to the definition of the inner class/enum *DurationValueRange*. The enum defines the intervals for which a separate regressor model will be trained. Adjust the intervals accordingly in order to allow the regressor model to learn the data curve with minimal error. Make sure that there are at least 2 data points available within each interval
7. Run *com.sap.sailing.windestimation.model.regressor.twdtransition.DurationBasedTwdTransitionStdRegressorTrainer*
8. Verify the trained regressor functions. They are printed in the console output of the previous step. For instance, you can visualize the polynoms by means of https://www.wolframalpha.com/

## Distance-based TWD delta standard deviation regressor

The steps of this sections are similar to the steps of the previous section. It is recommended to traverse through the previous section before starting with this one, because due to similarity of the steps, the similar steps in this section are described with less details and hints.

1. Run *com.sap.sailing.windestimation.data.importer.DistanceBasedTwdTransitionImporter*
2. Run *com.sap.sailing.windestimation.data.importer.AggregatedDistanceBasedTwdTransitionImporter* with at least 10 GB JVM memory.
3. Run *com.sap.sailing.windestimation.datavisualization.AggregatedDistanceDimensionPlot* to visualize the wind data. Here, the x-axis of the XY-chart represents **meters**
4. Open your graphical MongoDB client and connect to *windEstimation* database hosted by your local MongoDB. Open collection *aggregatedDistanceTwdTransition* collection. Within the collection you will see all the instances/data points visualized in the previous step. The total number of the points must not exceed 100.
5. Delete all the instances within the collection which do not make sense.
6. Open the source code of the class *com.sap.sailing.windestimation.model.regressor.twdtransition.DistanceBasedTwdTransitionRegressorModelMetadata*. Scroll down to the definition of the inner class/enum *DistanceValueRange*. The enum defines the intervals for which a separate regressor model will be trained. Adjust the intervals accordingly in order to allow the regressor model to learn the data curve with minimal error.
7. Open the source code of the class *com.sap.sailing.windestimation.model.regressor.twdtransition.DistanceBasedTwdTransitionStdRegressorTrainer* and scroll down to *getTrainingDataForDistance()* method. The method returns the training datasets which will be used for the model training. Adjust the datasets accordingly so that at least two datasets intersect with [fromInclusive; toExclusive] of each specified interval in the previous step
7. Run *com.sap.sailing.windestimation.model.regressor.twdtransition.DistanceBasedTwdTransitionStdRegressorTrainer*
8. Verify the trained regressor functions. They are printed in the console output of previous step