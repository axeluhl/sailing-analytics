package com.sap.sailing.simulator.test.analysis;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.Util.Quadruple;
import com.sap.sailing.gwt.ui.server.SimulatorServiceImpl;
import com.sap.sailing.gwt.ui.shared.ConfigurationException;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.RequestTotalTimeDTO;
import com.sap.sailing.gwt.ui.shared.ResponseTotalTimeDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;

public class PolylineTotalTimeComparison {

    private List<PositionDTO> turnPoints = null;
    private List<SimulatorWindDTO> allPoints = null;

    private Map<Integer, String> boatClassesIndexes = null;
    private Map<Integer, String> averageWindFlags = null;
    private List<Integer> timeStepMillisecondsSizes = null;

    private void loadTravemuendeSetup() {

        this.turnPoints = new ArrayList<PositionDTO>();
        this.turnPoints.add(new PositionDTO(53.971996999999995, 10.890202));
        this.turnPoints.add(new PositionDTO(53.971996999999995, 10.89015));
        this.turnPoints.add(new PositionDTO(53.971008, 10.889305));
        this.turnPoints.add(new PositionDTO(53.970982, 10.88933));
        this.turnPoints.add(new PositionDTO(53.969223, 10.893975));
        this.turnPoints.add(new PositionDTO(53.969066999999995, 10.893665));
        this.turnPoints.add(new PositionDTO(53.967254999999994, 10.891278));
        this.turnPoints.add(new PositionDTO(53.967197, 10.89124));
        this.turnPoints.add(new PositionDTO(53.967152, 10.891295));
        this.turnPoints.add(new PositionDTO(53.967152, 10.891352999999999));

        this.allPoints = new ArrayList<SimulatorWindDTO>();
        this.allPoints.add(new SimulatorWindDTO(53.971996999999995, 10.890202, 2.984523959547039, 349.9602498778341, 1317552429000L, true));
        this.allPoints.add(new SimulatorWindDTO(53.971996999999995, 10.89015, 2.983756943355911, 349.9029470610247, 1317552433000L, true));
        this.allPoints.add(new SimulatorWindDTO(53.971973, 10.890122, 2.983938144474681, 349.56901891966123, 1317552435000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971942999999996, 10.890096999999999, 2.9839576684565303, 348.50872500947935, 1317552438000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.97192, 10.890065, 1.9842870108009039, 352.4857255615666, 1317552441000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971892999999994, 10.890032999999999, 1.9812770598914324, 353.1945236858144, 1317552444000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971852, 10.889997, 3.9801076361930807, 322.1548942226666, 1317552447000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971824999999995, 10.889968, 3.9800110301713296, 323.6154147907459, 1317552450000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971787, 10.889927, 3.979486412399821, 326.80631462831866, 1317552454000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971767, 10.889904999999999, 3.979507535656368, 328.8074456249499, 1317552456000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.97174, 10.88988, 3.9784682874410784, 329.288199175368, 1317552459000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971709999999995, 10.8898569, 3.977261020564048, 328.2700374473921, 1317552462000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971672, 10.88983, 3.9767689693593313, 328.01671546008606, 1317552465000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971638, 10.889807, 3.9805844100702217, 340.23939624695726, 1317552468000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971602999999995, 10.889782, 3.9814823919456437, 340.7808678527942, 1317552471000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971568, 10.889752999999999, 3.9827147349476415, 342.3756501753792, 1317552474000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971537999999995, 10.889726999999999, 3.982971635797311, 342.49280184014435, 1317552477000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971505, 10.889693, 3.9826296937668966, 342.0367113801686, 1317552480000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971447, 10.88964, 3.983638981516868, 342.335253578428, 1317552484000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971419999999995, 10.889619999999999, 3.9846732402544496, 342.8898072504256, 1317552486000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971382, 10.88959, 2.989845510129004, 342.5263024471241, 1317552489000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971348, 10.8895579, 2.989920477422346, 341.86531643852015, 1317552492000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971317, 10.889522999999999, 3.9844819253767905, 341.24373659637564, 1317552495000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971283, 10.88949, 3.985170837245248, 339.9196093897658, 1317552498000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971244999999996, 10.889462, 2.9888075059893446, 338.13596902011193, 1317552501000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971207, 10.889433, 2.9890405117494936, 338.7793625873434, 1317552504000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971157, 10.889391999999999, 2.9896262730816066, 340.24386243072615, 1317552508000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971135, 10.889375, 1.9927249335417416, 340.7698130310835, 1317552510000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971084999999995, 10.889337, 1.9930779867514163, 341.90476339304263, 1317552513000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971047, 10.88931, 1.9933478962471867, 344.14057614393374, 1317552516000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.971008, 10.889305, 1.9932701656125142, 343.85644765261975, 1317552519000L, true));
        this.allPoints.add(new SimulatorWindDTO(53.970982, 10.88933, 1.9933553123860477, 342.6859369234896, 1317552522000L, true));
        this.allPoints.add(new SimulatorWindDTO(53.970969999999994, 10.889363, 1.9937413115992815, 341.9314895045188, 1317552525000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970955, 10.889403, 1.993937712533191, 342.2051664279124, 1317552528000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.97094, 10.889448, 1.9939420685473537, 342.2684239874015, 1317552531000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970917, 10.889495, 1.9939494862330756, 341.65494940937, 1317552534000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970898, 10.889553, 1.9939876194264992, 342.6794973612869, 1317552537000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970875, 10.8896219, 1.9941959494062582, 342.7641063383077, 1317552540000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970855, 10.889692, 1.994285708707734, 342.56791113518364, 1317552543000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970836999999996, 10.889764999999999, 1.9943696469750052, 342.8541098060506, 1317552546000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970817999999994, 10.889842, 2.992051028909478, 346.7460520737792, 1317552549000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970797999999995, 10.889899999999999, 2.99213533108893, 346.3595342740759, 1317552552000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970772, 10.88998, 2.9921378165235315, 346.3334069162466, 1317552556000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970757, 10.890018, 2.9919731161721685, 346.6541561148093, 1317552559000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970737, 10.890077999999999, 2.9918484052628167, 346.6265097547791, 1317552561000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970721999999995, 10.890137, 2.9919696463902894, 345.86944753319335, 1317552565000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970703, 10.890195, 2.9922359543491397, 345.16579495743713, 1317552567000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970687999999996, 10.890255, 2.9926110577532596, 345.31818096960166, 1317552571000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970667999999996, 10.890317, 3.990197303148564, 346.7917519111191, 1317552573000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.97065, 10.890378, 4.9877626042622305, 346.7871031348817, 1317552577000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.97063, 10.890438, 4.987946745853157, 346.11588655304735, 1317552579000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970611999999996, 10.8904979, 3.9903741992061335, 343.34353951300676, 1317552583000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970585, 10.890578, 3.99050019067749, 341.8299061497792, 1317552586000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970572999999995, 10.890618, 3.99055478275631, 341.72944470296346, 1317552589000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970546999999996, 10.890675, 3.9905383561192638, 342.65740656096045, 1317552591000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970527999999995, 10.890737, 4.988331474689867, 342.6670972067119, 1317552595000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970507999999995, 10.890792, 4.988416032049448, 343.0349072594504, 1317552597000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970485, 10.890855, 4.988375618098542, 343.33960214772776, 1317552601000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970467, 10.890917, 4.988444767040461, 343.52168191286523, 1317552603000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970454999999994, 10.890977, 5.986456652093722, 344.010261157041, 1317552607000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970428, 10.891062, 5.986666454129145, 344.559742974925, 1317552610000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970417, 10.891105, 5.986610328660012, 345.0432813363235, 1317552613000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970402, 10.891162999999999, 5.986550668900832, 346.10369655061356, 1317552615000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970375, 10.891219999999999, 5.98659235283101, 346.73416854200997, 1317552619000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970352, 10.891283, 5.986668543980886, 346.9074680016649, 1317552621000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970333, 10.891342, 5.9866231307038245, 347.57709090315444, 1317552624000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970313, 10.891399999999999, 5.986808952526647, 347.27457372630676, 1317552627000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970253, 10.891612, 4.989191910548317, 345.52662759359936, 1317552637000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970226999999994, 10.891695, 3.9914231763027828, 345.2417519488361, 1317552640000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970212, 10.891741999999999, 3.991459609920416, 344.3916306327969, 1317552643000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970192, 10.891805, 3.991471656272397, 343.8509733502731, 1317552645000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970177, 10.891867999999999, 3.9910165376159914, 344.01506150262946, 1317552649000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.97015, 10.891928, 3.9911184344639863, 343.7753933518346, 1317552651000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970132, 10.891993, 3.9915876593813024, 343.815805957377, 1317552655000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970112, 10.892059999999999, 3.9915831526256498, 344.9269148375556, 1317552657000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970088, 10.892128, 3.991470858615165, 346.53925429073087, 1317552661000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970067, 10.892202, 3.9915307447638333, 347.21342291948184, 1317552663000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970043, 10.892275, 2.9937487865211123, 349.2275086448683, 1317552667000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.970005, 10.8923729, 2.9938140232666157, 348.8778006253354, 1317552670000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969989999999996, 10.892422, 2.99381013146173, 348.6254217038023, 1317552673000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969963, 10.892495, 2.9938705558568466, 350.19547664370094, 1317552675000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969927999999996, 10.892567999999999, 2.9938640367432354, 352.4159434326862, 1317552679000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969895, 10.892642, 2.9938554080636357, 352.40403683218165, 1317552681000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969857, 10.8927099, 2.9939248281540203, 351.5959301668413, 1317552685000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969818, 10.892781999999999, 2.9939539722989266, 352.0724602737549, 1317552687000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.96978, 10.892857, 2.9939266694741358, 352.5559986519265, 1317552691000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969738, 10.892933, 2.9939376759381187, 352.35440146868115, 1317552693000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969697, 10.893017, 2.993893241330766, 353.01296399905954, 1317552697000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969657999999995, 10.893101999999999, 2.993882319709309, 354.11160932746793, 1317552699000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969617, 10.893182999999999, 2.9940120284375746, 354.26919571983257, 1317552703000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969573, 10.893262, 2.993931152453186, 354.0003407319675, 1317552705000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969535, 10.893334999999999, 2.9939898524920463, 353.6066204491938, 1317552709000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969497999999994, 10.89341, 2.993940616318989, 353.60229236543745, 1317552711000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969466999999995, 10.89349, 1.9960298392301392, 353.09279463052843, 1317552715000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969418, 10.89359, 1.9960972505128296, 354.6778570820155, 1317552718000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969395, 10.893642, 4.5, 354.91940712737585, 1317552721000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969356999999995, 10.893715, 4.5, 354.9195384808613, 1317552723000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.96933, 10.8937899, 4.5, 354.91904169480694, 1317552727000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.9693, 10.893862, 4.5, 354.91980993151304, 1317552729000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969265, 10.8939299, 4.5, 354.91980993151304, 1317552733000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969223, 10.893975, 4.5, 354.91980993151304, 1317552735000L, true));
        this.allPoints.add(new SimulatorWindDTO(53.969165, 10.893908, 4.5, 354.91384735818013, 1317552741000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969134999999994, 10.893839999999999, 1.9960981907199515, 348.42245445741435, 1317552745000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969093, 10.893718, 4.5, 354.9163400714003, 1317552748000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.969066999999995, 10.893665, 4.5, 354.91718237545496, 1317552751000L, true));
        this.allPoints.add(new SimulatorWindDTO(53.969017, 10.893616999999999, 4.5, 354.9159410187896, 1317552753000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.968972, 10.893557, 4.5, 354.91506069534375, 1317552757000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.968922, 10.893502, 4.5, 354.91524821759947, 1317552759000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.96888, 10.89344, 4.5, 354.9166854707072, 1317552763000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.968833, 10.893378, 4.5, 354.91771569148113, 1317552765000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.968792, 10.893322, 4.5, 354.9172604608012, 1317552769000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.968747, 10.893265, 4.5, 354.91667030891824, 1317552771000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.9687, 10.893207, 4.5, 354.9167062101114, 1317552774000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.968635, 10.893132, 4.5, 354.9168595934945, 1317552778000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.968607999999996, 10.893092, 1.9960522689645877, 350.0084083489642, 1317552781000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.96856, 10.89303, 1.9960049885015156, 349.84493107063867, 1317552783000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.968512999999994, 10.892965, 1.9960504668087709, 350.82419899674704, 1317552787000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.968467999999994, 10.892908, 1.996064961309818, 351.45110457463704, 1317552789000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.968424999999996, 10.892858, 1.9959876230098303, 352.5524384699868, 1317552793000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.968382999999996, 10.892807999999999, 1.9960192928327019, 353.65793707659947, 1317552795000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.968342, 10.892755, 1.9960278346932239, 355.2828843356294, 1317552798000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.968258, 10.892648, 1.9960824535972062, 354.7231263360055, 1317552804000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.968199999999996, 10.892577, 2.9931037531700673, 352.44770990541184, 1317552808000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.968173, 10.892545, 2.9931565364543964, 352.1224869251552, 1317552811000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.968137, 10.892498, 2.993097584404663, 352.79414066706164, 1317552813000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.968101999999995, 10.89245, 2.9927577733474813, 352.9048461112329, 1317552816000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.968075, 10.892413, 2.9925362333590697, 352.98479063979744, 1317552819000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.968033, 10.892358, 1.995946971363841, 354.2816882732099, 1317552823000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.967997999999994, 10.892313, 1.9959411192574583, 354.02001134693734, 1317552825000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.967957, 10.892253, 1.9959355367190348, 354.1644873724846, 1317552828000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.967883, 10.892177, 1.9959290166780002, 354.66718538526885, 1317552832000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.967782, 10.892026999999999, 2.9930287222267062, 352.3704208044175, 1317552841000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.967728, 10.891955, 2.992835485084626, 353.076715464954, 1317552844000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.967704999999995, 10.891917999999999, 2.9926339861674203, 353.83097223824956, 1317552846000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.96767, 10.891872, 3.9894798861062486, 353.0568578178748, 1317552849000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.967636999999996, 10.891824999999999, 3.99029623526274, 351.6913035617712, 1317552852000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.967605, 10.891788, 3.9900176540006194, 350.92699412051144, 1317552855000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.967572, 10.891753, 3.98963216150572, 350.9080630238719, 1317552858000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.967544999999994, 10.891708, 3.9893211832817705, 351.0195938755182, 1317552861000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.967513, 10.891667, 3.9889943435577653, 352.9769940529611, 1317552864000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.967468, 10.891613, 2.992841738848901, 354.1431350760541, 1317552868000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.96745, 10.891587, 2.992711364874862, 354.58350034143956, 1317552870000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.967414999999995, 10.891544999999999, 2.991981133988467, 353.2220244719365, 1317552873000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.967385, 10.891498, 3.99047116788407, 352.8497907252422, 1317552876000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.967358, 10.891449999999999, 3.9898675899461913, 352.61978997643286, 1317552879000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.967323, 10.891392999999999, 2.993610923200435, 352.94672187327166, 1317552882000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.967254999999994, 10.891278, 3.9906552072220673, 354.34816221096264, 1317552888000L, true));
        this.allPoints.add(new SimulatorWindDTO(53.967197, 10.89124, 4.980280822647906, 7.502663882083886, 1317552892000L, true));
        this.allPoints.add(new SimulatorWindDTO(53.967175, 10.891257, 4.981520957089659, 10.772025037312769, 1317552894000L, false));
        this.allPoints.add(new SimulatorWindDTO(53.967152, 10.891295, 5.976765929463788, 14.456567040003916, 1317552897000L, true));
        this.allPoints.add(new SimulatorWindDTO(53.967152, 10.891352999999999, 3.9869520603815034, 19.716634034606393, 1317552901000L, true));
    }

