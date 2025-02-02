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

package com.heretere.hac.api;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.heretere.hac.api.config.HACConfigHandler;
import com.heretere.hac.api.event.EventManager;
import com.heretere.hac.api.packet.PacketReferences;
import com.heretere.hac.api.player.HACPlayerList;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * API for HAC.
 */
public final class HACAPI {
    /**
     * The Config Handler instance.
     */
    private final @NotNull HACConfigHandler configHandler;
    /**
     * The Event Manager instance.
     */
    private final @NotNull EventManager eventManager;
    /**
     * The ThreadPool instance.
     */
    private final @NotNull ExecutorService threadPool;
    /**
     * The player list instance.
     */
    private final @NotNull HACPlayerList hacPlayerList;
    /**
     * The error handler instance.
     */
    private final @NotNull ErrorHandler errorHandler;
    /**
     * The packet references instance.
     */
    private final @NotNull PacketReferences packetReferences;
    /**
     * Whether or not the api has been loaded.
     */
    private boolean loaded;

    /**
     * Creates a new instance of the API. This is called in hac-core then registered to the Services Manager.
     * <p>
     * To get this instance call Bukkit.getServer().getServicesManager().load(HACAPI.class).
     *
     * @param parent The providing plugin.
     */
    public HACAPI(final @NotNull Plugin parent) {
        Preconditions.checkState(
            Bukkit.getServicesManager().load(HACAPI.class) == null,
            "API already registered. Please use the ServicesManager instead of creating a new instance."
        );

        this.loaded = true;

        this.configHandler = new HACConfigHandler(this, parent);
        this.eventManager = new EventManager();
        this.threadPool = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("hac-thread-%d")
                                                                                  .build());
        this.hacPlayerList = new HACPlayerList(this, parent);
        this.errorHandler = new ErrorHandler(parent);
        this.packetReferences = new PacketReferences();
    }

    /**
     * This is used to insure the API is loaded before passing any information.
     * It's really just a sanity check.
     */
    private void checkLoaded() {
        Preconditions.checkState(this.loaded, "HACAPI not loaded.");
    }

    /**
     * Unloads the API this method is called in hac-core to ensure the API shuts things down during a disable.
     */
    public void unload() {
        this.checkLoaded();
        this.configHandler.unload();
        this.threadPool.shutdown();
        this.loaded = false;
    }

    /**
     * Gets config handler.
     *
     * @return the config handler
     */
    public @NotNull HACConfigHandler getConfigHandler() {
        this.checkLoaded();
        return this.configHandler;
    }

    /**
     * This is the global event manager responsible for passing events throughout HAC.
     *
     * @return Global async event manager for HAC.
     */
    public @NotNull EventManager getEventManager() {
        this.checkLoaded();
        return this.eventManager;
    }

    /**
     * This is the thread pool instance for HAC. This is used to multi-thread the events inside HAC to reduce load
     * on the main server thread.
     *
     * @return By Default a Cached Thread Pool.
     */
    public @NotNull ExecutorService getThreadPool() {
        this.checkLoaded();
        return this.threadPool;
    }

    /**
     * Anytime a {@link com.heretere.hac.api.player.HACPlayer} is created it is registered to this list.
     * hac-core manages removing and adding players to the list.
     *
     * @return All HACPlayer instances registered by HAC.
     */
    public @NotNull HACPlayerList getHacPlayerList() {
        this.checkLoaded();
        return this.hacPlayerList;
    }

    /**
     * Anytime an error occurs inside the API it is sent to this {@link HACAPI.ErrorHandler},
     * errors outside the API should be handled by it's respective plugin.
     *
     * @return The ErrorHandler for the API.
     */
    public @NotNull ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    /**
     * Gets packet references.
     *
     * @return the packet references
     */
    public @NotNull PacketReferences getPacketReferences() {
        this.checkLoaded();
        return this.packetReferences;
    }

    /**
     * This class is used to offload the error's inside the API to one place. By default hac-core overwrites the
     * error handler so error information can be outputted to a log.
     */
    public static final class ErrorHandler {
        /**
         * The handler that errors are passed to.
         */
        private @NotNull Consumer<Throwable> handler;

        private ErrorHandler(final @NotNull Plugin parent) {
            this.handler = ex -> {
                if (ex != null) {
                    parent.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                }
            };
        }

        /**
         * Gets the currently registered error handler for the API.
         *
         * @return Consumer currently used for error handling.
         */
        public @NotNull Consumer<Throwable> getHandler() {
            return this.handler;
        }

        /**
         * Overwrites the error handler with a new consumer to handle errors.
         *
         * @param handler The new error handling consumer.
         */
        public void setHandler(final @NotNull Consumer<Throwable> handler) {
            this.handler = handler;
        }
    }
}
