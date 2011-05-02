package com.sap.sailing.domain

trait Speed {
	def knots = metersPerSecond*1852/3600
	def metersPerSecond:Double
	def kilometersPerHour = metersPerSecond/3.6
}