    @Before
    public void initialize() {

        this.loadTravemuendeSetup();

        this.boatClassesIndexes = new HashMap<Integer, String>();
        this.boatClassesIndexes.put(0, "49er");
        this.boatClassesIndexes.put(1, "49er rBethwaite");
        this.boatClassesIndexes.put(2, "49er ORC");
        this.boatClassesIndexes.put(3, "49er STG");
        this.boatClassesIndexes.put(4, "505 STG");

        this.averageWindFlags = new HashMap<Integer, String>();
        this.averageWindFlags.put(0, "default average wind");
        this.averageWindFlags.put(1, "real average wind");

        this.timeStepMillisecondsSizes = new ArrayList<Integer>();
        this.timeStepMillisecondsSizes.add(1000);
        this.timeStepMillisecondsSizes.add(1250);
        this.timeStepMillisecondsSizes.add(1500);
        this.timeStepMillisecondsSizes.add(1750);
        this.timeStepMillisecondsSizes.add(2000);
    }


	@Test
	public void runSimulation() throws ConfigurationException {
		
		for (final Entry<Integer, String> boatClassIndex : this.boatClassesIndexes.entrySet()) {
            for (final Entry<Integer, String> averageWindFlag : this.averageWindFlags.entrySet()) {
                for (final int timeStepMilliSeconds : this.timeStepMillisecondsSizes) {
                    this.getTotalTime(boatClassIndex.getKey(), averageWindFlag.getKey(), timeStepMilliSeconds);
                }
            }
        }
		
	}
	
