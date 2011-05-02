package com.sap.sailing.domain

import scala.math._

trait Position {
  /**
   * Latitude in degrees; -90..+90
   */
  def latDeg = latRad/Pi*180

  /**
   * Longitude in degrees; -180..+180
   */
  def lngDeg = lngRad/Pi*180
  
  /**
   * Latitude in radians; -PI/2..+PI/2
   */
  def latRad : Double = latDeg/180*Pi
  
  /**
   * Longitude in radians; -PI..+PI
   */
  def lngRad : Double = lngDeg/180*Pi
  
  def centralAngleRad(p:Position):Double = {
    acos(sin(latRad)*sin(p.latRad) + cos(latRad)*cos(p.latRad)*cos(p.lngRad-lngRad))
  }

  def distanceInSeaMiles(p: Position): Double = {
    centralAngleRad(p)/Pi*180*60
  }
}