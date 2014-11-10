package com.sap.sailing.polars.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.ArgumentMatcher;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.PolarSheetGenerationSettingsImpl;
import com.sap.sailing.domain.common.impl.PolarSheetsDataImpl;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.analysis.PolarSheetAnalyzer;
import com.sap.sailing.polars.analysis.impl.PolarSheetAnalyzerImpl;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;

public class PolarSheetAnalyzerTest {

    @Test
    public void testSpeedAndBearingCalculation() throws NotEnoughDataHasBeenAddedException {

        PolarSheetAnalyzer analyzer = new PolarSheetAnalyzerImpl(createMockedPolarDataService());
        BoatClass boatClass = mock(BoatClass.class);
        
        SpeedWithBearingWithConfidence<Void> result = analyzer.getAverageUpwindSpeedWithBearingOnStarboardTackFor(boatClass,
                new KnotSpeedImpl(14));
        assertThat(result.getObject().getKnots(), closeTo(8.5, 0.1));
        assertThat(result.getObject().getBearing().getDegrees(), closeTo(50, 0.1));
        assertThat(result.getConfidence(), closeTo(0.7, 0.1));
        
        SpeedWithBearingWithConfidence<Void> result2 = analyzer.getAverageDownwindSpeedWithBearingOnStarboardTackFor(boatClass,
                new KnotSpeedImpl(14));
        assertThat(result2.getObject().getKnots(), closeTo(14.2, 0.1));
        assertThat(result2.getObject().getBearing().getDegrees(), closeTo(142.8, 0.1));
        assertThat(result2.getConfidence(), closeTo(0.6, 0.1));
        
        SpeedWithBearingWithConfidence<Void> result3 = analyzer.getAverageUpwindSpeedWithBearingOnPortTackFor(boatClass,
                new KnotSpeedImpl(14));
        assertThat(result3.getObject().getKnots(), closeTo(8.6, 0.1));
        assertThat(result3.getObject().getBearing().getDegrees(), closeTo(-49.5, 0.1));
        assertThat(result3.getConfidence(), closeTo(0.7, 0.1));
        
        SpeedWithBearingWithConfidence<Void> result4 = analyzer.getAverageDownwindSpeedWithBearingOnPortTackFor(boatClass,
                new KnotSpeedImpl(14));
        assertThat(result4.getObject().getKnots(), closeTo(13.5, 0.1));
        assertThat(result4.getObject().getBearing().getDegrees(), closeTo(-143.6, 0.1));
        assertThat(result4.getConfidence(), closeTo(0.25, 0.1));
        
    }

