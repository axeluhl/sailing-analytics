# Training of internal Wind Estimation models

This document describes the generation process of Machine Learning (ML) models which are used internally by wind estimation. It is highly recommended to proceed this howto step by step considering the order of sections. At the end of this howto, you will generate a file containing the representation of internal models used by ``com.sap.sailing.windestimation`` bundle. You can use this file to update the wind estimation models of a running server instance. If you are interested in a simpler tutorial with less execution steps thanks to automation, then check out [Simple Guide for training of internal Wind Estimation models](./windestimation.md)

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

For each of the steps, appropriate Java classes must be executed per *Run with...->Java Application*. All referenced classes are located in *com.sap.sailing.windestimation.lab* Java project. Each class execution must finish without termination due to uncaught exceptions before proceeding to next instruction. You can skip the training of a model category if you do not want to update the models of that category on server. After model training, a new file with serialized representation of internal wind estimation models should be created in ``./windEstimationModels.dat``, which is normally */path/to/workspace/com.sap.sailing.windestimation/trained_wind_estimation_models* if you start the training classes in Eclipse per *Run with...->Java Application*.

The details of the training process for each model category are described in the following sections.

## Prerequisites

To complete the training process successfully, you need to make sure that you have the following stuff:

* A complete onboarding setup for SAP Sailing Analytics development
* MongoDB (**3.4 or higher!**) is up and running
* At least 100 GB free space on the partition, where MongoDB is operating
* Installed graphical MongoDB client such as MongoDB Compass (Community version)

## Get the training data from sapsailing.com

The following steps import all the data required from sapsailing.com into the local MongoDB. These steps constitute a preprequisite for training of all ML model categories:

1. Run *com.sap.sailing.windestimation.data.importer.ManeuverAndWindImporter*
2. Run *com.sap.sailing.windestimation.data.importer.PolarDataImporter*

## Maneuver classifiers training

1. Run *com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifierTrainer*. Within this step, the maneuver data is preprocessed and all maneuver classifiers are trained for each supported context.
2. Optionally run *com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifierScoring* to print the performance of the trained classifiers. After this step, a list with macro-averaged F2-score of each trained classifier will be stored in *./maneuverClassifierScores.csv*

## Duration-based TWD delta standard deviation regressor

1. Run *com.sap.sailing.windestimation.data.importer.DurationBasedTwdTransitionImporter*
2. Run *com.sap.sailing.windestimation.data.importer.AggregatedDurationBasedTwdTransitionImporter*
3. Run *com.sap.sailing.windestimation.datavisualization.AggregatedDurationDimensionPlot* to visualize the wind data. A Swing-based GUI-Window must open containing two charts. The upper chart is an XY-chart where the x-axis represents **seconds**, and the y-axis represents TWD delta-based series measures (e.g. standard deviation or mean). Below the chart, a histogram for data points of the XY-Chart is provided. You can zoom-in and zoom-out in each of the charts by mouse dragging. Be aware that currently, the zoom level of both charts is not synchronizing.
   ![Screenshot of graphical wind data visualization tool for duration dimension](../images/windestimation/aggregatedDurationBasedTwdDeltaTransitionBeforeDataCleansing.jpg "Screenshot of duration-based TWD delta visualization tool before data cleansing")
4. Open your graphical MongoDB client and connect to ``windEstimation`` database hosted within your local MongoDB. Open the collection with name ``aggregatedDurationTwdTransition``. Within the collection, you will see all the instances/data points visualized in the previous step. The attribute used for the x-axis is ``value``. Its corresponding metrics plotted in y-axis are the other attributes. ``std`` represents standard deviation (``Sigma`` curve in XY-chart) and ``std0`` represents standard deviation with zero as mean value (``Zero mean sigma`` curve in XY-chart).
   ![Screenshot of MongoDB Compass with opened aggregatedDurationTwdTransition collection](../images/windestimation/mongoDbCompassWithOpenedAggregatedDurationTwdTransitionCollection.jpg "Screenshot of MongoDB Compass with opened aggregatedDurationTwdTransition collection")
