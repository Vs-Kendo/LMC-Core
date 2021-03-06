package org.yunshanmc.lmc.core.command;

import com.google.common.base.Strings;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class LMCCommand {

    private static final String[] EMPTY_STRINGS = new String[0];

    private final String name;
    private String[] aliases;
    private String usage;
    private String description;
    private String[] permissions;

    private boolean valid;

    /**
     * @param name 命令名
     */
    public LMCCommand(String name) {
        this(name, null, null, null, null);
    }

    /**
     * @param name        命令名
     * @param permissions 命令所需权限
     */
    public LMCCommand(String name, String[] permissions) {
        this(name, null, null, null, permissions);
    }

    /**
     * @param name        命令名
     * @param usage       命令用法
     * @param description 命令描述
     * @param aliases     命令别名
     * @param permissions 命令所需权限
     */
    public LMCCommand(String name, String usage, String description, String[] aliases, String[] permissions) {
        Objects.requireNonNull(name);
        this.name = name;
        this.usage = Strings.nullToEmpty(usage);
        this.description = Strings.nullToEmpty(description);
        BiFunction<String[], Boolean, String[]> filter = (arr, admitEmpty) -> Arrays.stream(aliases != null ? aliases : EMPTY_STRINGS)
                .distinct()
                .filter(alias -> admitEmpty ? alias != null : !Strings.isNullOrEmpty(alias))
                .toArray(String[]::new);
        this.aliases = filter.apply(aliases, true);
        this.permissions = filter.apply(permissions, false);

        this.setValidity(true);
    }

    /**
     * 获取命令名
     *
     * @return 命令名
     */
    public final String getName() {
        return this.name;
    }

    /**
     * 获取命令别名
     *
     * @return 命令别名
     */
    public String[] getAliases() {
        return aliases;
    }

    /**
     * 获取命令用法
     * <p>
     * 一般为顺序列出的参数列表
     *
     * @return 命令用法
     */
    public String getUsage() {
        return this.usage;
    }

    /**
     * 获取命令描述
     *
     * @return 命令描述
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * 获取命令权限
     *
     * @return 命令描述
     */
    public String[] getPermissions() {
        return this.permissions;
    }

    /**
     * 查看命令是否有效
     * <p>
     * 当返回false时命令将不会被调用，并向玩家返回命令被禁用的信息
     *
     * @return 命令有效返回true，否则返回false
     */
    public boolean isValid() {
        return this.valid;
    }

    /**
     * 设置命令有效性
     *
     * @param valid 是否有效
     * @return 是否设置成功
     */
    public final boolean setValidity(boolean valid) {
        if (valid && !this.valid) {// 要设为有效且当前无效
            if (this.tryEnable()) {
                this.valid = true;
                return true;
            }
        } else if (!valid && this.valid) {// 要设为无效且当前有效
            if (this.tryDisable()) {
                this.valid = false;
                return true;
            }
        } else {// 要设置的有效性与当前有效性一致
            return true;
        }
        return false;
    }

    /**
     * 尝试启用命令
     *
     * @return 启用成功返回true，失败返回false
     */
    protected boolean tryEnable() {
        return true;
    }

    /**
     * 尝试禁用命令
     *
     * @return 禁用成功返回true，失败返回false
     */
    protected boolean tryDisable() {
        return true;
    }

    /**
     * 执行命令
     *
     * @param sender 命令执行者
     * @param label  玩家输入的主命令
     * @param args   参数列表
     */
    public abstract void execute(CommandSender sender, String label, String... args);

    /**
     * 显示命令帮助
     *
     * @param sender 显示命令帮助的对象
     */
    public void showHelp(CommandSender sender) {

    }

    /**
     * 执行命令
     *
     * @param sender 命令执行者
     * @param label  玩家输入的主命令
     * @param args   参数列表
     */
    public abstract void execute(net.md_5.bungee.api.CommandSender sender, String label, String... args);

    /**
     * 显示命令帮助
     *
     * @param sender 显示命令帮助的对象
     */
    public void showHelp(net.md_5.bungee.api.CommandSender sender) {

    }
}
