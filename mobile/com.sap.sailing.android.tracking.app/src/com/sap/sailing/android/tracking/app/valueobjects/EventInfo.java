package com.sap.sailing.android.tracking.app.valueobjects;

import android.os.Parcel;
import android.os.Parcelable;

public class EventInfo implements Parcelable {

    public String id;
    public String name;
    public String leaderboardName; // when using join-query
    public String competitorId; // when using join-query
    public String imageUrl;
    public long startMillis;
    public long endMillis;
    public int rowId;
    public String checkinDigest;
    public String server;

    public EventInfo() {

    }

    public EventInfo(Parcel in) {

        id = in.readString();
        name = in.readString();
        leaderboardName = in.readString();
        competitorId = in.readString();
        imageUrl = in.readString();
        startMillis = in.readLong();
        endMillis = in.readLong();
        rowId = in.readInt();
        checkinDigest = in.readString();
        server = in.readString();
    }

    public static final Creator<EventInfo> CREATOR = new Creator<EventInfo>() {
        @Override
        public EventInfo createFromParcel(Parcel in) {
            return new EventInfo(in);
        }

        @Override
        public EventInfo[] newArray(int size) {
            return new EventInfo[size];
        }
    };

    @Override
    public String toString() {
        return "eventName: " + name + ", leaderboardName: " + leaderboardName + ", competitorId: " + competitorId
                + ", eventImageUrl: " + imageUrl + ", eventStartMillis: " + startMillis + ", eventEndMillis: "
                + endMillis + ", eventRowId: " + ", checkinDigest: " + checkinDigest + rowId + ", server: " + server;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(leaderboardName);
        dest.writeString(competitorId);
        dest.writeString(imageUrl);
        dest.writeLong(startMillis);
        dest.writeLong(endMillis);
        dest.writeInt(rowId);
        dest.writeString(checkinDigest);
        dest.writeString(server);
    }
}
