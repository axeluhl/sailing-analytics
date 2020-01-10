package com.sap.sailing.domain.coursetemplate;

import com.sap.sse.common.Named;

/**
 * TODO remove this abstraction and revert all to MarkRole. The original idea
 * was to represent mark role "proxies" by objects of a type compatible to an
 * interface that MarkRole and the proxy type have in common, leading to
 * MarkRoleName[Impl]. However, I think it would be better to represent the
 * request for the creation of a role with a specific name during the creation
 * of a course template from a CourseConfiguration by annotating the MarkConfiguration
 * not only with storeToInventory and an optional Positioning object but also
 * by an optional mark role creation specification. This can be part of the
 * annotation type used for MarkConfiguration objects during the "request phase"
 * as implemented on branch bug5168 and already merged to bug5085. The merge
 * of those changes into branch bug5165 currently is causing conflicts that should
 * be solved.
 */
public interface IsMarkRole extends Named {
    String getShortName();
}
