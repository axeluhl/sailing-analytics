package com.sap.sailing.domain

import scala.math.Pi

class DegreePosition(_latDeg:Double, _lngDeg:Double) extends Position {
  override def latDeg = _latDeg
  override def lngDeg = _lngDeg
}