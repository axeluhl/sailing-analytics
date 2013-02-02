package com.sap.sailing.domain.racelog;

import java.io.Serializable;

public interface RaceLogCourseAreaChangedEvent extends RaceLogEvent {
	
	Serializable getCourseAreaId();

}
