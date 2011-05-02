package com.sap.sailing.domain

/**
 * Note that a Mark's position doesn't have to remain the same.
 * For example, if the mark is represented by a buoy, the buoy may
 * drift, based on wind and current, and hence change its position.
 * Such a position change may be tracked live by a GPS device.
 */
trait Mark extends Waypoint with Positioned {
}