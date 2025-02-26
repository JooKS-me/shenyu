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

package org.apache.shenyu.plugin.mock.generator;

import org.apache.shenyu.plugin.mock.util.RandomUtil;
import org.apache.shenyu.spi.Join;

/**
 * 11-digit mobile number generator.
 */
@Join
public class PhoneGenerator implements Generator<String> {
    
    @Override
    public String getName() {
        return "phone";
    }
    
    @Override
    public String generate() {
        StringBuilder builder = new StringBuilder("1");
        builder.append(RandomUtil.randomInt(3, 9));
        for (int i = 0; i < 9; i++) {
            builder.append(RandomUtil.randomInt(0, 9));
        }
        return builder.toString();
    }
    
    @Override
    public int getParamSize() {
        return 0;
    }
    
    @Override
    public boolean match(final String rule) {
        return rule.matches("^phone$");
    }
}
