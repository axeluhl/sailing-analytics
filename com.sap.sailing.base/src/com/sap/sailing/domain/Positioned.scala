package com.sap.sailing.domain

/**
 * Some item with a position. The position doesn't need to be fixed.
 * Asking the position will return the item's current position.
 */
trait Positioned {
	def position:Position
}