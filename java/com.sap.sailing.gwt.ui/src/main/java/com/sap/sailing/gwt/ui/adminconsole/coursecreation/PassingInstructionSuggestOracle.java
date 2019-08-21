package com.sap.sailing.gwt.ui.adminconsole.coursecreation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.suggestion.AbstractListSuggestOracle;

/**
 * Suggest oracle for use in {@link ControlPointEditDialog} which oracles the available passing instructions.
 */
public class PassingInstructionSuggestOracle extends AbstractListSuggestOracle<PassingInstruction> {

    private final Collection<PassingInstruction> allPassingInstructions;

    public PassingInstructionSuggestOracle() {
        allPassingInstructions = Arrays.asList(PassingInstruction.relevantValues());
    }


    /**
     * Clears the oracle suggestions, adds all existing passing instructions and finally removes the existing passing
     * instructions from the oracle.
     */
    public void resetAndRemoveExistingPassingInstructions(Iterable<String> passingInstructions) {
        Collection<PassingInstruction> users = new ArrayList<>(allPassingInstructions);
        users = users.stream().filter(u -> !Util.contains(passingInstructions, u.name())).collect(Collectors.toList());
        super.setSelectableValues(users);
    }

    /**
     * @returns a {@link PassingInstruction}-object from the current selectable values of this oracle, which is
     *          associated with the given name.
     */
    public PassingInstruction fromString(final String passingInstructionName) {
        for (PassingInstruction instruction : this.getSelectableValues()) {
            if (instruction.name().equals(passingInstructionName)) {
                return instruction;
            }
        }
        return null;
    }

    @Override
    protected String createSuggestionKeyString(PassingInstruction value) {
        return value.name();
    }

    @Override
    protected String createSuggestionAdditionalDisplayString(PassingInstruction value) {
        return null;
    }

    @Override
    protected Iterable<String> getMatchingStrings(PassingInstruction value) {
        return this.getSelectableValues().stream().map(r -> createSuggestionKeyString(r)).collect(Collectors.toList());
    }
}
