package com.sap.sailing.android.buoy.positioning.app.valueobjects;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.sap.sailing.android.shared.data.AbstractCheckinData;
import com.sap.sailing.android.shared.data.CheckinUrlInfo;
import com.sap.sailing.android.shared.data.LeaderboardInfo;

public class CheckinData extends AbstractCheckinData{
	public String leaderboardName;
	public String deviceUid;
	public String uriString;
	public String checkinDigest;
	public List<MarkInfo> marks;

	public void setCheckinDigestFromString(String checkinString)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(checkinString.getBytes("UTF-8"));
		byte[] digest = md.digest();
		StringBuffer buf = new StringBuffer();
		for (byte byt : digest) {
			buf.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
		}
		checkinDigest = buf.toString();
	}
	
	public LeaderboardInfo getLeaderboard() {
        LeaderboardInfo leaderboard = new LeaderboardInfo();
        leaderboard.name = leaderboardName;
        leaderboard.checkinDigest = checkinDigest;
        return leaderboard;
    }
	
	public CheckinUrlInfo getCheckinUrl(){
        CheckinUrlInfo checkinUrlInfo = new CheckinUrlInfo();
        checkinUrlInfo.urlString = uriString;
        checkinUrlInfo.checkinDigest = checkinDigest;
        return checkinUrlInfo;
    }
}
