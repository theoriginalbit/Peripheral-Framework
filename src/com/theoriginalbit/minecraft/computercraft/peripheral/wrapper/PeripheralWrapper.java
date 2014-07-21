package com.theoriginalbit.minecraft.computercraft.peripheral.wrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.theoriginalbit.minecraft.computercraft.peripheral.annotation.*;

import cpw.mods.fml.common.Loader;
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
 * with LuaFunction and retain references of methods annotated with OnAttach
 * and OnDetach so that your peripheral will function with ComputerCraft's
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
    private final LinkedHashMap<String, MethodWrapper> methods = Maps.newLinkedHashMap();
	private final String[] methodNames;
    private final ArrayList<IComputerAccess> computers = Lists.newArrayList();

    public PeripheralWrapper(Object peripheral) {
		final Class<?> peripheralClass = peripheral.getClass();
        final LuaPeripheral peripheralLua = peripheralClass.getAnnotation(LuaPeripheral.class);

        // validate the peripheral type
        final String pname = peripheralLua.value().trim();
        Preconditions.checkArgument(!pname.isEmpty(), "Peripheral name cannot be an empty string");

		for (Method m : peripheralClass.getMethods()) {
            // if the method defines it to be a LuaFunction, and it is specified to be enabled
            if (m.isAnnotationPresent(LuaFunction.class) && isEnabled(m)) {
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

        // check for the @Computer fields and assign them to this instances computer list
        for (Field f : peripheralClass.getDeclaredFields()) {
            if (f.isAnnotationPresent(ComputerList.class)) {
                try {
                    f.set(peripheral, computers);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        peripheralType = pname;
        Set<String> keys = methods.keySet();
		methodNames = keys.toArray(new String[keys.size()]);
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
        if (!computers.contains(computer)) {
            computers.add(computer);
        }
	}

	@Override
	public void detach(IComputerAccess computer) {
        if (computers.contains(computer)) {
            computers.remove(computer);
        }
	}

    /**
     * dan200, why do we have to do this? why can't we just use Java's native equals?
     */
	@Override
	public boolean equals(IPeripheral other) {
        return other != null && other instanceof PeripheralWrapper && other == this;
	}

    private boolean isEnabled(Method method) {
        // get the mod ids specified that this method should be enabled for
        final String[] modIds = method.getAnnotation(LuaFunction.class).modIds();
        // if there are not mod ids, then we should enable this
        if (modIds.length == 0) {
            return true;
        }
        // loop through the mod ids and see if any are present
        for (String mid : modIds) {
            // if one was present, load
            if (Loader.isModLoaded(mid)) {
                return true;
            }
        }
        // mods are specified, none are present, this method shouldn't load
        return false;
    }

}
