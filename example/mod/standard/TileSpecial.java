package mod.standard;

import com.google.common.collect.Lists;
import com.theoriginalbit.minecraft.computercraft.peripheral.annotation.Attach;
import com.theoriginalbit.minecraft.computercraft.peripheral.annotation.Detach;
import com.theoriginalbit.minecraft.computercraft.peripheral.annotation.LuaFunction;
import com.theoriginalbit.minecraft.computercraft.peripheral.annotation.LuaPeripheral;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;

/**
 * Again this is your standard TileEntity class, however this is where your peripheral fun can happen.
 *
 * This class is annotated with the LuaPeripheral annotation, this tells the PeripheralProvider that
 * you wish this TileEntity to be available to ComputerCraft as a peripheral, with the peripheral type
 * specified.
 *
 * This class must then define methods it wishes to be accessible in Lua with the LuaFunction annotation.
 *
 * If you wish your peripheral to know when a computer is attached or detached from your peripheral, you
 * must define a method with the Attach and Detach annotations respectively. Note: you may only define
 * one of each method!
 *
 * It is suggested that you annotate your TileEntities with OpenPeripheral's Ignore annotation so that if
 * you implement anything such as IInventory on your TileEntity, OpenPeripheral will not attempt to claim
 * the peripheral as its own. Ignore annotation url:
 * https://github.com/OpenMods/OpenPeripheral/blob/master/src/main/java/openperipheral/api/Ignore.java
 *
 * @author theoriginalbit
 */
@LuaPeripheral("special")
public class TileSpecial extends TileEntity {

    /**
     * LuaFunction marks that you wish a method to be accessible in Lua.
     * The name it has in Lua will be the same name you define here.
     */
    @LuaFunction
    public void doSomething() {
        System.out.println("Something was done with the special peripheral");
    }

    /**
     * The Peripheral Framework will detect return types, as well as parameter types
     * and will convert them appropriately, it will also handle if the developer/user
     * has provided you with the wrong type, e.g. if they provide a number to this method
     * it will error to them stating "expected string, got number"
     */
    @LuaFunction
    public boolean foo(String bar) {
        return bar.equals("correct!");
    }

    /**
     * Lets assume, that for some reason you wish to have the method appear under a different
     * name in Lua you can provide a name to the LuaFunction; the only time you should really
     * be using this is in the event of method overloading like shown below.
     */
    @LuaFunction(name = "bar")
    public boolean foo() {
        return foo("incorrect!");
    }

    /**
     * What if you need a reference to the computer that is invoking the method?
     * Sure, that's fine too. You can also get the ILuaContext if you want it
     * as well.
     */
    @LuaFunction
    public void wait(IComputerAccess computer) {
        computer.queueEvent("done_waiting", new Object[0]);
    }

    /**
     * By default the Peripheral-Framework will convert return values of Object[] into a
     * {@link java.util.Map} so that it is a table in Lua
     */
    @LuaFunction
    public Object[] getSomething() {
        // this will return the table {[1] = "entry1", [2] = "entry2"}
        return new Object[]{ "entry1", "entry2" };
    }

    /**
     * However to do multi-returns we also must return an Object array. So that problems
     * don't arise we must therefore tell the Peripheral-Framework not to convert this
     * return value to a table, but instead return it as a multi-return; we do this
     * via the LuaFunction annotation like so.
     */
    @LuaFunction(isMultiReturn = true)
    public Object[] getSomethingElse() {
        return new Object[]{ true, "This is a multi-return" };
    }

    // A list you can use to track all the computers attached to this peripheral
    private ArrayList<IComputerAccess> computers = Lists.newArrayList();

    /**
     * The method can be named anything you want, as long as it has the annotation it will
     * be invoked when a computer is attached to your peripheral
     */
    @Attach
    public void attach(IComputerAccess computer) {
        if (!computers.contains(computer)) {
            computers.add(computer);
        }
    }

    /**
     * The method can be named anything you want, as long as it has the annotation it will
     * be invoked when a computer is detached from your peripheral
     */
    @Detach
    public void detach(IComputerAccess computer) {
        if (computers.contains(computer)) {
            computers.remove(computer);
        }
    }

}