package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;

/**
 * Boat classes are displayed with different images (e.g. on a map) depending on the wind direction
 * and the course (upwind, downwind). This class holds all required images for the different situations.   
 * @author C5163874
 *
 */
public class BoatClassImageData {
    private final String mainBoatClassName;
    private final List<String> compatibleBoatClassNames;
    private final double boatClassLengthInMeter;
    private final int boatClassImageLengthInPx;
    private final int imageWidthInPx;
    private final int imageHeightInPx;

    private ImageResource upWindPortIcon;
    private ImageResource upWindPortIconSelected;
    private ImageResource upWindStarboardIcon;
    private ImageResource upWindStarboardIconSelected;

    /**
     * Boat image for wind from port and sails on starboard (downwind)
     */
    private ImageResource downWindPortIcon;
    private ImageResource downWindPortIconSelected;

    /**
     * Boat image with wind from starboard and sails on port (downwind)
     */
    private ImageResource downWindStarboardIcon;
    private ImageResource downWindStarboardIconSelected;

    private ImageResource reachingPortIcon;
    private ImageResource reachingPortIconSelected;
    private ImageResource reachingStarboardIcon;
    private ImageResource reachingStarboardIconSelected;

    private ImageTransformer upWindPortImageTransformer;
    private ImageTransformer upWindPortImageTransformerSelected;
    private ImageTransformer upWindStarboardImageTransformer;
    private ImageTransformer upWindStarboardImageTransformerSelected;

    private ImageTransformer downWindPortImageTransformer;
    private ImageTransformer downWindPortImageTransformerSelected;
    private ImageTransformer downWindStarboardImageTransformer;
    private ImageTransformer downWindStarboardImageTransformerSelected;

    private ImageTransformer reachingPortImageTransformer;
    private ImageTransformer reachingPortImageTransformerSelected;
    private ImageTransformer reachingStarboardImageTransformer;
    private ImageTransformer reachingStarboardImageTransformerSelected;

    public BoatClassImageData(String mainBoatClassName, double boatClassLengthInMeter, int boatClassImageLengthInPx,
            int imageWidthInPx, int imageHeightInPx) {
        this.mainBoatClassName = mainBoatClassName;
        this.boatClassLengthInMeter = boatClassLengthInMeter;
        this.boatClassImageLengthInPx = boatClassImageLengthInPx;
        this.imageWidthInPx = imageWidthInPx;
        this.imageHeightInPx = imageHeightInPx;
        this.compatibleBoatClassNames = new ArrayList<String>();
    }

    public BoatClassImageData(String mainBoatClassName, double boatClassLengthInMeter, int boatClassImageLengthInPx,
            int imageWidthInPx, int imageHeightInPx, String... compatibleBoatClasses) {
        this(mainBoatClassName, boatClassLengthInMeter, boatClassImageLengthInPx, imageWidthInPx, imageHeightInPx);
        for(String compatibleBoatClass: compatibleBoatClasses) {
            this.compatibleBoatClassNames.add(compatibleBoatClass);
        }
    }

    public ImageTransformer getBoatImageTransformerByLegTypeAndTack(LegType legType, Tack tack, boolean isSelected) {
        ImageTransformer result = null;
        if (tack == null) {
            tack = Tack.STARBOARD; // TODO this is just a fallback / workaround for not having a good image resource for unknown tack
        }
        switch (tack) {
        case PORT:
            switch (legType) {
            case DOWNWIND:
                if (isSelected) {
                    result = downWindPortImageTransformerSelected;
                } else {
                    result = downWindPortImageTransformer;
                }
                break;
            case REACHING:
                if (isSelected) {
                    result = reachingPortImageTransformerSelected;
                } else {
                    result = reachingPortImageTransformer;
                }
                break;
            case UPWIND:
                if (isSelected) {
                    result = upWindPortImageTransformerSelected;
                } else {
                    result = upWindPortImageTransformer;
                }
                break;
            }
            break;
        case STARBOARD:
            switch (legType) {
            case DOWNWIND:
                if (isSelected) {
                    result = downWindStarboardImageTransformerSelected;
                } else {
                    result = downWindStarboardImageTransformer;
                }
                break;
            case REACHING:
                if (isSelected) {
                    result = reachingStarboardImageTransformerSelected;
                } else {
                    result = reachingStarboardImageTransformer;
                }
                break;
            case UPWIND:
                if (isSelected) {
                    result = upWindStarboardImageTransformerSelected;
                } else {
                    result = upWindStarboardImageTransformer;
                }
                break;
            }
            break;
        }
        return result;
    }

    public ImageTransformer getBoatImageTransformerByTack(Tack tack, boolean isSelected) {
        ImageTransformer result = null;
        if (tack == null) {
            tack = Tack.STARBOARD; // TODO defaulting to a tack to avoid NPE; need to find a default representation for boats in unknown wind conditions
        }
        switch (tack) {
        case STARBOARD:
            if (isSelected) {
                result = upWindPortImageTransformerSelected;
            } else {
                result = upWindPortImageTransformer;
            }
            break;
        case PORT:
            if (isSelected) {
                result = upWindStarboardImageTransformerSelected;
            } else {
                result = upWindStarboardImageTransformer;
            }
        }
        return result;
    }

