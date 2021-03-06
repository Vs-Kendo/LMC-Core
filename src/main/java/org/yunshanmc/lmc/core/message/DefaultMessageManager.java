/*
 * Author: Yun-Shan
 * Date: 2017/07/20
 */
package org.yunshanmc.lmc.core.message;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yunshanmc.lmc.core.LMCPlugin;
import org.yunshanmc.lmc.core.config.ConfigManager;
import org.yunshanmc.lmc.core.internal.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认信息管理器
 */
public class DefaultMessageManager implements MessageManager {

    protected final LMCPlugin plugin;
    protected final ConfigManager configManager;

    protected MessageContext context;

    private Map<String, Message> messageCache = new HashMap<>();
    private FileConfiguration defaultMsg;
    private int debugLevel;

    private static final String MESSAGE_PATH = "messages.yml";

    public DefaultMessageManager(LMCPlugin plugin, ConfigManager configManager) {
        this(plugin, configManager, MESSAGE_PATH);
    }

    public DefaultMessageManager(LMCPlugin plugin, ConfigManager configManager, String defMsgPath) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.defaultMsg = configManager.getDefaultConfig(defMsgPath);
        // 避免空指针
        if (this.defaultMsg == null) this.defaultMsg = new YamlConfiguration();

        this.context = new MessageContext(plugin, this);
    }

    @Override
    public MessageContext getContext() {
        return this.context;
    }

    @Override
    public MessageSender getMessageSender() {
        return new DefaultMessageSender(this).setDebugLevel(this.getDebugLevel());
    }
    @Override
    public Message getMessage(String key) {
        return this.getMessage(key, null);
    }

    @Override
    public Message getMessage(String key, MessageContext context) {
        if (context == null) context = this.context;
        Message message = this.messageCache.get(key);
        if (message == null) {
            message = this.getMessageFromResource(key, context);
            if (message != null) this.messageCache.put(key, message);
        }
        if (message == null && !key.startsWith("__internal.")) {
            LMCPlugin lmcCore = Utils.getLMCCorePlugin();
            MessageManager coreMsgManager = lmcCore.getMessageManager();

            if (lmcCore == context.getPlugin() || this != coreMsgManager) {
                // 如果是在自己在自己的messageManager里搜索，则尝试搜索__internal
                // 自己的插件在别人的messageManager 或 别的插件在自己的messageManager 里搜索时不尝试__internal
                message = this.getMessage("__internal." + key, context);
            }

            if (message == null || message instanceof Message.MissingMessage) {
                // 自己的__internal未找到且当前不是LMC-Core插件，则尝试LMC-Core提供的公共message
                if (this != coreMsgManager) message = coreMsgManager.getMessage(key, context);
                // __internal的MissingMessage会换成没有__internal.前缀的key的MissingMessage
                else message = new Message.MissingMessage(key);
            }
        }
        if (message == null) return new Message.MissingMessage(key);
        return message;
    }

    @Override
    public void setDebugLevel(int debugLevel) {
        this.debugLevel = debugLevel;
    }

    @Override
    public int getDebugLevel() {
        return this.debugLevel;
    }

    protected Message getMessageFromResource(String key, MessageContext context) {
        FileConfiguration cfg = this.configManager.getUserConfig(MESSAGE_PATH);
        String msg = null;
        // 这段代码感觉特别不优雅，于是写清楚注释
        // 尝试从用户配置获取
        if (cfg != null && cfg.isString(key)) msg = cfg.getString(key);
        // 用户配置文件不存在或配置项不存在，尝试从默认配置获取
        if (msg == null && this.defaultMsg.isString(key)) msg = this.defaultMsg.getString(key);
        // 存在用户配置或默认配置
        if (msg != null) return new Message(msg, context);
        // 用户配置和默认配置都不存在
        return null;
    }
}
