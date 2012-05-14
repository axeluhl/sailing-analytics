package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;

/**
 * For each fleet in a {@link Series} there is one {@link RaceDefinition race} per "race column." Competitor to fleet assignment
 * may vary per race in case of the fleets being unordered in their {@link Series}, or may be fixed in case the fleets are
 * ordered in their {@link Series}, as usually the case in finals and medal series.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Fleet extends Named {

}