5. Delete all the instances within the collection which do not make sense. For this, use the data visualization tool from step 3 to identify such instances. Some of the instances are not representative due to the small number of supporting instances which is visualized in the histogram. Such instances can produce unreasonable bumps in the XY-chart. The desired output of this step is that the curve ``Zero mean sigma`` looks smooth and always growing, e.g. as depicted below:
   ![Screenshot of graphical visualization tool of duration dimension after data cleansing](../images/windestimation/aggregatedDurationBasedTwdDeltaTransitionAfterDataCleansing.jpg "Screenshot of duration-based TWD delta visualization tool after data cleansing")
   Use the ``Refresh charts`` button as often as needed to update the charts with the modified data in MongoDB.
6. Open the source code of the class ``com.sap.sailing.windestimation.model.regressor.twdtransition.DurationBasedTwdTransitionRegressorModelContext`` and scroll down to the definition of the inner enum ``DurationValueRange``. The enum defines the intervals for which a separate regressor model will be trained. Read the Javadoc of ``DurationValueRange`` and adjust the intervals accordingly in order to allow the regressor model to learn the ``Zero mean sigma`` curve with minimal error. You can also configure the polynomial which will be used as regressor function. Make sure that there are at least 2 data points contained within each configured interval. The data point with x = 0, y = 0 will be created automatically within the model training procedure.
7. Run *com.sap.sailing.windestimation.model.regressor.twdtransition.DurationBasedTwdTransitionStdRegressorTrainer*
8. Verify the trained regressor functions. They are printed in the console output of the previous step. For instance, you can visualize the polynoms by means of https://www.wolframalpha.com/

## Distance-based TWD delta standard deviation regressor

The steps of this sections are similar to the steps of the previous section. It is recommended to traverse through the previous section before starting with this one, because due to similarity of the steps, the similar steps in this section are described with less details and hints.

1. Run *com.sap.sailing.windestimation.data.importer.DistanceBasedTwdTransitionImporter*
2. Run *com.sap.sailing.windestimation.data.importer.AggregatedDistanceBasedTwdTransitionImporter*
3. Run *com.sap.sailing.windestimation.datavisualization.AggregatedDistanceDimensionPlot* to visualize the wind data. Here, the x-axis of the XY-chart represents **meters**
4. Open your graphical MongoDB client and connect to *windEstimation* database hosted by your local MongoDB. Open the collection *aggregatedDistanceTwdTransition*. Within the collection you will see all the instances/data points visualized in the previous step.
5. Delete all the instances within the collection which do not make sense.
6. Open the source code of the class *com.sap.sailing.windestimation.model.regressor.twdtransition.DistanceBasedTwdTransitionRegressorModelContext*. Scroll down to the definition of the inner class/enum *DistanceValueRange*. The enum defines the intervals for which a separate regressor model will be trained. Adjust the intervals accordingly in order to allow the regressor model to learn the data curve with minimal error.
7. Run *com.sap.sailing.windestimation.model.regressor.twdtransition.DistanceBasedTwdTransitionStdRegressorTrainer*
8. Verify the trained regressor functions. They are printed in the console output of the previous step.

## Generate the models file

Within this section, all the trained models produced within previous sections are aggregated and stored in a single file.

1. Run ``com.sap.sailing.windestimation.model.ExportedModelsGenerator``. Wait until the model serialization finishes and the program terminates normally. A new file with serialized representation of internal wind estimation models should be located in ``./windEstimationModels.dat``. The absolute path of the file must be printed in the console output of the program. You can upload the file via HTTP POST to http://sapsailing.com/windestimation/api/windestimation_data (see ``com.sap.sailing.windestimation.jaxrs.api.WindEstimationDataResource``) to update the wind estimation of a server instance. If you changed the source files of ``DurationValueRange`` or ``DistanceValueRange``, then you will need to update ``com.sap.sailing.windestimation`` bundle of the server instance which is meant to receive the new wind estimation models.


## Model Evaluation

This step is optional. However, it is recommended, to evaluate the performance of wind estimation operating with the new models.

1. Run ``com.sap.sailing.windestimation.evaluation.WindEstimatorManeuverNumberDependentEvaluationRunner`` to evaluate the wind estimation with the new trained models. The evaluation score will be stored as CSV in ``./maneuverNumberDependentEvaluation.csv``.