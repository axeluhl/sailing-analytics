package com.sap.sailing.gwt.ui.simulator.streamlets;

import com.sap.sailing.domain.common.Position;

/**
 * A field of vectors to display in a streamlet {@link Swarm}. The {@link Vector}s returned by {@link #getVector(Position)}
 * are used to initialize {@link Particle}s. Their "speed" is the {@link Vector#length() length} of the vector.<p>
 * 
 * This field provides color strings for each possible speed which are then used to style the particles as they fly through
 * the swarm.
 * 
 * @author Christopher Ronnewinkel (D036654)
 * @author Axel Uhl (D043530)
 *
 */
public interface VectorField {
    /**
     * Tells whether <code>p</code> is within the bounds of this field.
     */
    boolean inBounds(Position p, boolean visFull);

    /**
     * The vector field's value at position <code>p</code>.
     */
    Vector getVector(Position p);

    /**
     * @return the maximum {@link Vector#length() length} of any vector returned by {@link #getVector(Position)} in this field for
     * positions that are {@link #inBounds(Position, boolean) within bounds}.
     */
    double getMaxLength();

    double motionScale(int zoomLevel);

    /**
     * A weight between 0.0 and 1.0 (inclusive) that tells the probability at which a particle at position <code>p</code>
     * and with speed vector <code>v</code> will be shown. 1.0 means it will certainly be shown; 0.0 means it will certainly
     * not be shown.
     */
    double particleWeight(Position p, Vector v);

    /**
     * Computes a line width for a particle flying at a certain speed.
     * 
     * @param speed
     *            a speed as obtained by computing a {@link Vector}'s {@link Vector#length() length}.
     */
    double lineWidth(double speed);

    Position[] getFieldCorners(boolean visFull);

    double getParticleFactor();

    void setStep(int step);

    void nextStep();

    void prevStep();

    /**
     * Computes a color for a particle flying at a certain speed.
     * 
     * @param speed
     *            a speed as obtained by computing a {@link Vector}'s {@link Vector#length() length}.
     * @return an RGB color with optional transparency that is a legal color in CSS. Example:
     *         <code>"rgba(1, 2, 3, 0.4)"</code>
     */
    String getColor(double speed);
}
