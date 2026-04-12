package com.fluxcraft.MiaoMenu.proxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jspecify.annotations.NonNull;

import com.fluxcraft.MiaoMenu.MiaoMenu;
import com.fluxcraft.MiaoMenu.utils.Lang;

public class ProxyManager implements PluginMessageListener {
    private static final String BUNGEECORD_CHANNEL = "BungeeCord";

    private final MiaoMenu plugin;
    private ProxyType proxyType;

    public ProxyManager(MiaoMenu plugin) {
        this.plugin = plugin;
        this.proxyType = ProxyType.NONE;
    }

    public void initialize() {
        detectProxyType();
        if (proxyType != ProxyType.NONE) {
            plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, BUNGEECORD_CHANNEL, this);
            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, BUNGEECORD_CHANNEL);
            plugin.getLogger().info(Lang.get("log.proxy.detected").replace("{0}", proxyType.name()));
            if (proxyType == ProxyType.VELOCITY) {
                plugin.getLogger().info(Lang.get("log.proxy.velocity-forwarding-required"));
            }
        } else {
            plugin.getLogger().info(Lang.get("log.proxy.disabled"));
        }
    }

    private void detectProxyType() {
        if (plugin.getConfig().getBoolean("settings.velocity-network", false)) {
            proxyType = ProxyType.VELOCITY;
            return;
        }
        try {
            Class.forName("net.md_5.bungee.api.ChatColor");
            proxyType = ProxyType.BUNGEECORD;
        } catch (ClassNotFoundException ignored) {
            try {
                Class.forName("com.velocitypowered.api.proxy.ProxyServer");
                proxyType = ProxyType.VELOCITY;
            } catch (ClassNotFoundException ignored2) {
                proxyType = ProxyType.NONE;
            }
        }
    }

    public boolean isProxyConnected() {
        return proxyType != ProxyType.NONE;
    }

    public boolean sendServerCommand(Player player, String serverName) {
        if (proxyType == ProxyType.NONE) {
            plugin.getLogger().warning(Lang.get("log.proxy.not-detected"));
            return false;
        }
        try {
            sendBungeeCordServerCommand(player, serverName);
            return true;
        } catch (IllegalStateException e) {
            plugin.getLogger().log(Level.WARNING, Lang.get("log.proxy.send-failed").replace("{0}", serverName), e);
            return false;
        }
    }

    private void sendBungeeCordServerCommand(Player player, String serverName) {
        if (!plugin.getServer().getMessenger().isOutgoingChannelRegistered(plugin, BUNGEECORD_CHANNEL)) {
            throw new IllegalStateException(Lang.get("log.proxy.channel-not-registered").replace("{0}", BUNGEECORD_CHANNEL));
        }
        byte[] message = createBungeeCordServerMessage(serverName);
        player.sendPluginMessage(plugin, BUNGEECORD_CHANNEL, message);
    }

    private byte[] createBungeeCordServerMessage(String serverName) {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream dataOut = new DataOutputStream(byteStream)) {
            dataOut.writeUTF("Connect");
            dataOut.writeUTF(serverName);
            return byteStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(Lang.get("log.proxy.message-create-failed").replace("{0}", serverName), e);
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, @NonNull Player player, byte @NonNull [] message) {
        if (channel.equals(BUNGEECORD_CHANNEL)) {
            handleBungeeCordResponse(message);
        }
    }

    private void handleBungeeCordResponse(byte[] message) {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(message);
             DataInputStream dataIn = new DataInputStream(byteStream)) {
            String subchannel = dataIn.readUTF();
            plugin.getLogger().fine(Lang.get("log.proxy.message-received").replace("{0}", subchannel));
        } catch (IOException e) {
            plugin.getLogger().log(Level.FINE, Lang.get("log.proxy.response-handle-failed"), e);
        }
    }

    public enum ProxyType {
        NONE,
        BUNGEECORD,
        VELOCITY
    }
}
