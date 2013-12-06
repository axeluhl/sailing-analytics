package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.CountryCode;
import com.sap.sailing.domain.common.CountryCodeFactory;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.domain.common.impl.HtmlColor;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * The competitors produced by this dialog will have a <code>null</code>
 * {@link CompetitorDTO#getTwoLetterIsoCountryCode() twoLetterIsoCountryCode} and a <code>null</code>
 * {@link CompetitorDTO#getCountryName() countryName} because all of these can be derived from a valid
 * {@link CompetitorDTO#getThreeLetterIocCountryCode() threeLetterIocCountryCode}.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class CompetitorEditDialog extends DataEntryDialog<CompetitorDTO> {
    private final CompetitorDTO competitorToEdit;
    private final TextBox name;
    private final TextBox displayColorTextBox;
    private final ListBox threeLetterIocCountryCode;
    private final TextBox sailId;
    private final StringMessages stringMessages;
    
    public CompetitorEditDialog(final StringMessages stringMessages, CompetitorDTO competitorToEdit,
            DialogCallback<CompetitorDTO> callback) {
        super(stringMessages.editCompetitor(), stringMessages.editCompetitor(), stringMessages.ok(), stringMessages
                .cancel(), new Validator<CompetitorDTO>() {
                    @Override
                    public String getErrorMessage(CompetitorDTO valueToValidate) {
                        String result = null;
                        if (valueToValidate.getName() == null || valueToValidate.getName().isEmpty()) {
                            result = stringMessages.pleaseEnterAName();
                        } else if (valueToValidate.getSailID() == null || valueToValidate.getSailID().isEmpty()) {
                            result = stringMessages.pleaseEnterASailNumber();
                        } else if(valueToValidate.getDisplayColor() != null) {
                            String displayColor = valueToValidate.getDisplayColor();
                            try {
                                new HtmlColor(displayColor);
                            } catch (IllegalArgumentException e) {
                                result = "The display color must be in HTML color format, e.g. #ff0000";
                            }
                        }
                        return result;
                    }
                }, /* animationEnabled */true, callback);
        this.stringMessages = stringMessages;
        this.competitorToEdit = competitorToEdit;
        this.name = createTextBox(competitorToEdit.getName());
        this.displayColorTextBox = createTextBox(competitorToEdit.getDisplayColor()); 
        this.threeLetterIocCountryCode = createListBox(/* isMultipleSelect */ false);
        CountryCodeFactory ccf = CountryCodeFactory.INSTANCE;
        int i=0;
        List<CountryCode> ccs = new ArrayList<CountryCode>();
        Util.addAll(ccf.getAll(), ccs);
        ccs.add(null); // representing no nationality (NONE / white flag)
        Collections.sort(ccs, new Comparator<CountryCode>() {
            @Override
            public int compare(CountryCode o1, CountryCode o2) {
                return Util.compareToWithNull(o1 == null ? null : o1.getThreeLetterIOCCode(), o2 == null ? null : o2.getThreeLetterIOCCode());
            }
        });
        for (CountryCode cc : ccs) {
            if (cc == null) {
                this.threeLetterIocCountryCode.addItem("", ""); // the NONE country code that uses the empty, white flag
                if (competitorToEdit.getThreeLetterIocCountryCode() == null || competitorToEdit.getThreeLetterIocCountryCode().isEmpty()) {
                    this.threeLetterIocCountryCode.setSelectedIndex(i);
                }
                i++;
            } else if (cc.getThreeLetterIOCCode() != null) {
                this.threeLetterIocCountryCode.addItem(cc.getThreeLetterIOCCode() + " " + cc.getName(), cc.getThreeLetterIOCCode());
                if (cc.getThreeLetterIOCCode().equals(competitorToEdit.getThreeLetterIocCountryCode())) {
                    this.threeLetterIocCountryCode.setSelectedIndex(i);
                }
                i++;
            }
        }
        this.sailId = createTextBox(competitorToEdit.getSailID());
    }

    
    @Override
    public void show() {
        super.show();
        name.setFocus(true);
    }

    @Override
    protected CompetitorDTO getResult() {
        CompetitorDTO result = new CompetitorDTOImpl(name.getText(), displayColorTextBox.getText(),
                /* twoLetterIsoCountryCode */ null,
                threeLetterIocCountryCode.getValue(threeLetterIocCountryCode.getSelectedIndex()),
                /* countryName */null, sailId.getText(), competitorToEdit.getIdAsString(),
                competitorToEdit.getBoatClass());
        return result;
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(4, 2);
        result.setWidget(0, 0, new Label(stringMessages.name()));
        result.setWidget(0, 1, name);
        result.setWidget(1, 0, new Label(stringMessages.sailNumber()));
        result.setWidget(1, 1, sailId);
        result.setWidget(2, 0, new Label(stringMessages.nationality()));
        result.setWidget(2, 1, threeLetterIocCountryCode);
        result.setWidget(3, 0, new Label(stringMessages.color()));
        result.setWidget(3, 1, displayColorTextBox);
        return result;
    }

}
