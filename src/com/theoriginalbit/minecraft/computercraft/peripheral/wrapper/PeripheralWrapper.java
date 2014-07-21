package com.theoriginalbit.minecraft.computercraft.peripheral.wrapper;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.theoriginalbit.minecraft.computercraft.peripheral.annotation.*;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

/**
 * Peripheral Framework is an open-source framework that has the aim of
 * allowing developers to implement their ComputerCraft peripherals faster,
 * easier, and cleaner; allowing them to focus more on developing their
 * content.
 *
 * Copyright (C) 2014  Joshua Asbury (@theoriginalbit)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */

/**
 * This wraps the object annotated with LuaPeripheral that is supplied to
 * it from the Peripheral Provider, it will then wrap any methods annotated
 * with LuaFunction and retain references of methods annotated with Attach
 * and Detach so that your peripheral will function with ComputerCraft's
 * expected IPeripheral interface.
 *
 * IMPORTANT:
 * This is a backend class, you should never need to use this, and
 * modifying this may have unexpected results.
 *
 * @author theoriginalbit
 */
public class PeripheralWrapper implements IPeripheral {

    private final String peripheralType;
	private final Object instance;
    private final LinkedHashMap<String, MethodWrapper> methods = Maps.newLinkedHashMap();
    private final Method attach;
    private final Method detach;
	private final String[] methodNames;
	
	public PeripheralWrapper(Object peripheral) {
		final Class<?> peripheralClass = peripheral.getClass();
        final LuaPeripheral peripheralLua = peripheralClass.getAnnotation(LuaPeripheral.class);

        // validate the peripheral type
        final String pname = peripheralLua.value().trim();
        Preconditions.checkArgument(!pname.isEmpty(), "Peripheral name cannot be an empty string");

        Method attachMethod = null;
        Method detachMethod = null;

		for (Method m : peripheralClass.getMethods()) {
            // if the method defines it to be an attach
            if (isAttachMethod(m)) {
                Preconditions.checkArgument(attachMethod == null, "Duplicate methods found annotated with Attach, a peripheral can only define one Attach method");
                Class<?>[] params = m.getParameterTypes();
                Preconditions.checkArgument(params.length == 1, "Attach methods should only have one argument; IComputerAccess");
                Preconditions.checkArgument(IComputerAccess.class.isAssignableFrom(params[0]), "Invalid argument on Attach method should be IComputerAccess");
                attachMethod = m;
            // if the method defines it to be a detach
            } else if (isDetachMethod(m)) {
                Preconditions.checkArgument(detachMethod == null, "Duplicate methods found annotated with Detach, a peripheral can only define one Detach method");
                Class<?>[] params = m.getParameterTypes();
                Preconditions.checkArgument(params.length == 1, "Detach methods should only have one argument; IComputerAccess");
                Preconditions.checkArgument(IComputerAccess.class.isAssignableFrom(params[0]), "Invalid argument on Detach method should be IComputerAccess");
                detachMethod = m;
            // if the method defines it to be a LuaFunction
            } else if (m.isAnnotationPresent(LuaFunction.class)) {
                LuaFunction annotation = m.getAnnotation(LuaFunction.class);
                // extract the method name either from the annotation or the actual name
                final String name = annotation.name().trim().isEmpty() ? m.getName() : annotation.name().trim();
                // make sure it doesn't already exist
                Preconditions.checkArgument(!methods.containsKey(name), "Duplicate method found " + name + ". Either make use of the name in the LuaFunction annotation, or if these methods do the same purpose use the Alias annotation instead.");
                // wrap and store the method
				final MethodWrapper wrapper = new MethodWrapper(peripheral, m);
				methods.put(name, wrapper);
                // add Alias references too
                if (m.isAnnotationPresent(Alias.class)) {
                    for (String alias : m.getAnnotation(Alias.class).value()) {
                        Preconditions.checkArgument(!methods.containsKey(alias), "Duplicate method found while attempting to apply Alias " + alias);
                        methods.put(alias, wrapper);
                    }
                }
            // make sure the method isn't just annotated with the Alias annotation
			} else if (m.isAnnotationPresent(Alias.class)) {
                throw new RuntimeException("Alias annotations should only occur on LuaFunction annotated methods");
            }
		}

        instance = peripheral;
        peripheralType = pname;
        attach = attachMethod;
        detach = detachMethod;
		methodNames = (String[]) methods.keySet().toArray();
	}

	@Override
	public String getType() {
		return peripheralType;
	}

	@Override
	public String[] getMethodNames() {
		return methodNames;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int methodIdx, Object[] arguments) throws Exception {
        final String name = methodNames[methodIdx];
        final MethodWrapper method = methods.get(name);
        return method.invoke(computer, context, arguments);
	}

	@Override
	public void attach(IComputerAccess computer) {
        // try to invoke the defined attach method
        try {
            attach.invoke(instance, computer);
        } catch (Exception e) {
            if (attach != null) {
                e.printStackTrace();
            }
        }
	}

	@Override
	public void detach(IComputerAccess computer) {
        // try to invoke the defined detach method
        try {
            detach.invoke(instance, computer);
        } catch (Exception e) {
            if (detach != null) {
                e.printStackTrace();
            }
        }
	}

    /**
     * dan200, what the hell does this do? and how does it differ from Javas native equals?
     */
	@Override
	public boolean equals(IPeripheral other) {
		return false;
	}

    private boolean isAttachMethod(Method method) {
        if (method.isAnnotationPresent(Attach.class)) {
            Preconditions.checkArgument(!method.isAnnotationPresent(Alias.class), "Attach method cannot have an alias");
            Preconditions.checkArgument(!method.isAnnotationPresent(Detach.class), "Attach method cannot also be a Detach method");
            Preconditions.checkArgument(!method.isAnnotationPresent(LuaFunction.class), "Attach method cannot also be a LuaFunction method");
            return true;
        }
        return false;
    }

    private boolean isDetachMethod(Method method) {
        if (method.isAnnotationPresent(Detach.class)) {
            Preconditions.checkArgument(!method.isAnnotationPresent(Alias.class), "Detach method cannot have an alias");
            Preconditions.checkArgument(!method.isAnnotationPresent(Attach.class), "Detach method cannot also be an Attach method");
            Preconditions.checkArgument(!method.isAnnotationPresent(LuaFunction.class), "Detach method cannot also be a LuaFunction method");
            return true;
        }
        return false;
    }
}
