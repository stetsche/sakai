package org.sakaiproject.sitestats.impl;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@NonNull
@Builder
public class SitePresenceKey {


    private String siteId;
    private String userId;
    private String sessionId;


    public static SitePresenceKey from(SitePresenceRecord consolidation) {
        return SitePresenceKey.builder()
            .siteId(consolidation.getSiteId())
            .userId(consolidation.getUserId())
            .build();
    }
}
