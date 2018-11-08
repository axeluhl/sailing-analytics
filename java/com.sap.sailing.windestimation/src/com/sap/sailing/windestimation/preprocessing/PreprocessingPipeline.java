package com.sap.sailing.windestimation.preprocessing;

public interface PreprocessingPipeline<From, To> {

    To preprocessRace(From element);

}
