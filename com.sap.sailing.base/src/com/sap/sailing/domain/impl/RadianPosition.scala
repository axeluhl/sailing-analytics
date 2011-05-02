package com.sap.sailing.domain

import scala.math.Pi

class RadianPosition(_latRad:Double, _lngRad:Double) extends Position {
  override def latRad = _latRad
  override def lngRad = _lngRad
}