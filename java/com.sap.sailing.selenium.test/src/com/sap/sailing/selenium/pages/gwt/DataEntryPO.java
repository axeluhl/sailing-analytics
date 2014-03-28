package com.sap.sailing.selenium.pages.gwt;

import java.util.List;

import org.openqa.selenium.By.ByTagName;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.ClickAction;
import org.openqa.selenium.interactions.CompositeAction;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.KeyDownAction;
import org.openqa.selenium.interactions.KeyUpAction;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.interactions.MoveToOffsetAction;

import org.openqa.selenium.internal.Locatable;

import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.pages.common.CSSHelper;

public class DataEntryPO extends CellTableRowPO {
    protected static final String CELL_TAG_NAME = "td"; //$NON-NLS-1$
    
    @FindBy(how = ByTagName.class, using = CELL_TAG_NAME)
    private List<WebElement> columns;
    
    public DataEntryPO(CellTablePO<?> table, WebElement element) {
        super(table, element);
    }
    
    public DataEntryPO() {
        super();
    }
    
    public String getColumnContent(int column) {
        return this.columns.get(column).getText();
    }
    
    public String getColumnContent(String name) {
        return getColumnContent(this.table.getColumnIndex(name));
    }
    
    public boolean isSelected() {
        return CSSHelper.hasCSSClass((WebElement) this.context, CellTablePO.SELECTED_ROW_CSS_CLASS);
    }
    
    public void select() {
        Action action = getSelectAction();
        action.perform();
    }
    
    public void deselect() {
        if(isSelected()) {
            Action action = getModifiedSelectAction();
            action.perform();
        }
    }
    
    public void appendToSelection() {
        if(!isSelected()) {
            Action action = getModifiedSelectAction();
            action.perform();
        }
    }
    
    protected Action getSelectAction() {
        HasInputDevices devices = (HasInputDevices) this.driver;
        Mouse mouse = devices.getMouse();
        Locatable locatable = getLocatableForSelect();
        
        CompositeAction action = new CompositeAction();
        action.addAction(new MoveToOffsetAction(mouse, locatable, 1, 1));
        action.addAction(new ClickAction(mouse, null));
        
        return action;
    }
    
    protected Locatable getLocatableForSelect() {
        return (Locatable) (this.columns.isEmpty() ? this.context : this.columns.get(0));
    }
    
    protected Action getModifiedSelectAction() {
        CompositeAction action = getModifiedCompositeActionAction();
        action.addAction(getSelectAction());
        
        return action;
    }
    
    protected CompositeAction getModifiedCompositeActionAction() {
        HasInputDevices devices = (HasInputDevices) this.driver;
        
        final Mouse mouse = devices.getMouse();
        final Keyboard keyboard = devices.getKeyboard();
        
        return new CompositeAction() {
            @Override
            public void perform() {
                Action pressControl = new KeyDownAction(keyboard, mouse, Keys.CONTROL);
                pressControl.perform();
                
                super.perform();
                
                Action releaseControl = new KeyUpAction(keyboard, mouse, Keys.CONTROL);
                releaseControl.perform();
            }
        };
    }
}
