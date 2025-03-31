package com.sap.sailing.domain.common;

public interface LegIdentifier extends RegattaAndRaceIdentifier {
    /**
     * @return only the {@link RegattaAndRaceIdentifier} without a leg index; although this interface extends
     *         RegattaAndRaceIdentifier, {@code this} != {@code this.getRaceIdentifier()}, and not even
     *         {@code this.equals(this.getRaceIdentifier())} because a class cast exception would result if the
     *         {@link RegattaAndRaceIdentifier} used to construct this {@link LegIdentifier} was itself not a
     *         {@link LegIdentifier}. This is also important when trying to look up a race by its race identifier, as,
     *         e.g., in a map.
     */
    RegattaAndRaceIdentifier getRaceIdentifier();

    int getOneBasedLegIndex();
}
