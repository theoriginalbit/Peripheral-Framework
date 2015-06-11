/**
 * Copyright 2014 Joshua Asbury (@theoriginalbit)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.theoriginalbit.framework.peripheral.wrapper;

import com.google.common.base.Preconditions;
import com.theoriginalbit.framework.peripheral.PeripheralFramework;
import com.theoriginalbit.framework.peripheral.api.converter.IConversionRegistry;
import com.theoriginalbit.framework.peripheral.api.lua.Function;
import com.theoriginalbit.framework.peripheral.api.util.MultiReturn;
import com.theoriginalbit.framework.peripheral.api.util.TypeConversionException;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This wraps the method supplied from the LuaPeripheral, it provides
 * a simple invocation which converts the Lua arguments supplied to
 * the Java type expected of the LuaFunction, as well as converting
 * the return value back to Lua types. It will also provide user friendly
 * errors when the method is invoked incorrectly, such as 'expected string
 * got number' or 'expected 4 arguments, got 3'
 * <p/>
 * IMPORTANT:
 * This is a backend class, you should never need to use this, and
 * modifying this may have unexpected results.
 *
 * @author theoriginalbit
 */
public class WrapperMethod {

    private final Method method;
    private final Object instance;
    private final int luaParamsCount;
    private final Class<?>[] javaParams;
    private final boolean isMultiReturn;

    public WrapperMethod(Object peripheral, Method m) {
        // why? just 'cause
        Preconditions.checkArgument(m.isAnnotationPresent(Function.class));

        instance = peripheral;
        method = m;
        javaParams = method.getParameterTypes();
        isMultiReturn = method.getReturnType().isAssignableFrom(MultiReturn.class);

        // count how many parameters are required from Lua
        int count = javaParams.length;
        for (Class<?> clazz : javaParams) {
            if (IComputerAccess.class.isAssignableFrom(clazz)) {
                --count;
            } else if (ILuaContext.class.isAssignableFrom(clazz)) {
                --count;
            }
        }

        luaParamsCount = count;
    }

    public Object[] invoke(IComputerAccess access, ILuaContext context, Object[] arguments) throws LuaException {
        // make sure they've provided enough args
        if (arguments.length != luaParamsCount) {
            throw new LuaException(String.format("expected %d arg(s), got %d", luaParamsCount, arguments.length));
        }

        Object[] args = new Object[javaParams.length];
        IConversionRegistry conversionRegistry = PeripheralFramework.getConversionRegistry();

        try {
            for (int i = 0; i < args.length; ++i) {
                if (IComputerAccess.class.isAssignableFrom(javaParams[i])) {
                    args[i] = access;
                } else if (ILuaContext.class.isAssignableFrom(javaParams[i])) {
                    args[i] = context;
                } else {
                    args[i] = conversionRegistry.toJava(arguments[i], javaParams[i]);
                }
            }
        } catch (TypeConversionException e) {
            throw new LuaException(e.getMessage());
        }

        try {
            if (isMultiReturn) {
                // get the result
                final Object[] result = ((MultiReturn) method.invoke(instance, args)).getValues();
                // convert its inner members
                for (int i = 0; i < result.length; ++i) {
                    result[i] = conversionRegistry.toLua(result[i]);
                }
                return result;
            } else {
                // return the result converted, if the method returns Object[] this will be converted to a Map
                return new Object[]{conversionRegistry.toLua(method.invoke(instance, args))};
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new LuaException("Developer problem, please present your client log file to the developer of this peripheral.");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new LuaException("Developer problem, please present your client log file to the developer of this peripheral.");
        } catch (InvocationTargetException e) {
            String message;
            Throwable cause = e;
            while (true) {
                if (!((message = cause.getMessage()) == null && (cause = cause.getCause()) != null)) break;
            }
            throw new LuaException(message);
        } catch (Exception e) {
            throw new LuaException(e.getMessage());
        }
    }

}