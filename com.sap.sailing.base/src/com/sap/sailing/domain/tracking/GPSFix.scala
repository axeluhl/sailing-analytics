package com.sap.sailing.domain.tracking
import com.sap.sailing.domain.TimePoint
import com.sap.sailing.domain.Positioned

trait GPSFix extends Positioned {
	def timepoint:TimePoint
}