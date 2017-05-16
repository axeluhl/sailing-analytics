package com.sap.sailing.domain.abstractlog.race;

import java.io.Serializable;

public interface RaceLogCourseAreaChangedEvent extends RaceLogEvent {

    Serializable getCourseAreaId();

}
