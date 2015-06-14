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
package com.theoriginalbit.peripheral.wrapper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.theoriginalbit.peripheral.api.event.Mount;
import com.theoriginalbit.peripheral.api.filesystem.IMount;
import dan200.computercraft.api.peripheral.IComputerAccess;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Joshua Asbury (@theoriginalbit)
 */
public class WrapperComputer extends WrapperGeneric {
    private static final HashMap<Integer, Integer> MOUNT_COUNTS = Maps.newHashMap();
    private final ArrayList<IMount> mounts = Lists.newArrayList();

    public WrapperComputer(Object peripheral) {
        super(peripheral);

        final Class<?> peripheralClass = peripheral.getClass();
        if (peripheralClass.isAnnotationPresent(Mount.class)) {
            final Mount annotationMount = peripheralClass.getAnnotation(Mount.class);

            // Build the specified mount classes
            for (Class<? extends IMount> clazz : annotationMount.value()) {
                try {
                    final IMount mount = clazz.newInstance();
                    mounts.add(mount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void attach(IComputerAccess computer) {
        super.attach(computer);

        // mount anything required to this computer
        if (mounts.isEmpty()) {
            return;
        }

        if (methodAttach != null) {
            try {
                methodAttach.invoke(null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        final int id = computer.getID();

        // make an entry for this computer if there isn't one
        if (!MOUNT_COUNTS.containsKey(id)) {
            MOUNT_COUNTS.put(id, 0);
        }

        // perform the mount if needed
        int count = MOUNT_COUNTS.get(id);
        if (count++ == 0) {
            for (IMount mount : mounts) {
                computer.mount(mount.getMountLocation(), mount);
            }
        }
        // remember how many peripherals are attached to this computer
        MOUNT_COUNTS.put(id, count);
    }

    @Override
    public void detach(IComputerAccess computer) {
        super.detach(computer);

        // un-mount anything required to this computer
        if (mounts.isEmpty()) {
            return;
        }

        if (methodDetach != null) {
            try {
                methodDetach.invoke(null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        final int id = computer.getID();

        // if there is no entry for this computer something has gon seriously wrong, but lets ignore it
        if (!MOUNT_COUNTS.containsKey(id)) {
            return;
        }

        // if it was the last peripheral mounted to the computer, un-mount the mounts
        int count = MOUNT_COUNTS.get(id);
        if (--count == 0) {
            for (IMount mount : mounts) {
                computer.mount(mount.getMountLocation(), mount);
                computer.unmount(mount.getMountLocation());
            }
        }
        // remember how many peripherals are attached to this computer
        MOUNT_COUNTS.put(id, count);
    }

}
