package com.sap.sailing.gwt.ui.leaderboard;


/**
 * In the particular case of a {@link RaceColumn}, this interface helps to decouple the life cycle of the child columns,
 * the parent column and the initialization of the {@link RaceColumn}'s <code>raceName</code> attribute. The latter is
 * set only after the superclass constructor is run. The superclass constructor, however, already requests the column
 * map which requires constructing certain column types (such as {@link ManeuverCountRaceColumn}) which require to
 * know the race name at some later time. When the detail column map is constructed, the race name attribute is
 * not yet set. This interface helps delaying the access from within the child column implementation to the latest possible
 * time so that the parent column then already knows the race name.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface RaceNameProvider {
    String getRaceColumnName();
}
