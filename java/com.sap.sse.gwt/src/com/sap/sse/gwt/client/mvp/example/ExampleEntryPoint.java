package com.sap.sse.gwt.client.mvp.example;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sse.gwt.client.mvp.AbstractEntryPoint;
import com.sap.sse.gwt.client.mvp.example.hello.HelloPlace;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 * 
 * @author Axel Uhl (d043530)
 */
public class ExampleEntryPoint extends AbstractEntryPoint {
    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        // There are two typical ways in which to tell the concrete views to use, based on a responsive pattern:
        //  1) Use deferred binding in the GWT module's .gwt.xml flie, as in:
        //
        //   <replace-with class="com.hellomvp.client.ClientFactoryImpl">
        //     <when-type-is class="com.hellomvp.client.ClientFactory"/>
        //   </replace-with>
        AppClientFactory clientFactory = GWT.create(AppClientFactory.class);

        //  2) Use some case distinction here in the entry point to figure out device type and instantiate programmatically
        // final AppClientFactory clientFactory;
        // if (I'm on such and such a device) {
        //     clientFactory = new MySuchAndSuchClientFactory();
        // } else {
        //     clientFactory = new MyThisAndThatClientFactory();
        // }
        SimplePanel rootWidgetAndStage = new SimplePanel();
        onModuleLoad(rootWidgetAndStage, rootWidgetAndStage, new HelloPlace("World!"), clientFactory,
                AppPlaceHistoryMapper.class, new AppActivityMapper(clientFactory));
    }
}
