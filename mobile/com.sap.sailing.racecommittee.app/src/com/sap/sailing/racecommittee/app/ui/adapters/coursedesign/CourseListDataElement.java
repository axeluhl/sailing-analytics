package com.sap.sailing.racecommittee.app.ui.adapters.coursedesign;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.racecommittee.app.domain.RoundingDirection;

public class CourseListDataElement {
	
	private Mark leftMark;
	private Mark rightMark;
	private RoundingDirection roundingDirection;
	
	public Mark getLeftMark() {
		return leftMark;
	}
	
	public void setLeftMark(Mark mark) {
		leftMark = mark;
	}
	
	public Mark getRightMark() {
		return rightMark;
	}
	
	public void setRightMark(Mark mark) {
		rightMark = mark;
	}
	
	public RoundingDirection getRoundingDirection() {
		return roundingDirection;
	}
	
	public void setRoundingDirection(RoundingDirection direction) {
		roundingDirection = direction;
	}
}
