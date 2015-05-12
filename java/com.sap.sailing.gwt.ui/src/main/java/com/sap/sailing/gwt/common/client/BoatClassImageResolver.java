package com.sap.sailing.gwt.common.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.domain.common.BoatClassMasterdata;

public class BoatClassImageResolver {
    private static Map<String, ImageResource> boatClassIconsMap;
    private static BoatClassImageResources imageResources = BoatClassImageResources.INSTANCE;
    
    static {
        boatClassIconsMap = new HashMap<String, ImageResource>();
        
        boatClassIconsMap.put(BoatClassMasterdata._12M.getDisplayName(), imageResources._12mRIcon());
        boatClassIconsMap.put(BoatClassMasterdata._2_4M.getDisplayName(), imageResources._2_4mIcon());
        boatClassIconsMap.put(BoatClassMasterdata._29ER.getDisplayName(), imageResources._29erIcon());
        boatClassIconsMap.put(BoatClassMasterdata._420.getDisplayName(), imageResources._420Icon());
        boatClassIconsMap.put(BoatClassMasterdata._470.getDisplayName(), imageResources._470Icon());
        boatClassIconsMap.put(BoatClassMasterdata._49ERFX.getDisplayName(), imageResources._49erFXIcon());
        boatClassIconsMap.put(BoatClassMasterdata._49ER.getDisplayName(), imageResources._49erIcon());
        boatClassIconsMap.put(BoatClassMasterdata._5O5.getDisplayName(), imageResources._505mIcon());
        boatClassIconsMap.put(BoatClassMasterdata.A_CAT.getDisplayName(), imageResources.A_CatIcon());
        boatClassIconsMap.put(BoatClassMasterdata.ALBIN_EXPRESS.getDisplayName(), imageResources.AlbinExpressIcon());
        boatClassIconsMap.put(BoatClassMasterdata.B_ONE.getDisplayName(), imageResources.BOne());
        boatClassIconsMap.put(BoatClassMasterdata.CONTENDER.getDisplayName(), imageResources.ContenderIcon());
        boatClassIconsMap.put(BoatClassMasterdata.DRAGON_INT.getDisplayName(), imageResources.DragonIcon());
        boatClassIconsMap.put(BoatClassMasterdata.EUROPE_INT.getDisplayName(), imageResources.EuropeIcon());
        boatClassIconsMap.put(BoatClassMasterdata.EXTREME_40.getDisplayName(), imageResources.Extreme40Icon());
        boatClassIconsMap.put(BoatClassMasterdata.D_35.getDisplayName(), imageResources.D35Icon());
        boatClassIconsMap.put(BoatClassMasterdata.F_16.getDisplayName(), imageResources.F16Icon());
        boatClassIconsMap.put(BoatClassMasterdata.F_18.getDisplayName(), imageResources.F18Icon());
        boatClassIconsMap.put(BoatClassMasterdata.FARR_30.getDisplayName(), imageResources.Farr30Icon());
        boatClassIconsMap.put(BoatClassMasterdata.FINN.getDisplayName(), imageResources.FinnIcon());
        boatClassIconsMap.put(BoatClassMasterdata.FLYING_DUTCHMAN.getDisplayName(), imageResources.FlyingDutchmanIcon());
        boatClassIconsMap.put(BoatClassMasterdata.FOLKBOAT.getDisplayName(), imageResources.FolkBoatIcon());
        boatClassIconsMap.put(BoatClassMasterdata.H_BOAT.getDisplayName(), imageResources.H_BoatIcon());
        boatClassIconsMap.put(BoatClassMasterdata.HOBIE_16.getDisplayName(), imageResources.HobieIcon());
        boatClassIconsMap.put(BoatClassMasterdata.HOBIE_TIGER.getDisplayName(), imageResources.HobieIcon());
        boatClassIconsMap.put(BoatClassMasterdata.HOBIE_WILD_CAT.getDisplayName(), imageResources.HobieIcon());
        boatClassIconsMap.put(BoatClassMasterdata.J24.getDisplayName(), imageResources.J24Icon());
        boatClassIconsMap.put(BoatClassMasterdata.J70.getDisplayName(), imageResources.J70Icon());
        boatClassIconsMap.put(BoatClassMasterdata.J80.getDisplayName(), imageResources.J80Icon());
        boatClassIconsMap.put(BoatClassMasterdata.LASER_SB3.getDisplayName(), imageResources.LaserSB3Icon());
        boatClassIconsMap.put(BoatClassMasterdata.LASER_INT.getDisplayName(), imageResources.LaserIcon());
        boatClassIconsMap.put(BoatClassMasterdata.LASER_4_7.getDisplayName(), imageResources.LaserIcon());
        boatClassIconsMap.put(BoatClassMasterdata.LASER_RADIAL.getDisplayName(), imageResources.LaserIcon());
        boatClassIconsMap.put(BoatClassMasterdata.MELGES_24.getDisplayName(), imageResources.Melges24Icon());
        boatClassIconsMap.put(BoatClassMasterdata.MINI_TRANSAT.getDisplayName(), imageResources.MiniTransatIcon());
        boatClassIconsMap.put(BoatClassMasterdata.MUSTO_SKIFF.getDisplayName(), imageResources.MustoSkiffIcon());
        boatClassIconsMap.put(BoatClassMasterdata.NACRA_17.getDisplayName(), imageResources.Nacra17Icon());
        boatClassIconsMap.put(BoatClassMasterdata.OK.getDisplayName(), imageResources.OKIcon());
        boatClassIconsMap.put(BoatClassMasterdata.OPTIMIST.getDisplayName(), imageResources.OptimistIcon());
        boatClassIconsMap.put(BoatClassMasterdata.PIRAT.getDisplayName(), imageResources.PiratIcon());
        boatClassIconsMap.put(BoatClassMasterdata.PLATU_25.getDisplayName(), imageResources.Platu25Icon());
        boatClassIconsMap.put(BoatClassMasterdata.RS_X.getDisplayName(), imageResources.RSXIcon());
        boatClassIconsMap.put(BoatClassMasterdata.SONAR.getDisplayName(), imageResources.SonarIcon());
        boatClassIconsMap.put(BoatClassMasterdata.STAR.getDisplayName(), imageResources.StarIcon());
        boatClassIconsMap.put(BoatClassMasterdata.STREAMLINE.getDisplayName(), imageResources.StreamlineIcon());
        boatClassIconsMap.put(BoatClassMasterdata.SWAN_45.getDisplayName(), imageResources.Swan45Icon());
        boatClassIconsMap.put(BoatClassMasterdata.TORNADO.getDisplayName(), imageResources.TornadoIcon());
        boatClassIconsMap.put(BoatClassMasterdata.X_99.getDisplayName(), imageResources.X99Icon());
        boatClassIconsMap.put(BoatClassMasterdata.INTERNATIONAL_14.getDisplayName(), imageResources.International14Icon());
        boatClassIconsMap.put(BoatClassMasterdata.DYAS.getDisplayName(), imageResources.DyasIcon());
        boatClassIconsMap.put(BoatClassMasterdata.OPEN_BIC.getDisplayName(), imageResources.OpenBicIcon());
    }

    public static ImageResource getBoatClassIconResource(String displayName) {
        return boatClassIconsMap.get(displayName);
    }
}
