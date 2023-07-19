/**
 * Copyright (c) 2023 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.condition.api;

import java.util.List;

import org.sakaiproject.condition.api.model.Condition;

public interface ConditionService {


    public void init();

    public Condition getCondition(String conditionId);

    public List<Condition> getConditionsForSite(String siteId);

    public Condition saveCondition(Condition condition);

    public boolean deleteCondition(String conditionId);

    public boolean evaluateCondition(Condition condition);

    public boolean isConditionUsed(Condition condition);

    public boolean isToolIdSupported(String toolId);
}
