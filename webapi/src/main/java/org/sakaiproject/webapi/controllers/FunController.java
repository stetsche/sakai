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

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.profile2.logic.ProfileImageLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class FunController extends AbstractSakaiApiController {


    @Autowired
    private ProfileImageLogic profileImageLogic;


    @PutMapping(value = "/users/{userId}/image-official/url", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> setOfficialImageUrl(@PathVariable String userId, @RequestParam String url) {
        checkSakaiSession();
        String returnUrl;
        if (StringUtils.isNotBlank(url)) {
            boolean success = profileImageLogic.saveOfficialImageUrl(userId, url);
            returnUrl = success ? url : "fail";
            return ResponseEntity.ok(returnUrl);
        } else {
            return ResponseEntity.badRequest().body("blank");
        }
    }
}
