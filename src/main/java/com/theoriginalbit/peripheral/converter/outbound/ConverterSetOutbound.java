/**
 * Copyright 2014-2015 Joshua Asbury (@theoriginalbit)
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.theoriginalbit.peripheral.converter.outbound;

import com.google.common.collect.Maps;
import com.theoriginalbit.peripheral.api.converter.IConversionRegistry;
import com.theoriginalbit.peripheral.api.util.SimpleOutboundConverter;
import com.theoriginalbit.peripheral.api.util.TypeConversionException;

import java.util.Map;
import java.util.Set;

/**
 * @author Joshua Asbury (@theoriginalbit)
 */
public class ConverterSetOutbound extends SimpleOutboundConverter<Set<?>> {
    @Override
    public Object convert(IConversionRegistry registry, Set<?> value) throws TypeConversionException {
        Map<Object, Boolean> result = Maps.newHashMap();
        for (Object e : value) {
            result.put(registry.toLua(e), true);
        }
        return result;
    }
}