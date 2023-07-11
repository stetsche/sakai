package org.sakaiproject.webapi.beans;

import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ToolItemRestBean {


    private String id;
    private String collectionId;
    private String toolId;
    private Set<ConditionRestBean> conditions;
}
