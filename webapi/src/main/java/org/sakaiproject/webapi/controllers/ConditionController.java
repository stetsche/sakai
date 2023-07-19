/******************************************************************************
 * Copyright 2023 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.cardgame.api.CardGameService;
import org.sakaiproject.cardgame.api.model.CardGameStatItem;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.comparator.UserSortNameComparator;
import org.sakaiproject.webapi.beans.CardGameUserRestBean;
import org.sakaiproject.webapi.beans.ConditionRestBean;
import org.sakaiproject.webapi.beans.ToolItemRestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.sakaiproject.condition.api.ConditionService;
import org.sakaiproject.condition.api.model.Condition;
import org.sakaiproject.api.privacy.PrivacyManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ConditionController extends AbstractSakaiApiController {


    @Autowired
    private SecurityService securityService;

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @Autowired
    private UserDirectoryService userDirectoryService;

    @Autowired
    private ConditionService conditionService;

    @GetMapping(value = "/sites/{siteId}/conditions/{conditionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConditionRestBean> getCondition(@PathVariable String siteId, @PathVariable String conditionId) {
        checkSakaiSession();
        checkSite(siteId);

        if (StringUtils.isNotBlank(conditionId)) {
            Condition condition = conditionService.getCondition(conditionId);
            if (condition != null) {
                return ResponseEntity.ok(ConditionRestBean.builder()
                        .id(conditionId)
                        .toolId(condition.getToolId())
                        .met(conditionService.evaluateCondition(condition))
                        .build());
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping(value = "/sites/{siteId}/conditions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<ConditionRestBean>> getConditions(@PathVariable String siteId) {
        checkSakaiSession();
        checkSite(siteId);

        // List<Condition> conditions = conditionService.getConditionsForSite(siteId);

        // return ResponseEntity.ok(conditions.stream()
        //         .map(condition -> ConditionRestBean.builder()
        //                 .id(condition.getId())
        //                 .toolId(condition.getToolId())
        //                 .met(conditionService.evaluateCondition(condition))
        //                 .build())
        //         .collect(Collectors.toSet()));
//     }

//     public ResponseEntity<Set<ConditionRestBean>> createCondition(@PathVariable String siteId, @RequestParam String toolId, @RequestParam String itemId) {
//         return ResponseEntity.ok(conditions);

        Set<ConditionRestBean> conditions = new HashSet<>(){{
                add(ConditionRestBean.builder()
                        .id("uuid-id-id-uuid")
                        // .toolId(toolId)
                        .type("greater_then")
                        .attribute("10")
                        .met(true)
                        .build());
                add(ConditionRestBean.builder()
                        .id("uuid-di-di-uuid")
                        // .toolId(toolId)
                        .type("smaller_then")
                        .attribute("5")
                        .met(false)
                        .build());
        }};
        return ResponseEntity.ok(conditions);
    }

    @GetMapping(value = "/sites/{siteId}/conditions/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<ToolItemRestBean>> getItems(@PathVariable String siteId, @RequestParam String toolId, @RequestParam String itemId) {
        Set<ToolItemRestBean> items = new HashSet<>(){{
                add(ToolItemRestBean.builder()
                        .id("uuid-id-id-uuid")
                        .toolId(toolId)
                        .conditions(new HashSet<>(){{
                                add(ConditionRestBean.builder()
                                        .id("uuid-id-id-uuid")
                                        .toolId(toolId)
                                        .operator("greater_then")
                                        .attribute("10")
                                        .met(true)
                                        .build());
                                add(ConditionRestBean.builder()
                                        .id("uuid-di-di-uuid")
                                        .toolId(toolId)
                                        .operator("smaller_then")
                                        .attribute("5")
                                        .met(false)
                                        .build());
                                }})
                        .build());
                add(ToolItemRestBean.builder()
                        .id("uuid-di-di-uuid")
                        .toolId(toolId)
                        .conditions(new HashSet<>(){{
                                add(ConditionRestBean.builder()
                                        .id("uuid-id-id-uuid")
                                        .toolId(toolId)
                                        .operator("greater_then")
                                        .attribute("10")
                                        .met(true)
                                        .build());
                                add(ConditionRestBean.builder()
                                        .id("uuid-di-di-uuid")
                                        .toolId(toolId)
                                        .operator("smaller_then")
                                        .attribute("5")
                                        .met(false)
                                        .build());
                                }})
                        .build());
        }};

        return ResponseEntity.ok(items);
    }
}