    private void getTotalTime(final int boatClassIndex, final int useRealAverageWindSpeed, final int stepDurationMilliseconds) throws ConfigurationException {

        final SimulatorServiceImpl simulatorService = new SimulatorServiceImpl();
        final RequestTotalTimeDTO requestData = new RequestTotalTimeDTO(boatClassIndex, this.allPoints, this.turnPoints, useRealAverageWindSpeed == 1,
                stepDurationMilliseconds, true);
        final ResponseTotalTimeDTO receiveData = simulatorService.getTotalTime_new(requestData);

        final SpeedWithBearing averageWind = simulatorService.getAverageWind();
        final double stepSizeMeters = simulatorService.getStepSizeMeters();
        int stepIndex = 0;
        
        String event = "unknown";
        String race = "unknown";
        String competitor = "unknown";
        int leg = 1;
        double gps_time = 0;
        double pd1_time = 0;
        double pd2_time = 0;
        double pd3_time = 0;
        double wind_avg_speed = 0;
        double boat_avg_speed = 0;
        double boat_avg_bearing = 0;

        System.err.println("==================================================");
        System.err.println(this.boatClassesIndexes.get(boatClassIndex));
        System.err.println("average wind speed = " + averageWind.getKnots() + " knots, bearing = " + averageWind.getBearing().getDegrees() + " degrees");
        System.err.println("step size = " + stepSizeMeters + " meters");
        stepIndex = 0;
        for (final Quadruple<PositionDTO, PositionDTO, Double, Double> segment : receiveData.segments) {
            System.err.println("segment " + stepIndex + " from [" + segment.getA().latDeg + "," + segment.getA().lngDeg + "] to ["
                    + segment.getB().latDeg + "," + segment.getB().lngDeg + "], length = " + segment.getC() + " meters, time = " + segment.getD() / 1000.
                    + " seconds");
            stepIndex++;
        }
        System.err.println(this.boatClassesIndexes.get(boatClassIndex) + ", " + this.averageWindFlags.get(useRealAverageWindSpeed) + ", "
                + stepDurationMilliseconds + "milliseconds timestep, total time: " + receiveData.totalTimeSeconds
                + " seconds");
        System.err.println("==================================================");
    }

}
