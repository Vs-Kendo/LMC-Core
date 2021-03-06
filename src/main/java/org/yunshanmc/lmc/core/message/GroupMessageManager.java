package org.yunshanmc.lmc.core.message;

import org.bukkit.configuration.file.FileConfiguration;
import org.yunshanmc.lmc.core.LMCPlugin;
import org.yunshanmc.lmc.core.config.ConfigManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 分组信息管理器
 */
public class GroupMessageManager extends DefaultMessageManager {

    private static final String MESSAGE_DIR = "messages";

    private MessageGroup defMsgGroup;
    private String userMsgPath;

    public GroupMessageManager(LMCPlugin plugin, ConfigManager configManager) {
        this(plugin, configManager, MESSAGE_DIR);
    }

    public GroupMessageManager(LMCPlugin plugin, ConfigManager configManager, String defMsgPath) {
        super(plugin, configManager);
        this.userMsgPath = defMsgPath;
        // init default message name
        Map<String, FileConfiguration> cfgs = configManager.getDefaultConfigs(defMsgPath, true);
        MessageGroup msgGroup = new MessageGroup("", cfgs.remove(""), new HashMap<>());
        int startIdx = defMsgPath.length() + 1;// "xxx" + "/"
        cfgs.forEach((path, cfg) -> {
            MessageGroup group = msgGroup;
            MessageGroup sub;
            if (path.endsWith(".yml")) path = path.substring(startIdx, path.length() - 4);

            for (String key : path.split("(/)|(\\\\)")) {
                sub = group.subGroups.get(key);
                if (sub == null) {
                    sub = new MessageGroup(key, cfg, new HashMap<>());
                    group.subGroups.put(key, sub);
                }
                group = sub;
            }
        });
        this.defMsgGroup = msgGroup;
    }

    @Override
    protected Message getMessageFromResource(String key, MessageContext context) {
        // 根目录的messages.yml的优先级高于messages目录下的分组信息
        Message msg = super.getMessageFromResource(key, context);
        if (msg != null) return msg;

        String path = this.userMsgPath + File.separator + key;
        int firstSep;
        FileConfiguration cfg;
        do {
            char[] chars = path.toCharArray();

            String cfgPath = path.indexOf('.') > 0 ? new String(chars, 0, path.indexOf('.')) : path;
            cfg = this.configManager.getUserConfig(cfgPath + ".yml");

            if (cfg != null
                    // 去除key的组路径部分
                    && cfg.isString(key.substring(path.indexOf('.') - this.userMsgPath.length()))) {
                // 在用户配置中找到
                return new Message(cfg.getString(key.substring(path.indexOf('.') - this.userMsgPath.length())), context);
            }

            // 用户配置中未找到，尝试在默认配置中查找
            if ((firstSep = path.indexOf('.')) == -1) return this.defMsgGroup.getMessage(key, context);

            chars[firstSep] = File.separatorChar;
            path = new String(chars);
        } while (true);
    }

    private class MessageGroup {

        private final String name;
        private final FileConfiguration cfg;
        private final Map<String, MessageGroup> subGroups;

        public MessageGroup(String name, FileConfiguration cfg, Map<String, MessageGroup> subGroups) {
            this.name = name;
            this.cfg = cfg;
            this.subGroups = subGroups;
        }

        public Message getMessage(String key, MessageContext context) {
            Message msg = null;
            if (this.cfg != null && this.cfg.isString(key)) {
                msg = new Message(cfg.getString(key), context);
            } else {// 搜索子组
                int groupSep = key.indexOf('.');
                if (groupSep > 0) {
                    String group = key.substring(0, groupSep);
                    MessageGroup sub = this.subGroups.get(group);
                    if (sub != null) {
                        msg = sub.getMessage(key.substring(groupSep + 1), context);
                    }
                }
            }
            return msg;
        }

        public String getName() {
            return this.name;
        }
    }
}
