/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.debug.entity.living;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingElytraDeployEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Client side: cancels the first elytra deploy press of every airborne period ("triple-jump" style), so the first
 * airborne press does nothing and the second deploys. Landing resets it. Server side: logs every deploy; with
 * {@code CANCEL_SERVER} on, cancels every deploy, so the elytra never opens and {@code isElytraFlying()} stays
 * false on both sides.
 */
@Mod(modid = ElytraDeployEventTest.MODID, name = "Elytra Deploy Event Test", version = "1.0", acceptableRemoteVersions = "*")
@Mod.EventBusSubscriber
public class ElytraDeployEventTest
{
    public static final String MODID = "elytradeployeventtest";
    private static final boolean ENABLED = false;
    private static final boolean CANCEL_CLIENT_FIRST_PRESS = true;
    private static final boolean CANCEL_SERVER = false;
    private static final Logger LOGGER = LogManager.getLogger(MODID);

    // Players whose first press of the current airborne period was already swallowed. Only touched on the client
    // main thread; the integrated server shares these statics, so the server side must never use it.
    private static final Set<UUID> CLIENT_CANCELED = new HashSet<>();

    @SubscribeEvent
    public static void onElytraDeploy(LivingElytraDeployEvent event)
    {
        if (!ENABLED)
        {
            return;
        }
        EntityLivingBase entity = event.getEntityLiving();
        boolean remote = entity.world.isRemote;
        if (remote)
        {
            // add() is false once this airborne period's first press has already been swallowed
            if (CANCEL_CLIENT_FIRST_PRESS && CLIENT_CANCELED.add(entity.getUniqueID()))
            {
                event.setCanceled(true);
            }
        }
        else if (CANCEL_SERVER)
        {
            event.setCanceled(true);
        }
        LOGGER.info("[{}] {} deploying {}: {}", remote ? "client" : "server", entity.getName(), event.getItemStack(), event.isCanceled() ? "canceled" : "allowed");
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingUpdateEvent event)
    {
        EntityLivingBase entity = event.getEntityLiving();
        if (ENABLED && entity.world.isRemote && entity.onGround && entity instanceof EntityPlayer && !CLIENT_CANCELED.isEmpty())
        {
            CLIENT_CANCELED.remove(entity.getUniqueID());
        }
    }
}
