/*
 * Offium
 * Copyright (C) 2021 krusic22
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.krusic22.offium;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;


public class Offium extends Plugin implements Listener {

    static final Pattern valid = Pattern.compile("[a-zA-Z0-9_]*");
    static final int MAX_USERNAME_LENGHT = 16;
    static final int MIN_USERNAME_LENGHT = 3;
    static final int HOSTNAME_LENGHT = 4;

    @Override
    public void onEnable() {
        getLogger().warning("You really shouldn't be using this.");
        getLogger().warning("Unless you drive a Volvo.");
        final PluginManager p = getProxy().getPluginManager();
        p.registerListener(this, this);
    }

    @Override
    public void onDisable() {
        final PluginManager p = getProxy().getPluginManager();
        p.unregisterListeners(this);
        getLogger().warning("Offium disabled.");
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void PreLoginEvent(PreLoginEvent event) {
        if (event.isCancelled()) {
            return;
        }

        // Get connection
        final PendingConnection connection = event.getConnection();
        final String hostname = connection.getVirtualHost().getHostName();
        final String[] hostarr = hostname.split("\\.");
        if (hostarr.length == HOSTNAME_LENGHT && "offium".equalsIgnoreCase(hostarr[1])) {
            final String username = hostarr[0];

            if (valid.matcher(username).matches() && (username.length() >= MIN_USERNAME_LENGHT) && (username.length() <= MAX_USERNAME_LENGHT)) {
                try {
                    //Get field name from InitialHandler class
                    final Field field = InitialHandler.class.getDeclaredField("name");
                    //Funny Java Private
                    field.setAccessible(true);
                    field.set(connection, username);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    event.setCancelReason(new ComponentBuilder("Error setting username.").color(ChatColor.RED).bold(true).create());
                    event.setCancelled(true);
                    getLogger().severe("Error setting username.");
                }
                final UUID offlineuuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
                // Not required, just to prevent other plugins from doing BS.
                connection.setOnlineMode(false);
                connection.setUniqueId(offlineuuid);
                getLogger().info(username + " joined with UUID: " + offlineuuid);
            } else {
                event.setCancelReason(new ComponentBuilder("Invalid username provided.").color(ChatColor.RED).bold(true).create());
                event.setCancelled(true);
                getLogger().info("Invalid username provided.");
            }
        }
    }
}
