package com.sap.sailing.domain.queclinkadapter;

import java.text.ParseException;

public interface MessageParser {

    Message parse(String messageIncludingTailCharacter) throws ParseException;

}
