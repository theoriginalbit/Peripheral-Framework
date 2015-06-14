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
package com.theoriginalbit.peripheral.turtle;

import com.theoriginalbit.peripheral.wrapper.WrapperComputer;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.*;
import net.minecraft.item.ItemStack;

/**
 * @author Joshua Asbury (@theoriginalbit)
 */
public abstract class UpgradePeripheral implements ITurtleUpgrade {
    private final int id;
    private final String name;
    private final ItemStack stack;

    public UpgradePeripheral(int upgradeId, String adjective, ItemStack craftingItemStack) {
        id = upgradeId;
        name = adjective;
        stack = craftingItemStack;
    }

    @Override
    public final int getUpgradeID() {
        return id;
    }

    @Override
    public final String getUnlocalisedAdjective() {
        return name;
    }

    @Override
    public final TurtleUpgradeType getType() {
        return TurtleUpgradeType.Peripheral;
    }

    @Override
    public final ItemStack getCraftingItem() {
        return stack;
    }

    @Override
    public final IPeripheral createPeripheral(ITurtleAccess turtle, TurtleSide side) {
        try {
            final WrapperComputer wrapper = getPeripheralWrapper(turtle, side);
            update(turtle, side, wrapper);
            return wrapper;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public final TurtleCommandResult useTool(ITurtleAccess turtle, TurtleSide side, TurtleVerb verb, int direction) {
        return TurtleCommandResult.failure();
    }

    @Override
    public final void update(ITurtleAccess turtle, TurtleSide side) {
        IPeripheral peripheral = turtle.getPeripheral(side);
        if (peripheral instanceof WrapperComputer) {
            update(turtle, side, (WrapperComputer) peripheral);
        }
    }

    protected abstract void update(ITurtleAccess turtle, TurtleSide side, WrapperComputer peripheral);

    protected abstract WrapperComputer getPeripheralWrapper(ITurtleAccess access, TurtleSide side);
}
