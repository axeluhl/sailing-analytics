package com.sap.sailing.server.trackfiles.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.Test;

import com.sap.sailing.domain.common.sensordata.BravoExtendedSensorDataMetadata;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;

public class BravoExtendedDataImportTest extends AbstractBravoDataImportTest {
    
    @Override
    protected Map<String, Integer> getColumnData() {
        return BravoExtendedSensorDataMetadata.getColumnNamesToIndexInDoubleFix();
    }
    
    @Override
    protected int getTrackColumnCount() {
        return BravoExtendedSensorDataMetadata.getTrackColumnCount();
    }
    
    @Test
    public void testDefaultHeaderNoDataImport() throws FormatNotSupportedException, IOException {
        testImport(ImportData.DUMMY_DEFAULT_HEADER_NO_DATA);
    }
    
    @Test
    public void testDefaultHeaderOneLineImport() throws FormatNotSupportedException, IOException {
        testImport(ImportData.DUMMY_DEAFULT_HEADER_ONE_LINE);
    }
    
    @Test
    public void testRandomHeaderOneLineImport() throws FormatNotSupportedException, IOException {
        testImport(ImportData.DUMMY_RANDOM_HAEDER_ONE_LINE);
    }
    
    private enum ImportData implements ImportDataDefinition {
        DUMMY_DEFAULT_HEADER_NO_DATA(0, 0) {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(HEADER_ORDER_DEFAULT.getBytes(StandardCharsets.UTF_8));
            }
        },
        DUMMY_DEAFULT_HEADER_ONE_LINE(1, 1) {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream((HEADER_ORDER_DEFAULT + DUMMY_CONTENT).getBytes(StandardCharsets.UTF_8));
            }
        },
        DUMMY_RANDOM_HAEDER_ONE_LINE(1, 1) {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream((HEADER_ORDER_RANDOM + DUMMY_CONTENT).getBytes(StandardCharsets.UTF_8));
            }
        };
        
        private final int expectedFixesCount;
        private final int expectedFixesConsolidated;

        private ImportData(int expectedFixesCount, int expectedFixesConsolidated) {
            this.expectedFixesCount = expectedFixesCount;
            this.expectedFixesConsolidated = expectedFixesConsolidated;
        }
        
        @Override
        public int getExpectedFixesCount() {
            return expectedFixesCount;
        }
        
        @Override
        public int getExpectedFixesConsolidated() {
            return expectedFixesConsolidated;
        }
    }
    
    private static final String HEADER_ORDER_DEFAULT = "jjlDATE\tjjlTIME\tEpoch\tMagneticVariation\tCurrentSpeed\t"
            + "CurrentDirection\tWindGradient\tLat\tLon\tCOG\tSOG\tHdg\tHeel\tTrim\tFlightPathAngle\tRollRate\t"
            + "PitchRate\tYawRate\tAccelX\tAccelY\tAccelZ\tHeaveSpeed\tHeave\tRideHeightPort\tRideHeightStbd\t"
            + "RideHeight\tBS\tLwy\tCse\tSOW\tAWA\tAWS\tTWA\tTWS\tTWD\tUpwashAngle\tUpwashSpeed\tAdjWindAngle\t"
            + "AdjWindSpeed\tVMG\tMastRotation\tDaggerBoardRakeAnglePort\tRudderRakeAnglePort\tDaggerBoardRakeAngleStbd\t"
            + "RudderRakeAngleStbd\tTargetTWA\tTargetVMG\tTargetBS\tPolarBS\tBsPercentage\tVmgPercentage\t\n";
    
    private static final String HEADER_ORDER_RANDOM = "jjlDATE\tjjlTIME\tEpoch\tMagneticVariation\tCurrentSpeed\t"
            + "CurrentDirection\tWindGradient\tLat\tLon\tCOG\tSOG\tHdg\tHeel\tTrim\tFlightPathAngle\tRollRate\t"
            + "AdjWindSpeed\tVMG\tMastRotation\tDaggerBoardRakeAnglePort\tRudderRakeAnglePort\tDaggerBoardRakeAngleStbd\t"
            + "RideHeight\tBS\tLwy\tCse\tSOW\tAWA\tAWS\tTWA\tTWS\tTWD\tUpwashAngle\tUpwashSpeed\tAdjWindAngle\t"
            + "PitchRate\tYawRate\tAccelX\tAccelY\tAccelZ\tHeaveSpeed\tHeave\tRideHeightPort\tRideHeightStbd\t"
            + "RudderRakeAngleStbd\tTargetTWA\tTargetVMG\tTargetBS\tPolarBS\tBsPercentage\tVmgPercentage\t\n";
    
    private static final String DUMMY_CONTENT = "20170721.000000\t144509.020891\t1500648308988.000000\t0.000000\t"
            + "0.000000\t0.000000\t0.100000\t4121.750774\t211.855241\t267.200000\t12.108000\t274.401591\t8.293443\t"
            + "1.381434\t-0.607992\t4.466850\t2.295866\t4.974762\t0.021349\t0.049948\t-0.003009\t-0.059050\t0.564561\t"
            + "-0.142641\t0.661195\t0.640803\t11.024831\t4.831789\t274.531789\t11.415373\t-28.235512\t12.815985\t"
            + "-88.908514\t6.063496\t183.968325\t0.000000\t0.000000\t0.000000\t0.000000\t-0.080389\t-46.507845\t"
            + "-0.301864\t0.915471\t0.737711\t5.518818\t60.000000\t4.049104\t8.098208\t11.025103\t134.646042\t0.684715\t\n";

}
