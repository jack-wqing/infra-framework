/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jindi.infra.traffic.sentinel.cluster.selector;

import com.jindi.infra.traffic.sentinel.cluster.entity.TokenServerNode;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collection;
import java.util.List;

/**
 * hash Selector:
 *  集群多台机器进行访问: 需要选择相同的机器，使用简单的hash算法，进行选择，性能较高，服务端的机器变动不平凡的情况下
 */
public class HashSelector extends AbstractSelector<TokenServerNode> {

    @Value("${spring.application.name}")
    private String applicationName;

    @Override
    public TokenServerNode doSelect(List<TokenServerNode> source) {
        int index = hashIndex(source);
        if(index == -1){
            return null;
        }
        return source.get(index);
    }

    public int hashIndex(Collection<TokenServerNode> source){
        if(source == null || source.size() < 1){
            return -1;
        }
        return applicationName.hashCode() % source.size();
    }

}
