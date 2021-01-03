/*
 * MIT License
 *
 * Copyright (c) 2021 Justin Heflin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.heretere.hac.core.proxy.player.executors;

import com.heretere.hac.api.HACAPI;
import com.heretere.hac.api.event.PacketEventExecutor;
import com.heretere.hac.api.event.Priority;
import com.heretere.hac.api.event.packet.wrapper.clientside.EntityActionPacket;
import com.heretere.hac.api.player.HACPlayer;
import com.heretere.hac.core.proxy.player.PlayerData;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

/**
 * The type Player data entity action executor.
 */
public final class PlayerDataEntityActionExecutor extends PacketEventExecutor<EntityActionPacket> {

    /**
     * Instantiates a new Player data entity action executor.
     *
     * @param api        the HACAPI reference
     * @param identifier the identifier
     */
    public PlayerDataEntityActionExecutor(
        final @NotNull HACAPI api,
        final @NotNull String identifier
    ) {
        super(
            Priority.PROCESS_1,
            identifier,
            api.getPacketReferences().getClientSide().getEntityAction(),
            false
        );
    }

    @Override
    public boolean execute(
        final @NotNull HACPlayer player,
        final @NotNull EntityActionPacket packet
    ) {
        player.getDataManager().getData(PlayerData.class).ifPresent(playerData -> {
            switch (packet.getAction()) {
                case START_SNEAKING:
                    playerData.getCurrent().setSneaking(true);
                    break;
                case STOP_SNEAKING:
                    playerData.getCurrent().setSneaking(false);
                    break;
                case START_SPRINTING:
                    playerData.getCurrent().setSprinting(true);
                    break;
                case STOP_SPRINTING:
                    playerData.getCurrent().setSprinting(false);
                    break;
                case START_FALL_FLYING:
                    playerData.getCurrent().setElytraFlying(true);
                    break;
                default:
                    break;
            }
        });

        return true;
    }

    @Override
    public void onStop(
        final @NotNull HACPlayer player,
        final @NotNull EntityActionPacket packet
    ) {
        throw new NotImplementedException("Updater Class.");
    }
}
