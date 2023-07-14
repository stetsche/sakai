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
package org.sakaiproject.conditions.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CARDGAME_STAT_ITEM",
        indexes = { @Index(name = "IDX_CARDGAME_STAT_ITEM_PLAYER_ID", columnList = "PLAYER_ID") })
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Condition {


    @Id
    @Column(name = "ID", nullable = false, length = 36)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(name = "OPERATOR", nullable = false)
    @Enumerated(EnumType.STRING)
    private Operator operator;

    @Column(name = "ARGUMENT", nullable = false)
    private String argument;

    @Column(name = "TOOL_ID", nullable = false)
    private String toolId;

    @Column(name = "ITEM_ID", nullable = false)
    private String itemId;


    public String getReference() {
        return "/condition/" + id;
    }
}
