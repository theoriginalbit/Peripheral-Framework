Peripheral Framework
====================

**IMPORTANT! This framework is intended for ComputerCraft 1.65 and any subsequent versions which make use of the same API**


Purpose
-------------

An open-source framework that has the aim of allowing developers to implement their ComputerCraft peripherals more easily, more quickly, and more cleanly; this has the advantage of allowing you to focus on writing useful code instead of formatting your code to the expectations of ComputerCraft's native API.


What Makes it Good?
-------------

The Peripheral Framework provides numerous advantages for both developers and users when it comes to making a peripheral. To fully appreciate just how much Peripheral Framework can do for you, take a look at a <a href="https://theoriginalbit.github.io/peripheral-framework/comparison.html" target="_blank">side-by-side comparison</a> between a native implementation of monitors and the simplified peripheral framework implementation. 

Below is a compiled list of the many things that this framework does to make your life easier.

 - **Remove the need for `getMethodNames` —** No more massive switch statements to handle something that should be incredibly simple. Write your methods as if you have the intention of reading them later.
 - **Re-use your code —** You shouldn't have to reimplement your peripheral methods in order to provide them as Lua functions. With this framework, simply adding the `@LuaFunction` annotation above your method will mark it as a Lua function.
 - **IPeripheral implementation not required —** There can be no doubt that interfaces are quite useful, but sometimes they add unnecessary clutter to your code. By using this framework you'll remove the need to implement the `IPeripheral` interface and free your code from contract methods.
 - **Seamless conversion between Lua and Java —** Don't worry about wrapping your return values in an `Object[]` array: this framework will handle wrapping your return values in an array and converting them to the proper types.
 - **Automatically handle incorrect arguments —** Without the Peripheral Framework, ensuring that the user has input correct argument types can be a pain. With the framework however, useful error messages are automatically returned to the user informing them what the argument types should be (e.g. `expected string, got number`).
 - **And more ...** The Peripheral Framework does a lot behind the scenes that will help you out when designing peripherals.


Installation for Developers
--------------

**NOTE: It is suggested that you utilize [OpenPeripheral's `@Ignore` annotation](https://github.com/OpenMods/OpenPeripheral/blob/master/src/main/java/openperipheral/api/peripheral/Ignore.java) with TileEntities in order to prevent OpenPeripheral from claiming your TileEntity as its own.**

As a developer, installing this framework is as simple as copying the contents of the [`src`](/src) directory into your project: this includes dan200's ComputerCraft API if you do not have it installed to your project already.


Installation for Users
--------------

Users should not need to install this framework as developers should bundle it with their mod files. If you have been instructed to install this framework separately by a mod author, they have done something wrong.

License
-------

The Peripheral-Framework developed by Joshua Asbury (@theoriginalbit) is distributed under the terms of the Apache License version 2, which can be found under the name LICENSE in the distribution.
