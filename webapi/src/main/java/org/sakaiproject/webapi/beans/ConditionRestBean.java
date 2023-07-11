package org.sakaiproject.webapi.beans;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConditionRestBean {


    private String id;
    private String toolId;
    private boolean met;
    private String type;
    private String attribute;
}
