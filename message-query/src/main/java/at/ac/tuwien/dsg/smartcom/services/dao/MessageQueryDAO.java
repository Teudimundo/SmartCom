/**
 * Copyright (c) 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184 (http://dsg.tuwien.ac.at)
 *
 * This work was partially supported by the EU FP7 FET SmartSociety (http://www.smart-society-project.eu/).
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package at.ac.tuwien.dsg.smartcom.services.dao;

import at.ac.tuwien.dsg.smartcom.exception.IllegalQueryException;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.services.QueryCriteriaImpl;

import java.util.Collection;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface MessageQueryDAO {

    /**
     * Queries the message log for messages that match a given query statement (similar to the JPA Criteria API).
     *
     * @param criteria Statement that will be used to query the message log using the QueryCriteria class (similar to the JPA Criteria API).
     * @return Returns a collection of messages that match the given query statement.
     * @throws IllegalQueryException if the query is not valid.
     */
    public Collection<Message> query(QueryCriteriaImpl criteria) throws IllegalQueryException;
}