    private PolarDataService createMockedPolarDataService() throws NotEnoughDataHasBeenAddedException {
        PolarDataService mockedPolarDataService = mock(PolarDataService.class);
        PolarSheetsData data = createPolarSheetsData();
        when(mockedPolarDataService.getPolarSheetForBoatClass(any(BoatClass.class))).thenReturn(data);
        
        //Starboard Upwind
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                        argThat(new BearingMatcher(49)))).thenReturn(
                new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(8.478048671702888), 0.5, null));
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(50)))).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(8.466303997538812), 0.5, null));
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(51)))).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(8.583383077026435), 0.5, null));
        
        //Starboard Downwind
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(141)))).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(13.794354053931528), 0.5, null));
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(142)))).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(13.665782579628802), 0.5, null));
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(143)))).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(14.169301888320263), 0.5, null));
        
        //Port Upwind
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(-48)))).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(8.445599410456111), 0.5, null));
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(-50)))).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(8.553274292235153), 0.5, null));
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(-49)))).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(8.614582090896583), 0.5, null));
        
        //Port Downwind
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(-145)))).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(13.78894715705271), 0.5, null));
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(-144)))).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(13.420656294986587), 0.5, null));
        when(mockedPolarDataService.getSpeed(any(BoatClass.class), argThat(new SpeedMatcher(14)),
                argThat(new BearingMatcher(-143)))).thenReturn(
        new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(13.268607457651942), 0.5, null));
        
        return mockedPolarDataService;
    }

    /**
     * The following data is taken from a real race. (49ER yellow R2, KW 2014)
     * Only the data for windspeed 14kn is used. The first block contains the 
     * boatspeeds for every angle to the wind. The second block contains the
     * datacount for each angle (number of underlying fixes).
     * 
     * @return
     */
    private PolarSheetsData createPolarSheetsData() {
        PolarSheetGenerationSettings settings = PolarSheetGenerationSettingsImpl.createStandardPolarSettings();
        Number[][] averagedPolarDataByWindSpeed = {
                {},
                {},
                {},
                {},
                {},
                { 0.0, 0.0, 0.35653425045212994, 0.0, 0.20934280998529808, 0.0, 0.0, 0.0, 0.3145227273331494, 0.0,
                        0.5123377765857685, 0.0, 0.0, 0.0, 0.3910305177324864, 0.01848543712702339, 74.14368617329595,
                        0.19873208170945428, 1.7877762410964606, 13.813130768721209, 10.642219209509047,
                        13.401166322502394, 8.369967315755705, 8.612484953161221, 6.830752063578309, 6.731226410756168,
                        7.534989526402173, 7.417304789344726, 7.689655016976699, 7.856820838665987, 7.877814207628512,
                        7.686942654891556, 7.94739600490786, 7.250053609589324, 7.658903932314423, 8.264104048379547,
                        7.97995749658058, 8.328973177890093, 8.445018706583248, 8.391458379377287, 8.316439838911991,
                        8.308509782296186, 8.41538252133265, 8.485027362920015, 8.301702107250204, 8.419623807296604,
                        8.624320785824453, 8.459533164762528, 8.563247204808349, 8.478048671702888, 8.466303997538812,
                        8.583383077026435, 8.459935549408716, 8.536070928645852, 8.484085598981617, 8.354759109745252,
                        8.273057288860901, 8.234006491204791, 7.8843340092591445, 7.832604832601752, 7.752662035724477,
                        7.383456897289133, 6.821527524231923, 6.316822983662531, 5.94010223470201, 5.778283414530163,
                        5.288261984314158, 5.0632678301953415, 4.575991234971232, 4.4246483551331615,
                        3.754104138232388, 3.574202962043522, 2.809782370429744, 2.3756561838866554,
                        1.4733065799328493, 1.4254216934512096, 2.3480707715085587, 2.2261678547163744,
                        1.3404842722592116, 1.3284863115290033, 1.2125511969957223, 1.6516010763946074,
                        1.3858564062376648, 1.206826186297077, 1.3739210408131197, 1.484483916721746, 1.16345069673396,
                        1.030671321957646, 1.0099211183312304, 1.128789637547173, 1.3136200715041277,
                        1.0889100793268662, 1.619317841041691, 1.3193441080791892, 1.0031422557092482,
                        0.7919668067046017, 0.9306316555528391, 0.8006819358007504, 1.4692741310417397,
                        1.0078551872416406, 0.18256090691178703, 1.0285132794809515, 0.6438471218131188,
                        0.6380303949260124, 0.664014588165121, 0.7808063306582489, 0.7898512748200681,
                        0.6511549225287516, 0.6376995913182613, 0.7570886726617079, 0.6619740768653626,
                        0.7632028965886186, 0.6338755107017753, 0.6640484201429284, 2.127282972819131,
                        2.8987479648532997, 6.683424580288278, 8.507014641722314, 7.703364841210107, 4.71357814053702,
                        8.000825810341617, 7.671441013221641, 9.115713955723916, 9.023412736773132, 7.998471846760262,
                        10.3966884832781, 9.795531845297319, 8.84626879400292, 10.7522343414125, 12.416303781075975,
                        11.722882328824802, 11.955629251917987, 13.323328215681695, 12.902836778810396,
                        12.82088099069422, 13.274245192043882, 13.06739796258905, 13.047121734187872,
                        13.442188461750433, 13.332939764570671, 13.558632900491695, 13.794354053931528,
                        13.665782579628802, 14.169301888320263, 13.974157665863203, 14.222780950415583,
                        14.47187020132089, 14.341301813979271, 14.194209653738831, 14.344773681878142,
                        13.962146190803251, 14.11383996792033, 14.281588010608605, 14.603553724187172,
                        14.762753353002006, 14.123456040381438, 15.046630203667128, 14.39273398895829,
                        14.224594624139609, 15.655628777674853, 15.21593321294868, 13.558581393009002,
                        11.63031430546637, 11.885139926443486, 8.090192790566025, 8.85949813118447, 9.734779186465921,
                        9.822728590064322, 9.45181850091312, 12.306111307584684, 13.943542199831565,
                        11.610604465928457, 12.093955433640602, 12.352243834497187, 5.509081709082437,
                        9.386182259798517, 11.796573155778503, 10.752292039895153, 11.909890662822576,
                        10.116169101754338, 12.694529106484943, 12.670730206147631, 10.379478057323539,
                        12.672626215427158, 10.724408451389763, 10.087325562474476, 6.243751559092033,
                        10.222427424506762, 10.02529936912795, 11.0353088918245, 11.096075421534366,
                        11.682256641020984, 10.803765317446135, 11.21085498256219, 12.986416296517614,
                        13.6093151775165, 11.851727180875798, 15.451306097090033, 12.382813246378253,
                        14.86900369646574, 14.349497782030753, 13.368463669152609, 14.914498105361215,
                        13.627605853093092, 14.511034781204497, 15.204877751897692, 13.56324380463669,
                        13.796723457212012, 14.126664346993518, 14.083851682454043, 13.984023085926546,
                        13.77727116288626, 13.924407601290493, 14.167888563062107, 13.969213312748776,
                        13.78894715705271, 13.420656294986587, 13.268607457651942, 13.107087350500771,
                        12.968302178632449, 12.900886466337163, 12.63727656702256, 13.324437322806876,
                        12.447444273895949, 11.929720685469128, 11.866720727517666, 11.845857431417869,
                        12.34750950745547, 11.82999403657738, 11.56808623971416, 11.005790647087728,
                        11.199641554710265, 11.147034585879346, 9.581506313873323, 9.377505334232003,
                        9.165927874941573, 9.039110322467469, 9.158860532503148, 8.335083032404867, 7.794601241888998,
                        8.095394521682454, 5.288448397940189, 5.032462558577374, 6.0372815329868175, 5.550230684686165,
                        5.469127647370254, 7.712573013931298, 6.905684954724151, 6.244400641700857, 4.733792287830745,
                        5.703038182173567, 5.705814871750967, 3.6710816576685543, 4.290237091844638,
                        3.9287689933642715, 3.8420701003482005, 4.049827829086237, 3.2238964501634513,
                        3.153176378342545, 2.7186371105079665, 4.5320344683521725, 6.205007707190399,
                        0.9290805079838338, 5.461334397488564, 0.11769821370235434, 4.821232524035565,
                        6.535593439138594, 1.430640812116557, 1.6028097505376921, 2.444286329582734, 3.432921232002375,
                        1.5524241414822946, 4.463117209069594, 9.233008385665372, 6.701689021468631, 6.091887266547403,
                        6.736705820169654, 6.638753611795072, 8.315606347683866, 7.192407235278191, 7.065178905446558,
                        7.340556868387216, 7.8140170631317565, 8.490879454652937, 8.869186790970112, 8.089554910312003,
                        8.773712136668893, 8.48502143463395, 8.506940480879672, 8.426924203848873, 8.470754959099478,
                        8.34341243311524, 8.812850540132228, 8.471103442329234, 8.95942249226152, 8.368125204092756,
                        8.101176539059828, 8.536922798672386, 8.039899341914161, 8.66061693085369, 8.522478004005418,
                        8.354071503807528, 8.299401288177423, 8.308870759000826, 8.382553283761265, 8.508054693539464,
                        8.508341232737465, 8.416280733708618, 8.624730989938895, 8.445599410456111, 8.553274292235153,
                        8.614582090896583, 8.619979792865113, 8.497641411226578, 8.59426371914717, 8.638910090156958,
                        8.720583873644639, 8.582290408559516, 8.508363936388186, 8.555021199411986, 8.575018051008957,
                        8.415002407687826, 8.570989437948075, 8.47499610688996, 8.153991578948714, 8.20568750487526,
                        8.16792006664647, 8.035532991444567, 8.374627467796898, 8.53819686502552, 7.998112484617343,
                        5.001488152415163, 4.248045472089586, 8.06284784348518, 6.586154602284839, 9.171084517442075,
                        9.047519598278601, 8.290140115687464, 6.627513257025532, 9.118867780171255, 0.0,
                        0.15031820068279983, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.19624692515939246, 0.0, 0.0, 0.0,
                        0.6993196184976416, 0.0, 0.0, 0.0, 0.0, 0.0, 0.37664203710504496 }, {}, {}, {}, {} };
        
        Map<Integer, Integer[]> dataCountsPerAngleAndWindSpeed = new HashMap<>();
        Integer[] dataCountsPerAngle = { 0, 0, 2, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 3, 1, 2, 1, 6, 2, 2, 2, 1, 6, 6, 17,
                13, 19, 30, 43, 32, 50, 48, 57, 70, 76, 107, 129, 177, 186, 222, 237, 275, 285, 321, 322, 401, 451,
                466, 413, 468, 479, 459, 429, 360, 345, 317, 288, 278, 202, 175, 130, 113, 104, 100, 73, 45, 51, 52,
                37, 40, 36, 26, 23, 32, 33, 29, 29, 22, 24, 27, 23, 22, 23, 22, 21, 15, 18, 20, 14, 15, 21, 20, 19, 6,
                12, 10, 9, 10, 8, 1, 9, 6, 6, 14, 16, 7, 7, 6, 2, 5, 3, 6, 5, 8, 7, 14, 14, 12, 9, 20, 14, 22, 28, 14,
                20, 22, 29, 30, 38, 45, 50, 72, 87, 117, 114, 110, 145, 148, 156, 165, 150, 194, 224, 211, 196, 199,
                198, 182, 155, 125, 85, 69, 65, 49, 46, 61, 47, 48, 51, 45, 21, 14, 8, 9, 8, 8, 3, 7, 5, 2, 10, 8, 7,
                7, 10, 8, 13, 13, 11, 5, 9, 9, 8, 15, 6, 3, 5, 11, 9, 12, 5, 13, 12, 18, 15, 31, 25, 35, 36, 39, 59,
                71, 55, 52, 64, 72, 66, 70, 89, 104, 81, 120, 130, 117, 107, 113, 137, 136, 126, 108, 103, 92, 85, 76,
                65, 75, 75, 70, 57, 49, 49, 35, 44, 35, 18, 22, 17, 18, 10, 6, 4, 10, 9, 10, 8, 4, 4, 4, 10, 4, 4, 4,
                9, 8, 8, 7, 7, 6, 4, 7, 4, 1, 6, 6, 4, 6, 5, 5, 5, 6, 5, 9, 7, 12, 16, 10, 25, 25, 30, 29, 47, 44, 31,
                69, 68, 79, 62, 66, 86, 72, 84, 89, 75, 73, 62, 104, 110, 147, 137, 170, 204, 310, 347, 354, 377, 433,
                457, 419, 436, 467, 502, 513, 525, 463, 422, 405, 363, 309, 282, 262, 186, 162, 123, 97, 81, 45, 35,
                30, 24, 20, 12, 9, 8, 5, 6, 4, 6, 7, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 1 };
        dataCountsPerAngleAndWindSpeed.put(5, dataCountsPerAngle);
        PolarSheetsData data = new PolarSheetsDataImpl(averagedPolarDataByWindSpeed, 50000,
                dataCountsPerAngleAndWindSpeed, settings.getWindStepping(), null);
        return data;
    }
    
    private class SpeedMatcher extends ArgumentMatcher<Speed> {
        
        private final double speedToMatchInKnots;
        
        public SpeedMatcher(double speedToMatchInKnots) {
            this.speedToMatchInKnots = speedToMatchInKnots;
        }


        @Override
        public boolean matches(Object argument) {
            boolean result = false;
            if (argument != null) {
                Speed speed = (Speed) argument;
                if (speed.getKnots() > speedToMatchInKnots - 0.05 && speed.getKnots() < speedToMatchInKnots + 0.05) {
                    result = true;
                }
            }
            return result;
        }
        
    }
    
    private class BearingMatcher extends ArgumentMatcher<Bearing> {

        private final double bearingToMatchInDegrees;

        public BearingMatcher(double bearingToMatchInDegrees) {
            this.bearingToMatchInDegrees = bearingToMatchInDegrees;
        }

        @Override
        public boolean matches(Object argument) {
            boolean result = false;
            if (argument != null) {
                Bearing bearing = (Bearing) argument;
                if (bearing.getDegrees() > bearingToMatchInDegrees - 0.4999999
                        && bearing.getDegrees() < bearingToMatchInDegrees + 0.49999999) {
                    result = true;
                }
            }
            return result;
        }

    }

}
