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

package net.minecraftforge.event.entity.living;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * Fired when a player is about to deploy an elytra and start gliding.<br>
 * <br>
 * This event is fired on both sides:<br>
 * - on the client, immediately before {@link CPacketEntityAction.Action#START_FALL_FLYING} is sent. Vanilla's own
 * predicate applies: a fresh jump press (or auto-jump) while airborne, falling, not already gliding, not
 * creative-flying, with a usable elytra in the chest slot.<br>
 * - on the server, immediately before {@link EntityPlayerMP#setElytraFlying()} inside the validated branch of
 * {@link net.minecraft.network.NetHandlerPlayServer#processEntityAction(CPacketEntityAction)}, on the main thread.<br>
 * Use {@code getEntityLiving().world.isRemote} to tell the sides apart.<br>
 * <br>
 * {@link #elytra} contains the elytra {@link ItemStack} worn in the chest slot.<br>
 * <br>
 * This event is not fired when {@code FallFlying} is restored from NBT, nor for non-player entities.
 * {@code EntityLivingBase.updateElytra()} can only preserve or clear the gliding flag, so this event covers every
 * deploy.<br>
 * <br>
 * This event is {@link Cancelable}.<br>
 * If this event is canceled on the client, the packet is not sent; vanilla will not retry until the next fresh
 * jump press.<br>
 * If this event is canceled on the server, {@link Constants.EntityFlags#ELYTRA_FLYING} stays clear and nothing
 * is broadcast.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 **/
@Cancelable
public class LivingElytraDeployEvent extends LivingEvent
{
    private final ItemStack elytra;

    public LivingElytraDeployEvent(EntityLivingBase entity, ItemStack elytra)
    {
        super(entity);
        this.elytra = elytra;
    }

    public ItemStack getItemStack()
    {
        return elytra;
    }
}
