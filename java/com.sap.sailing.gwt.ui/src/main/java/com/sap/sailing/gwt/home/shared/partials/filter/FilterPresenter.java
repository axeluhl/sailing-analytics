package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.List;

import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;
import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sse.common.filter.Filter;

public abstract class FilterPresenter<T> {
    private final Filter<T> alwaysMatchingFilter = new Filter<T>() {
        @Override
        public boolean matches(T object) {
            return true;
        }

        @Override
        public String getName() {
            return "alwaysMatchingFilter";
        }
    };
    private final FilterWidget<T> filterWidget;

    public FilterPresenter(FilterWidget<T> filterWidget) {
        this.filterWidget = filterWidget;
        this.filterWidget.asWidget().setVisible(false);
    }

    protected abstract List<FilterValueChangeHandler<T>> getCurrentValueChangeHandlers();

    protected void addHandler(FilterValueChangeHandler<T> handler) {
        this.filterWidget.addFilterValueChangeHandler(handler);
    }

    public void update() {
        boolean haveFilterableValues = false;
        for (FilterValueChangeHandler<T> valueChangeHandler : getCurrentValueChangeHandlers()) {
            haveFilterableValues |= valueChangeHandler.hasFilterableValues();
        }
        this.filterWidget.asWidget().setVisible(haveFilterableValues);
        Filter<T> filter = haveFilterableValues ? this.filterWidget.getFilter() : alwaysMatchingFilter;
        for (FilterValueChangeHandler<T> valueChangeHandler : getCurrentValueChangeHandlers()) {
            valueChangeHandler.onFilterValueChanged(filter);
        }
    }
    
    public <D extends DTO> RefreshableWidget<D> getRefreshableWidgetWrapper(final RefreshableWidget<D> wrappedWidget) {
        return new RefreshableWidget<D>() {
            @Override
            public void setData(D data) {
                wrappedWidget.setData(data);
                FilterPresenter.this.update();
            }
        };
    }
    
}