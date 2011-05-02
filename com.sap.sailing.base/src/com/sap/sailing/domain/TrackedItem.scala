package com.sap.sailing.domain

/**
 * Tracks a {@link Positioned} over time.
 */
trait TrackedItem {
  /**
   * Tries to approximate the position that the item might have had at
   * time point <code>t</code>. It is permissible for an implementation
   * to return <code>null</code> in case the position is not known and
   * cannot reasonably be interpolated.
   */
  def position(t:TimePoint):Position
}