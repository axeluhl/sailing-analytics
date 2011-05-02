package com.sap.sailing.domain

import scala.math._

trait Position {
  /**
   * Latitude in degrees; -90..+90
   */
  def latDeg: Double

  /**
   * Longitude in degrees; -180..+180
   */
  def lngDeg: Double
  
  /**
   * Latitude in radians; -PI/2..+PI/2
   */
  def latRad : Double
  
  /**
   * Longitude in radians; -PI..+PI
   */
  def lngRad : Double
  
  def centralAngleRad(p:Position):Double = {
    acos(sin(latRad)*sin(p.latRad) + cos(latRad)*cos(p.latRad)*cos(p.lngRad-lngRad))
  }

  def distanceInSeaMiles(p: Position): Double = {
    centralAngleRad(p)/Pi*180*60
  }
}