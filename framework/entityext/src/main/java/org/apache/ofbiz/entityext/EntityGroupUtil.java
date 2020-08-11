/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.apache.ofbiz.entityext;

import org.apache.ofbiz.entity.*;
import org.apache.ofbiz.entity.model.*;
import org.apache.ofbiz.entity.util.*;

import java.util.*;
import java.util.stream.*;

/**
 * EntityEcaUtil
 */
public final class EntityGroupUtil {

    public static final String module = EntityGroupUtil.class.getName();

    private EntityGroupUtil () {}

    public static Set<String> getEntityNamesByGroup(String entityGroupId, Delegator delegator, boolean requireStampFields) throws GenericEntityException {
        Set<String> entityNames = new HashSet<>();

        List<GenericValue> entitySyncGroupIncludes = EntityQuery.use(delegator).from("EntityGroupEntry").where("entityGroupId", entityGroupId).queryList();
        List<ModelEntity> modelEntities = getModelEntitiesFromRecords(entitySyncGroupIncludes, delegator, requireStampFields);
        for (ModelEntity modelEntity: modelEntities) {
            entityNames.add(modelEntity.getEntityName());
        }

        return entityNames;
    }

    public static List<ModelEntity> getModelEntitiesFromRecords(List<GenericValue> entityGroupEntryValues, Delegator delegator, boolean requireStampFields) throws GenericEntityException {
        return delegator.getModelReader().getEntityNames().stream()
            .map(delegator::getModelEntity)
            .filter(modelEntity -> !(modelEntity instanceof ModelViewEntity))
            .filter(modelEntity -> !requireStampFields || modelEntity.isField(ModelEntity.STAMP_FIELD) && modelEntity.isField(ModelEntity.STAMP_TX_FIELD))
            .filter(modelEntity -> {
                    if (entityGroupEntryValues.size() == 0) return true;
                    List<String> applEnumIds = entityGroupEntryValues.stream()
                        .filter(entitySyncInclude -> {
                            String entityOrPackage = entitySyncInclude.getString("entityOrPackage");
                            return modelEntity.getEntityName().equals(entityOrPackage) ||
                                modelEntity.getPackageName().startsWith(entityOrPackage);
                        })
                        .map(entitySyncInclude -> entitySyncInclude.getString("applEnumId"))
                        .collect(Collectors.toList());
                    return applEnumIds.contains("ESIA_ALWAYS")
                        || (applEnumIds.contains("ESIA_INCLUDE")
                        && !applEnumIds.contains("ESIA_EXCLUDE"));
                    // make sure this log message is not checked in uncommented:
                    //Debug.logInfo("In runEntitySync adding [" + modelEntity.getEntityName() + "] to list of Entities to sync", module);
                })
            .collect(Collectors.toCollection(LinkedList::new));
    }
}
