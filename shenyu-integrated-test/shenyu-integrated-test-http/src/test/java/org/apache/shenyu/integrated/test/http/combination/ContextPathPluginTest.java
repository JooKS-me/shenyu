/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shenyu.integrated.test.http.combination;

import org.apache.shenyu.common.dto.ConditionData;
import org.apache.shenyu.common.dto.convert.rule.impl.ContextMappingHandle;
import org.apache.shenyu.common.enums.OperatorEnum;
import org.apache.shenyu.common.enums.ParamTypeEnum;
import org.apache.shenyu.common.utils.JsonUtils;
import org.apache.shenyu.integratedtest.common.dto.OrderDTO;
import org.apache.shenyu.integratedtest.common.helper.HttpHelper;
import org.apache.shenyu.web.controller.PluginController;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class ContextPathPluginTest extends AbstractPluginDataInit {

    @BeforeClass
    public static void setup() {
        initSelectorAndRule()
    }

    @Test
    public void test() throws IOException {
        OrderDTO user = new OrderDTO("123", "Tom");
        user = HttpHelper.INSTANCE.postGateway("/http/order/save", user, OrderDTO.class);
        assertThat(user.getName(), is("hello world save order"));

        Map<String, Object> response = HttpHelper.INSTANCE.getFromGateway("/http/order/findById?id=1001", Map.class);
        assertThat(response.get("error"), is("Not Found"));
        assertThat(response.get("path"), is("/error/order/findById"));
    }

    private static List<ConditionData> buildSelectorConditionList() {
        ConditionData conditionData = new ConditionData();
        conditionData.setParamName(ParamTypeEnum.URI.getName());
        conditionData.setOperator(OperatorEnum.EQ.getAlias());
        conditionData.setParamValue("/http/order/findById");
        return Collections.singletonList(conditionData);
    }

    private static List<PluginController.RuleLocalData> buildRuleLocalDataList() {
        PluginController.RuleLocalData ruleLocalData = new PluginController.RuleLocalData();
        ContextMappingHandle handle = new ContextMappingHandle();
        handle.setContextPath("/http");
        handle.setAddPrefix("/error");
        ruleLocalData.setRuleHandler(JsonUtils.toJson(handle));
        ConditionData conditionData = new ConditionData();
        conditionData.setParamType(ParamTypeEnum.URI.getName());
        conditionData.setOperator(OperatorEnum.EQ.getAlias());
        conditionData.setParamValue("/http/order/findById");
        return Collections.singletonList(ruleLocalData);
    }
}
