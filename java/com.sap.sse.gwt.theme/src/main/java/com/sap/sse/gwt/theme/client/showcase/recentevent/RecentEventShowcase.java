package com.sap.sse.gwt.theme.client.showcase.recentevent;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.theme.client.component.recentevent.RecentEvent;
import com.sap.sse.gwt.theme.client.component.recentevent.RecentEventData;

public class RecentEventShowcase extends Composite {

    private static RecentEventShowcaseUiBinder uiBinder = GWT.create(RecentEventShowcaseUiBinder.class);

    interface RecentEventShowcaseUiBinder extends UiBinder<Widget, RecentEventShowcase> {
    }

    @UiField(provided = true)
    RecentEvent finishedItem;

    @UiField(provided = true)
    RecentEvent liveItem;

    public RecentEventShowcase() {
        finishedItem = new RecentEvent(new RecentEventData() {

            @Override
            public boolean isLive() {
                return false;
            }

            @Override
            public String getVenue() {
                return "Venue1";
            }

            @Override
            public String getLocation() {
                return "Loc1";
            }

            @Override
            public Date getEventStart() {
                return new Date();
            }

            @Override
            public String getEventName() {
                return "SomeName";
            }

            @Override
            public String getEventImageUrl() {

                return "http://demotivators.despair.com/demotivational/mistakesdemotivator.jpg";
            }

            @Override
            public Date getEventEnd() {
                return new Date();
            }

            @Override
            public Command getCommand() {
                return new Command() {

                    @Override
                    public void execute() {
                        Window.alert("done");
                    }
                };
            }
        });

        liveItem = new RecentEvent(new RecentEventData() {

            @Override
            public boolean isLive() {
                return true;
            }

            @Override
            public String getVenue() {
                return "Venue2";
            }

            @Override
            public String getLocation() {
                return "Loc2";
            }

            @Override
            public Date getEventStart() {
                return new Date();
            }

            @Override
            public String getEventName() {
                return "SomeName";
            }

            @Override
            public String getEventImageUrl() {

                return "http://demotivators.despair.com/demotivational/mistakesdemotivator.jpg";
            }

            @Override
            public Date getEventEnd() {
                return new Date();
            }

            @Override
            public Command getCommand() {
                return new Command() {

                    @Override
                    public void execute() {
                        Window.alert("done");
                    }
                };
            }
        });

        initWidget(uiBinder.createAndBindUi(this));
    }
}
