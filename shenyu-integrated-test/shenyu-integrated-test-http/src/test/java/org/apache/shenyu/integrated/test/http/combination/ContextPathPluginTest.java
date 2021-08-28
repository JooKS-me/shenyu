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
import org.apache.shenyu.common.dto.SelectorData;
import org.apache.shenyu.common.dto.convert.rule.impl.ContextMappingHandle;
import org.apache.shenyu.common.dto.convert.rule.impl.DivideRuleHandle;
import org.apache.shenyu.common.enums.OperatorEnum;
import org.apache.shenyu.common.enums.ParamTypeEnum;
import org.apache.shenyu.common.enums.PluginEnum;
import org.apache.shenyu.common.utils.JsonUtils;
import org.apache.shenyu.integratedtest.common.dto.OrderDTO;
import org.apache.shenyu.integratedtest.common.helper.HttpHelper;
import org.apache.shenyu.web.controller.PluginController;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class ContextPathPluginTest extends AbstractPluginDataInit {

    @BeforeClass
    public static void setup() throws IOException {
        final String selectorHandler = "[{\"upstreamHost\":\"127.0.0.1\",\"upstreamUrl\":\"127.0.0.1:8189\",\"protocol\":\"http://\",\"weight\":50,\"timestamp\":0,\"warmup\":0,\"status\":true}]";
        String selectorAndRulesResultForDivide = initSelectorAndRules(PluginEnum.DIVIDE.getName(), "/test/order", selectorHandler, buildSelectorConditionListForDivide(), buildRuleLocalDataListForDivide());
        assertThat(selectorAndRulesResultForDivide, is("success"));
        String selectorAndRulesResultForContextPath = initSelectorAndRules(PluginEnum.CONTEXT_PATH.getName(), "/test/order","", buildSelectorConditionListForContextPath(), buildRuleLocalDataListForContextPath());
        assertThat(selectorAndRulesResultForContextPath, is("success"));
    }

    @Test
    public void test() throws IOException {
        OrderDTO user = new OrderDTO("123", "Tom");
        user = HttpHelper.INSTANCE.postGateway("/test/order/save", user, OrderDTO.class);
        assertThat(user.getName(), is("hello world save order"));

        Map<String, Object> response = HttpHelper.INSTANCE.getFromGateway("/test/order/findById?id=1001", Map.class);
        assertThat(response.get("error"), is("Not Found"));
        assertThat(response.get("path"), is("/error/order/findById"));
    }

    private static List<ConditionData> buildSelectorConditionListForDivide() {
        ConditionData conditionData = new ConditionData();
        conditionData.setParamType(ParamTypeEnum.URI.getName());
        conditionData.setOperator(OperatorEnum.MATCH.getAlias());
        conditionData.setParamValue("/test/order/**");
        return Collections.singletonList(conditionData);
    }

    private static List<PluginController.RuleLocalData> buildRuleLocalDataListForDivide() {
        List<PluginController.RuleLocalData> ruleLocalDataList = new ArrayList<>();
        ruleLocalDataList.add(buildRuleLocalDataForDivide("/test/order/findById"));
        ruleLocalDataList.add(buildRuleLocalDataForDivide("/test/order/save"));
        return ruleLocalDataList;
    }

    private static PluginController.RuleLocalData buildRuleLocalDataForDivide(final String paramValue) {
        PluginController.RuleLocalData ruleLocalData = new PluginController.RuleLocalData();
        DivideRuleHandle handle = new DivideRuleHandle();
        handle.setRetry(0);
        handle.setLoadBalance("random");
        ruleLocalData.setRuleHandler(JsonUtils.toJson(handle));
        ConditionData conditionData = new ConditionData();
        conditionData.setParamType(ParamTypeEnum.URI.getName());
        conditionData.setOperator(OperatorEnum.EQ.getAlias());
        conditionData.setParamValue(paramValue);
        ruleLocalData.setConditionDataList(Collections.singletonList(conditionData));
        return ruleLocalData;
    }

    private static List<ConditionData> buildSelectorConditionListForContextPath() {
        ConditionData conditionData = new ConditionData();
        conditionData.setParamType(ParamTypeEnum.URI.getName());
        conditionData.setOperator(OperatorEnum.MATCH.getAlias());
        conditionData.setParamValue("/test/order/**");
        return Collections.singletonList(conditionData);
    }

    private static List<PluginController.RuleLocalData> buildRuleLocalDataListForContextPath() {
        List<PluginController.RuleLocalData> ruleLocalDataList = new ArrayList<>();
        ruleLocalDataList.add(buildRuleLocalDataForContextPath("/test", "error", "/test/order/findById"));
        ruleLocalDataList.add(buildRuleLocalDataForContextPath("/test", "", "/test/order/save"));
        return ruleLocalDataList;
    }

    private static PluginController.RuleLocalData buildRuleLocalDataForContextPath(final String contextPath, final String addPrefix, final String paramValue) {
        PluginController.RuleLocalData ruleLocalData = new PluginController.RuleLocalData();
        ContextMappingHandle handle = new ContextMappingHandle();
        handle.setContextPath(contextPath);
        handle.setAddPrefix(addPrefix);
        ruleLocalData.setRuleHandler(JsonUtils.toJson(handle));
        ConditionData conditionData = new ConditionData();
        conditionData.setParamType(ParamTypeEnum.URI.getName());
        conditionData.setOperator(OperatorEnum.EQ.getAlias());
        conditionData.setParamValue(paramValue);
        return ruleLocalData;
    }

    @AfterClass
    public static void clean() throws IOException {
        List<SelectorData> selectorDataList = findListSelectorByPluginName("divide");
        List<SelectorData> selectorDataForDivide = selectorDataList.stream().filter(selectorData -> "/test/order".equals(selectorData.getName())).collect(Collectors.toList());
        assertThat(selectorDataForDivide.size(), is(1));
        String selectorIdForDivide = selectorDataForDivide.get(0).getId();
        String message = deleteSelector("divide", selectorIdForDivide);
        assertThat(message, is("success"));

        selectorDataList = findListSelectorByPluginName("context_path");
        List<SelectorData> selectorDataForContextPath = selectorDataList.stream().filter(selectorData -> "/test/order".equals(selectorData.getName())).collect(Collectors.toList());
        assertThat(selectorDataForContextPath.size(), is(1));
        String selectorIdForContextPath = selectorDataForContextPath.get(0).getId();
        message = deleteSelector("context_path", selectorIdForContextPath);
        assertThat(message, is("success"));
    }
}
