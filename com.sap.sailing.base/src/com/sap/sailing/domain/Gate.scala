package com.sap.sailing.domain

trait Gate extends Waypoint {
	def left:Buoy
	def right:Buoy
}