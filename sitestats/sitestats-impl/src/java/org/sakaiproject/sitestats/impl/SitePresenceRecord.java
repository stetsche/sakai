package org.sakaiproject.sitestats.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SitePresenceRecord extends PresenceRecord {


    private String siteId;
    private String userId;


    public SitePresenceKey getKey() {
        return SitePresenceKey.from(this);
    }
}