    public void setUpWindPortIcons(ImageResource upWindPortIcon, ImageResource upWindPortIconSelected) {
        this.upWindPortIcon = upWindPortIcon;
        this.upWindPortIconSelected = upWindPortIconSelected;

        if (upWindPortIcon != null) {
            upWindPortImageTransformer = new ImageTransformer(upWindPortIcon);
        }
        if (upWindPortIconSelected != null) {
            upWindPortImageTransformerSelected = new ImageTransformer(upWindPortIconSelected);
        }
    }

    public void setUpWindStarboardIcons(ImageResource upWindStarboardIcon, ImageResource upWindStarboardIconSelected) {
        this.upWindStarboardIcon = upWindStarboardIcon;
        this.upWindStarboardIconSelected = upWindStarboardIconSelected;

        if (upWindStarboardIcon != null) {
            upWindStarboardImageTransformer = new ImageTransformer(upWindStarboardIcon);
        }
        if (upWindStarboardIconSelected != null) {
            upWindStarboardImageTransformerSelected = new ImageTransformer(upWindStarboardIconSelected);
        }
    }

    public void setDownWindPortIcons(ImageResource downWindPortIcon, ImageResource downWindPortIconSelected) {
        this.downWindPortIcon = downWindPortIcon;
        this.downWindPortIconSelected = downWindPortIconSelected;

        if (downWindPortIcon != null) {
            downWindPortImageTransformer = new ImageTransformer(downWindPortIcon);
        }
        if (downWindPortIconSelected != null) {
            downWindPortImageTransformerSelected = new ImageTransformer(downWindPortIconSelected);
        }
    }

    public void setDownWindStarboardIcons(ImageResource downWindStarboardIcon, ImageResource downWindStarboardIconSelected) {
        this.downWindStarboardIcon = downWindStarboardIcon;
        this.downWindStarboardIconSelected = downWindStarboardIconSelected;

        if (downWindStarboardIcon != null) {
            downWindStarboardImageTransformer = new ImageTransformer(downWindStarboardIcon);
        }
        if (downWindStarboardIconSelected != null) {
            downWindStarboardImageTransformerSelected = new ImageTransformer(downWindStarboardIconSelected);
        }
    }

    public void setReachingPortIcons(ImageResource reachingPortIcon, ImageResource reachingPortIconSelected) {
        this.reachingPortIcon = reachingPortIcon;
        this.reachingPortIconSelected = reachingPortIconSelected;

        if (reachingPortIcon != null) {
            reachingPortImageTransformer = new ImageTransformer(reachingPortIcon);
        }
        if (reachingPortIconSelected != null) {
            reachingPortImageTransformerSelected = new ImageTransformer(reachingPortIconSelected);
        }
    }

    public void setReachingStarboardIcons(ImageResource reachingStarboardIcon,
            ImageResource reachingStarboardIconSelected) {
        this.reachingStarboardIcon = reachingStarboardIcon;
        this.reachingStarboardIconSelected = reachingStarboardIconSelected;

        if (reachingStarboardIcon != null) {
            reachingStarboardImageTransformer = new ImageTransformer(reachingStarboardIcon);
        }
        if (reachingStarboardIconSelected != null) {
            reachingStarboardImageTransformerSelected = new ImageTransformer(reachingStarboardIconSelected);
        }
    }

    public boolean isBoatClassNameCompatible(String boatClass) {
        boolean result = false;
        // remove all white space characters
        String boatClassToCheck = boatClass.replaceAll("\\s","");
        // remove all '-' characters
        boatClassToCheck = boatClass.replaceAll("-","");
        
        if(mainBoatClassName.equalsIgnoreCase(boatClassToCheck)) {
            result = true;
        } else {
            for(String compatibleName: compatibleBoatClassNames) {
                if(compatibleName.equalsIgnoreCase(boatClassToCheck)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
    
    public String getMainBoatClassName() {
        return mainBoatClassName;
    }

    public List<String> getCompatibleBoatClassNames() {
        return compatibleBoatClassNames;
    }

    public ImageResource getUpWindPortIcon() {
        return upWindPortIcon;
    }

    public ImageResource getUpWindPortIconSelected() {
        return upWindPortIconSelected;
    }

    public ImageResource getUpWindStarboardIcon() {
        return upWindStarboardIcon;
    }

    public ImageResource getUpWindStarboardIconSelected() {
        return upWindStarboardIconSelected;
    }

    public ImageResource getDownWindPortIcon() {
        return downWindPortIcon;
    }

    public ImageResource getDownWindPortIconSelected() {
        return downWindPortIconSelected;
    }

    public ImageResource getDownWindStarboardIcon() {
        return downWindStarboardIcon;
    }

    public ImageResource getDownWindStarboardIconSelected() {
        return downWindStarboardIconSelected;
    }

    public double getBoatClassLengthInMeter() {
        return boatClassLengthInMeter;
    }

    public int getBoatClassImageLengthInPx() {
        return boatClassImageLengthInPx;
    }

    public int getImageWidthInPx() {
        return imageWidthInPx;
    }

    public int getImageHeightInPx() {
        return imageHeightInPx;
    }

    public ImageResource getReachingPortIcon() {
        return reachingPortIcon;
    }

    public ImageResource getReachingPortIconSelected() {
        return reachingPortIconSelected;
    }

    public ImageResource getReachingStarboardIcon() {
        return reachingStarboardIcon;
    }

    public ImageResource getReachingStarboardIconSelected() {
        return reachingStarboardIconSelected;
    }
}
