package com.sap.sailing.racecommittee.app.domain.impl;

import android.os.Parcel;
import android.os.Parcelable;

import com.sap.sailing.domain.abstractlog.race.state.RaceStateEvent;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateEventImpl;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateEvents;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RaceStateEventImplParcel implements Parcelable {

    public static final Creator<RaceStateEventImplParcel> CREATOR = new Creator<RaceStateEventImplParcel>() {
        @Override
        public RaceStateEventImplParcel createFromParcel(Parcel in) {
            return new RaceStateEventImplParcel(in);
        }

        @Override
        public RaceStateEventImplParcel[] newArray(int size) {
            return new RaceStateEventImplParcel[size];
        }
    };
    private long timePoint;
    private String eventName;

    public RaceStateEventImplParcel(RaceStateEvent raceStateEvent) {
        this.timePoint = raceStateEvent.getTimePoint().asMillis();
        this.eventName = raceStateEvent.getEventName().name();
    }

    protected RaceStateEventImplParcel(Parcel in) {
        timePoint = in.readLong();
        eventName = in.readString();
    }

    public TimePoint getTimePoint() {
        return new MillisecondsTimePoint(timePoint);
    }

    public RaceStateEvents getEventName() {
        return RaceStateEvents.valueOf(eventName);
    }

    public RaceStateEvent getRaceStateEvent() {
        return new RaceStateEventImpl(getTimePoint(), getEventName());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timePoint);
        dest.writeString(eventName);
    }

    @Override
    public String toString() {
        return "RaceStateEventImpl [timePoint=" + getTimePoint() + ", eventName=" + getEventName() + "]";
    }

}
