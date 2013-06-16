package com.sap.sailing.server.gateway.impl;

import com.sap.sailing.domain.common.racelog.utils.FlagAlphabetInterpretationTemplate;

public class FlagAlphabetInterpreter extends FlagAlphabetInterpretationTemplate {

    @Override
    protected String getTextForRunningRace() {
        return "Race is running";
    }

    @Override
    protected String getTextForRunningRaceWithEarlyStarter() {
        return "Race is running";
    }

    @Override
    protected String getTextForFinishingRace() {
        return "Race is finishing";
    }

    @Override
    protected String getTextForFinishedRace() {
        return "Race is finished";
    }

    @Override
    protected String getTextForScheduledRace() {
        return "Race is scheduled";
    }

    @Override
    protected String getTextForRaceInStartphase() {
        return "Race in start phase";
    }

    @Override
    protected String getTextForUnscheduledRace() {
        return "No start time announced yet";
    }

    @Override
    protected String getTextForFirstSubstituteFlag() {
        return "General recall";
    }

    @Override
    protected String getTextForAnsweringPennantFlag() {
        return "Start postponed";
    }

    @Override
    protected String getTextForAnsweringPennantWithAlphaFlags() {
        return "Start postponed - no more racing today";
    }

    @Override
    protected String getTextForAnsweringPennantWithHotelFlags() {
        return "Start postponed - further signals ashore";
    }

    @Override
    protected String getTextForNovemberFlag() {
        return "Race abandoned";
    }

    @Override
    protected String getTextForNovemberWithAlphaFlags() {
        return "Race abandoned - no more racing today";
    }

    @Override
    protected String getTextForNovemberWithHotelFlags() {
        return "Race abandoned - further signals ashore";
    }

}
