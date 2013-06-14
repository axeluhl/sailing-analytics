package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.base.BoatClass;


public interface RaceType {
    
    public enum OlympicRaceCode {

//        SAM102000 Men's Windsurfer = Windsufer M�nner RS:X
//        SAW102000 Women's Windsurfer = Windsurfer Damen RS:X
//        SAM004000 Men's One Person Dinghy = Laser M�nner
//        SAW103000 Women's One Person Dinghy = Laser Damen Laser Radial
//        SAM002000 Men's One Person Dinghy Heavy = Finn Dinghy M�nner
//        SAM005000 Men's Two Person Dinghy = 470er M�nner
//        SAW005000 Women's Two Person Dinghy = 470er Damen
//        SAM009000 Men's Skiff = 49er M�nner
//        SAM007000 Men's Keelboat = Starboot M�nner 
//        SAW010000 Women's Match Racing = Matchrace Damen Elliott 6M (modified)
        

        UNKNOWN("Unspecified", "Unspecified", Gender.unspecified),
        FINN("SAM002", "Finn", Gender.male), 
        LASER("SAM004", "Laser", Gender.male), 
        _470_WOMEN("SAW005", "470", Gender.female), 
        _470_MEN("SAM005", "470", Gender.male), 
        STAR("SAM007", "Star", Gender.male), 
        _49ER("SAM009", "49er", Gender.male), 
        ELLIOTT6M("SAW007", "Elliott 6M", Gender.female), 
        RSX_WOMEN("SAW102", "RS:X", Gender.female), 
        RSX_MEN("SAM102", "RS:X", Gender.male), 
        LASER_RADIAL("SAW103", "Laser Radial", Gender.female), 
        _49ERFX_TODO("xxxxx_TODO_xxxxx", "49er FX", Gender.female), 
        NACRA17_TOTO("xxxxx_TODO_xxxxx", "Nacra 17", Gender.mixed);
        
        enum Gender {
            male, female, mixed, unspecified;
        }
        
        public final String swissTimingCode;
        
        public final String boatClassName;
        
        public final Gender gender;
        
        public final boolean typicallyStartsUpwind;

        private OlympicRaceCode(String swissTimingCode, String boatClassName, Gender gender) {
            this.swissTimingCode = swissTimingCode;
            this.boatClassName = boatClassName;
            this.gender = gender;
            this.typicallyStartsUpwind = true; 
        }
        
    }

    OlympicRaceCode getRaceCode();

    BoatClass getBoatClass();

}
