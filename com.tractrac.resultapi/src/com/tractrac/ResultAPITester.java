/*
 * Demonstration of the TracTrac Result API 
 * 
 */
package com.tractrac;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.tractrac.ResultAPI.LiveResult;
import com.tractrac.ResultAPI.LiveResultItem;
import com.tractrac.ResultAPI.MarkResult;
import com.tractrac.ResultAPI.MarkResultItem;
import com.tractrac.ResultAPI.PerLegResult;
import com.tractrac.ResultAPI.ResultGenerator;

/**
 *
 * @author Lasse Staffensen, TracTrac
 */
public class ResultAPITester {


  // Set the time zone to UTC to correspond with the server  
  static {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }
 /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            
            // Use the start-time of program to make a simply timer
            long startTimeReal = System.currentTimeMillis();
            // Set the start-time of the race
            Calendar cal = new GregorianCalendar(111, 3, 01, 00, 20, 00);
            long StartTimeRace = cal.getTimeInMillis();
            // Set the speed up of the demonstration
            int speedup = 10;
            
            // Instance the resultgenerator using a URL for a specific race
            ResultGenerator generator = new ResultGenerator(new URL("http://germanmaster.traclive.dk/events/event_20110308_SAPWorldCh/clientparams.php?event=event_20110308_SAPWorldCh&race=5ffe619e-4962-11e0-8236-406186cbf87c&ci=&minimize=&LiveDelaySecs="));
            
            // Start out not using custom wind
            boolean useWindDirection = false;
            double windDirectionDegrees = 0;
            
            // Set up a simply text-based GUI
            while (true) {
              System.out.print("Press L for live result, P for per leg result, W to set wind, Q to quit.\n:");

              //  open up standard input
              BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

              String command = null;

              try {
                 command = br.readLine();
                 // Calculate time according to our simple timer
                 long time = (System.currentTimeMillis() - startTimeReal) * speedup + StartTimeRace;
                 System.out.println("Current time: " + new SimpleDateFormat().format(new Date(time)));
                 if (useWindDirection) System.out.println("Using wind directino: " + windDirectionDegrees);
                 else System.out.println("Not using wind direction");
                 if (command.equalsIgnoreCase("Q")) System.exit(0);
                 // Check of wind should be changed
                 if (command.equalsIgnoreCase("w")) {
                    System.out.println("\nlEnter wind. N for none or 0-359 to set wind direction.\n:");
                    command = br.readLine();
                    if (command.equalsIgnoreCase("n")) {
                        useWindDirection = false;
                        windDirectionDegrees = 0;
                    } else {
                        try {
                            windDirectionDegrees = Double.valueOf(command).doubleValue();
                            useWindDirection = true;
                        } catch (Exception e) {
                            System.out.println("Unable to interpret wind direction");
                        }
                    }

                 }
                 // Recalculate results - using or not using custom wind
                 if (useWindDirection) generator.recalcuateWithWind(time, windDirectionDegrees);
                 else generator.recalculate(time);
                 
                 // Generator the live results lists
                 if (command.equalsIgnoreCase("L")) {
                     // Get a list for each class
                     for(LiveResult liveResult : generator.getLiveResults()) {
                         System.out.println("\tClass: " + liveResult.getClassName());
                         // Get a result item for each competitor
                         for (LiveResultItem item : liveResult.values()) {
                             System.out.println("\t\tRank: " + item.getRank());
                             System.out.println("\t\t\tName: " + item.getName());
                             System.out.println("\t\t\tShort: " + item.getShortName());
                             System.out.println("\t\t\tTeam: " + item.getTeam());
                             System.out.println("\t\t\tDescription: " + item.getDescription());
                             System.out.println("\t\t\tNationality: " + item.getNationality());
                             System.out.println("\t\t\tAfter at finish: " + item.getAfterAtFinish());
                             System.out.println("\t\t\tDistance to next mark: " + item.getDistNextMark());
                             System.out.println("\t\t\tDistance to finish: " + item.getDistToFinish());
                             System.out.println("\t\t\tDistance behind leader: " + item.getDistanceBehind());
                             System.out.println("\t\t\tHeading: " + item.getHeading());
                             System.out.println("\t\t\tLatitude: " + item.getLatitude());
                             System.out.println("\t\t\tLongitude: " + item.getLongitude());
                             System.out.println("\t\t\tSpeed: " + item.getSpeed());
                             System.out.println("\t\t\tFinished: " + item.isHasFinished());
                         }
                     } 
                 }
                 
                 // Generator the per leg result list
                 if (command.equalsIgnoreCase("P")) {
                     // Get at per leg result per class
                     for (PerLegResult plr : generator.getPerLegResults()) {
                         System.out.println("\tClass: " + plr.getClassName());
                         int m = 0;
                         // Get a result list per mark
                         for (MarkResult markResult : plr.values()) {
                            System.out.println("\tMark: " + markResult.getMarkName() + " - " + (m+1) + "/" + plr.getMarkCount());
                            m++;
                            // Get a result item per competitor
                            for (MarkResultItem item : markResult.values()) {
                             System.out.println("\t\tRank: " + item.getRank());
                             System.out.println("\t\t\t\tName: " + item.getName());
                             System.out.println("\t\t\t\tShort: " + item.getShortName());
                             System.out.println("\t\t\t\tTeam: " + item.getTeam());
                             System.out.println("\t\t\t\tDescription: " + item.getDescription());
                             System.out.println("\t\t\t\tNationality: " + item.getNationality());
                             System.out.println("\t\t\t\tAfter: " + item.getAfter());
                             System.out.println("\t\t\t\tDeltarank: " + item.getDeltarank());
                             System.out.println("\t\t\t\tLeg rank: " + item.getLegrank());
                             System.out.println("\t\t\t\tLeg time: " + item.getLegtime());
                             System.out.println("\t\t\t\tSOG: " + item.getSOG());
                             System.out.println("\t\t\t\tTop speed: " + item.getTopspeed());
                             System.out.println("\t\t\t\tTravelled distance: " + item.getTraveledDistance());
                             System.out.println("\t\t\t\tVMG: " + item.getVMG());
                                
                            }
                         }
                         
                        }
                 }
                 
              } catch (IOException ioe) {
                 System.out.println("IO error trying to read stdin");
                 System.exit(1);
              }
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
