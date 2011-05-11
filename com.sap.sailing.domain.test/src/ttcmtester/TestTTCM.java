/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ttcmtester;

import com.tractrac.clientmodule.ControlPoint;
import com.tractrac.clientmodule.Event;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.RaceCompetitor;
import com.tractrac.clientmodule.Route;
import com.tractrac.clientmodule.data.CompetitorPositionRawData;
import com.tractrac.clientmodule.data.ControlPointPositionData;
import com.tractrac.clientmodule.data.DataController;
import com.tractrac.clientmodule.data.ICallbackData;
import com.tractrac.clientmodule.data.MarkPassingsData;
import com.tractrac.clientmodule.data.MessageData;
import com.tractrac.clientmodule.data.RouteData;
import com.tractrac.clientmodule.data.StartStopTimesData;
import com.tractrac.clientmodule.setup.Info;
import com.tractrac.clientmodule.setup.KeyValue;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.TimeZone;



/**
 *
 * @author Lasse Staffensen
 */
public class TestTTCM implements com.tractrac.clientmodule.data.DataController.Listener {



  static {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  private static void show(Object obj) {
    System.out.println(Thread.currentThread().getName() + ": " + obj);
  }

  public void liveDataConnected() {
    show("Live data connected");
  }

  public void liveDataDisconnected() {
    show("Live data disconnected");
  }

  public void storedDataBegin() {
    show("Fetching stored data");
  }

  public void storedDataEnd() {
    show("Done fetching stored data");
  }

  public void storedDataProgress(float progress) {
    show("Stored data progress: " + (int) (100*progress) + "%");
  }

  public void stopped() {
    show("Stopped");
  }

  private ICallbackData<ControlPoint, ControlPointPositionData> receiveControlPoint =
            new ICallbackData<ControlPoint, ControlPointPositionData>() {
              public void gotData(ControlPoint tracked, ControlPointPositionData record) {
                show(record);
              }
            };

  private ICallbackData<Event, StartStopTimesData> receiveEventStartStop =
            new ICallbackData<Event, StartStopTimesData>() {
              public void gotData(Event tracked, StartStopTimesData record) {
                show(record);
              }
            };

  private ICallbackData<Event, MessageData> receiveEventMessage =
            new ICallbackData<Event, MessageData>() {
              public void gotData(Event tracked, MessageData record) {
                show(record);
              }
            };

  private ICallbackData<RaceCompetitor, CompetitorPositionRawData> receiveRaw =
            new ICallbackData<RaceCompetitor, CompetitorPositionRawData>() {
              public void gotData(RaceCompetitor tracked, CompetitorPositionRawData record) {
                show(record);
              }
            };

  private ICallbackData<Race, StartStopTimesData> receiveRaceStartStop =
            new ICallbackData<Race, StartStopTimesData>() {
              public void gotData(Race tracked, StartStopTimesData record) {
                show(record);
              }
            };

  private ICallbackData<Race, MessageData> receiveRaceMessage =
            new ICallbackData<Race, MessageData>() {
              public void gotData(Race tracked, MessageData record) {
                show(record);
              }
            };

  private ICallbackData<RaceCompetitor, MarkPassingsData> receivePassings =
            new ICallbackData<RaceCompetitor, MarkPassingsData>() {
              public void gotData(RaceCompetitor tracked, MarkPassingsData record) {
                show(record);
              }
            };

  private ICallbackData<Route, RouteData> receiveRoute =
            new ICallbackData<Route, RouteData>() {
              public void gotData(Route tracked, RouteData record) {
                show(record);
              }
            };

  private void addSubscriptions(Event event, DataController controller) {
    long fromTime = 0;
    controller.add(ControlPointPositionData.subscribe(event, receiveControlPoint, fromTime));
    controller.add(StartStopTimesData.subscribeEvent(event, receiveEventStartStop));
    controller.add(MessageData.subscribeSequence(event, receiveEventMessage, fromTime));
    for (Race race: event.getRaceList()) {
      controller.add(CompetitorPositionRawData.subscribe(race, receiveRaw, fromTime));
      controller.add(StartStopTimesData.subscribeRace(race, receiveRaceStartStop));
      controller.add(MessageData.subscribeSequence(race, receiveRaceMessage, fromTime));
      controller.add(MarkPassingsData.subscribe(race, receivePassings));
      controller.add(RouteData.subscribe(race, receiveRoute));
    }
  }

  private static URI parseUri(String st) throws URISyntaxException {
    if ("-".equals(st))
      return null;
    if (st.startsWith("file:/"))    // Special case to support relative paths
      return new File(st.substring(6)).toURI();
    return new URI(st);
  }

  public static void main(String[] args) throws MalformedURLException, URISyntaxException, IOException {

    if (args.length != 3) {
      System.out.println("Usage: TTCM param-uri live-uri stored-uri\n\n" +
              "       param-uri    Location of configuration file\n" +
              "         file:/param.txt               Absolute or relative path to file\n" +
              "         http://example.com/param.txt  URL\n\n" +
              "       live-uri     Live data source\n" +
              "         tcp://example.com:1234        Host and port for TCP communication\n" +
              "         -                             Don't receive live data\n\n" +
              "       stored-uri   Stored data source\n" +
              "         tcp://example.com:1234        Host and port for TCP communication\n" +
              "         http://example.com/stored.bin URL\n" +
              "         file:/c:/stored.bin           Absolute or relative path to file\n" +
              "         -                             Don't receive stored data\n");
      System.exit(1);
    }

    URL paramUrl  = parseUri(args[0]).toURL();
    URI liveUri   = parseUri(args[1]);
    URI storedUri = parseUri(args[2]);

    // Read event data from configuration file
    Event event = KeyValue.setup(paramUrl);

    // Show event information, just to check visually
    Info.showEventInfo(event, new Info.LineWriter() {
      public void println(String str) { System.out.println(str); }
    });

    TestTTCM ttcm = new TestTTCM();

    // Initialize data controller using live and stored data sources
    DataController controller = new DataController(liveUri, storedUri, ttcm);

    // Add data subscriptions and corresponding data handlers
    ttcm.addSubscriptions(event, controller);

    // Start live and stored data streams
    Thread io = new Thread(controller, "io");
    io.start();

    if (controller.hasLiveData()) {
      // Go ahead with GUI or other stuff in main thread
      System.out.println("Press key to cancel live data stream");
      System.in.read();
      System.out.println("Cancelling data stream");
      // Stop data streams
      controller.stop(false);
    }

    System.out.println("Waiting for stored data stream to finish");
    try {
      io.join();
    } catch (InterruptedException ex) {}
  }


}