/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.grading.api;

/**
 * The grading types that a gradebook could be configured as
 */
public enum GradeType {

    UNKNOWN(0),
    POINTS(1),
    PERCENTAGE(2),
    LETTER(3);

    private final int value;

    GradeType(int value) {
        this.value = value;
    }

    public GradeType getValue() {
        return GradeType.values()[this.value];
    }

    public boolean equals(GradeType gradeType) {
        return gradeType != null && this.value == gradeType.value;
    }
}

