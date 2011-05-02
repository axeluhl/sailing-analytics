package com.sap.sailing.domain

trait TimePoint {
	def millis = nanos/1000000
	def nanos:Long
}