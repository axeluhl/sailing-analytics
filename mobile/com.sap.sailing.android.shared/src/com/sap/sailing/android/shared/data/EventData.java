package com.sap.sailing.android.shared.data;

public class EventData {

    public String serverUrl;
    public Event event;
    public Competitor competitor;

    public class Event {
        public String eventId;
        public String eventStartDate;
        public String eventTitle;
        public EventDays[] eventDays;
        public String eventEndDate;
    }

    public class EventDays {
        public String eventDayEnd;
        public String eventDayStart;
    }

    public class Competitor {
        public String competitorName;
        public String competitorProfileImageUrl;
        public String competitorId;
    }
}
