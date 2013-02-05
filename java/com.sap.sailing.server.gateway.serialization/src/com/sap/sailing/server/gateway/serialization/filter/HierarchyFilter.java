package com.sap.sailing.server.gateway.serialization.filter;

public interface HierarchyFilter<ParentType, ElementType> extends Filter<ElementType> {
	
	public class NoFilter<ParentType, ElementType> implements HierarchyFilter<ParentType, ElementType> {
		@Override
		public boolean isFiltered(ParentType parent, ElementType object) {
			return false;
		}

		@Override
		public boolean isFiltered(ElementType object) {
			return false;
		}
	}

	public boolean isFiltered(ParentType parent, ElementType object);
	
}
