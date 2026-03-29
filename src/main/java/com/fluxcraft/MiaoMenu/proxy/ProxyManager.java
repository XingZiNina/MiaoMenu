package com.fluxcraft.MiaoMenu.proxy;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jspecify.annotations.NonNull;

import com.fluxcraft.MiaoMenu.MiaoMenu;

public class ProxyManager implements PluginMessageListener {
    private final MiaoMenu plugin;
    private ProxyType proxyType;
    private static final String BUNGEECORD_CHANNEL = "BungeeCord";

    public ProxyManager(MiaoMenu plugin) {
        this.plugin = plugin;
        this.proxyType = ProxyType.NONE;
    }

    public void initialize() {
        detectProxyType();
        if (proxyType != ProxyType.NONE) {
            plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, BUNGEECORD_CHANNEL, this);
            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, BUNGEECORD_CHANNEL);
            plugin.getLogger().info("检测到代理: " + proxyType + " - 跨服命令已启用");
            if (proxyType == ProxyType.VELOCITY) {
                plugin.getLogger().info("Velocity 模式: 请确保已启用 BungeeCord 插件消息转发");
            }
        } else {
            plugin.getLogger().info("未检测到代理 - 跨服命令已禁用");
        }
    }

    private void detectProxyType() {
        if (plugin.getConfig().getBoolean("settings.velocity-network", false)) {
            proxyType = ProxyType.VELOCITY;
        } else {
            try {
                Class.forName("net.md_5.bungee.api.ChatColor");
                proxyType = ProxyType.BUNGEECORD;
            } catch (ClassNotFoundException ignored) {
                try {
                    Class.forName("com.velocitypowered.api.proxy.ProxyServer");
                    proxyType = ProxyType.VELOCITY;
                } catch (ClassNotFoundException ignored2) {}
            }
        }
    }

    public boolean isProxyConnected() {
        return proxyType != ProxyType.NONE;
    }

    public boolean sendServerCommand(Player player, String serverName) {
        if (proxyType == ProxyType.NONE) {
            plugin.getLogger().warning("未检测到代理，无法发送跨服命令");
            return false;
        }

        try {
            sendBungeeCordServerCommand(player, serverName);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("跨服命令发送失败: " + e.getMessage());
            return false;
        }
    }

    private void sendBungeeCordServerCommand(Player player, String serverName) {
        if (plugin.getServer().getMessenger().isOutgoingChannelRegistered(plugin, BUNGEECORD_CHANNEL)) {
            byte[] message = createBungeeCordServerMessage(serverName);
            player.sendPluginMessage(plugin, BUNGEECORD_CHANNEL, message);
        } else {
            plugin.getLogger().warning("频道 " + BUNGEECORD_CHANNEL + " 未注册!");
        }
    }

    private byte[] createBungeeCordServerMessage(String serverName) {
        try (java.io.ByteArrayOutputStream byteStream = new java.io.ByteArrayOutputStream();
             java.io.DataOutputStream dataOut = new java.io.DataOutputStream(byteStream)) {
            dataOut.writeUTF("Connect");
            dataOut.writeUTF(serverName);
            return byteStream.toByteArray();
        } catch (Exception e) {
            plugin.getLogger().warning("创建 BungeeCord 消息失败: " + e.getMessage());
            return new byte[0];
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, @NonNull Player player, byte @NonNull [] message) {
        if (channel.equals(BUNGEECORD_CHANNEL)) {
            handleBungeeCordResponse(message);
        }
    }

    private void handleBungeeCordResponse(byte[] message) {
        try (java.io.ByteArrayInputStream byteStream = new java.io.ByteArrayInputStream(message);
             java.io.DataInputStream dataIn = new java.io.DataInputStream(byteStream)) {
            String subchannel = dataIn.readUTF();
            plugin.getLogger().fine("收到 BungeeCord 消息: " + subchannel);
        } catch (Exception e) {
            plugin.getLogger().fine("处理 BungeeCord 响应失败: " + e.getMessage());
        }
    }

    public enum ProxyType {
        NONE,
        BUNGEECORD,
        VELOCITY
    }
}
