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
    private RaceStateEvent raceStateEvent;

    public RaceStateEventImplParcel(RaceStateEvent raceStateEvent) {
        this.raceStateEvent = raceStateEvent;
    }

    protected RaceStateEventImplParcel(Parcel in) {
        TimePoint timePoint = new MillisecondsTimePoint(in.readLong());
        RaceStateEvents eventName = RaceStateEvents.valueOf(in.readString());
        raceStateEvent = new RaceStateEventImpl(timePoint, eventName);
    }

    public TimePoint getTimePoint() {
        return raceStateEvent.getTimePoint();
    }

    public RaceStateEvents getEventName() {
        return raceStateEvent.getEventName();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(raceStateEvent.getTimePoint().asMillis());
        dest.writeString(raceStateEvent.getEventName().name());
    }
}
