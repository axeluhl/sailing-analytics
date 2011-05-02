package com.sap.sailing.base.test
import com.sap.sailing.domain.DegreePosition

import scala.math.abs 
import org.junit.Assert._
import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit

class PositionTests extends AssertionsForJUnit {
	@Test def simpleTest() {
	  val p1 = new DegreePosition(49.2, 008.3)
	  val p2 = new DegreePosition(49.3, 008.2)
	  assertTrue(p1.distanceInSeaMiles(p2) < 10)
	  val northPole = new DegreePosition(90, 0)
	  val southPole = new DegreePosition(-90, 0)
	  assertTrue(abs(southPole.distanceInSeaMiles(northPole) * 1.852 - 20000) < 2)
	}
}