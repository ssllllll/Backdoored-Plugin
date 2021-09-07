package me.ego.ezbd.lib.fo.model;

import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Common;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

class VaultHook {
    private Chat chat;
    private Economy economy;
    private Permission permissions;

    VaultHook() {
        this.setIntegration();
    }

    void setIntegration() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServicesManager().getRegistration(Chat.class);
        RegisteredServiceProvider<Permission> permProvider = Bukkit.getServicesManager().getRegistration(Permission.class);
        if (economyProvider != null) {
            this.economy = (Economy)economyProvider.getProvider();
        }

        if (chatProvider != null) {
            this.chat = (Chat)chatProvider.getProvider();
        }

        if (permProvider != null) {
            this.permissions = (Permission)permProvider.getProvider();
        }

    }

    boolean isChatIntegrated() {
        return this.chat != null;
    }

    boolean isEconomyIntegrated() {
        return this.economy != null;
    }

    String getCurrencyNameSG() {
        return this.economy != null ? Common.getOrEmpty(this.economy.currencyNameSingular()) : "Money";
    }

    String getCurrencyNamePL() {
        return this.economy != null ? Common.getOrEmpty(this.economy.currencyNamePlural()) : "Money";
    }

    double getBalance(Player player) {
        return this.economy != null ? this.economy.getBalance(player) : -1.0D;
    }

    void withdraw(Player player, double amount) {
        if (this.economy != null) {
            this.economy.withdrawPlayer(player.getName(), amount);
        }

    }

    void deposit(Player player, double amount) {
        if (this.economy != null) {
            this.economy.depositPlayer(player.getName(), amount);
        }

    }

    Boolean hasPerm(@NonNull OfflinePlayer player, String perm) {
        if (player == null) {
            throw new NullPointerException("player is marked non-null but is null");
        } else {
            try {
                return this.permissions != null ? perm != null ? this.permissions.playerHas((String)null, player, perm) : true : null;
            } catch (Throwable var4) {
                Common.logTimed(900, "SEVERE: Unable to ask Vault plugin if " + player.getName() + " has " + perm + " permission, returning false. This error only shows every 15 minutes. Run /vault-info and check if your permissions plugin is running correctly.");
                return false;
            }
        }
    }

    Boolean hasPerm(@NonNull String player, String perm) {
        if (player == null) {
            throw new NullPointerException("player is marked non-null but is null");
        } else {
            return this.permissions != null ? perm != null ? this.permissions.has((String)null, player, perm) : true : null;
        }
    }

    Boolean hasPerm(@NonNull String world, @NonNull String player, String perm) {
        if (world == null) {
            throw new NullPointerException("world is marked non-null but is null");
        } else if (player == null) {
            throw new NullPointerException("player is marked non-null but is null");
        } else {
            return this.permissions != null ? perm != null ? this.permissions.has(world, player, perm) : true : null;
        }
    }

    String getPrimaryGroup(Player player) {
        return this.permissions != null ? this.permissions.getPrimaryGroup(player) : "";
    }

    String getPlayerPrefix(Player player) {
        return this.lookupVault(player, VaultHook.VaultPart.PREFIX);
    }

    String getPlayerSuffix(Player player) {
        return this.lookupVault(player, VaultHook.VaultPart.SUFFIX);
    }

    String getPlayerGroup(Player player) {
        return this.lookupVault(player, VaultHook.VaultPart.GROUP);
    }

    private String lookupVault(Player player, VaultHook.VaultPart vaultPart) {
        if (this.chat == null) {
            return "";
        } else {
            String[] groups = this.chat.getPlayerGroups(player);
            String fallback = vaultPart == VaultHook.VaultPart.PREFIX ? this.chat.getPlayerPrefix(player) : (vaultPart == VaultHook.VaultPart.SUFFIX ? this.chat.getPlayerSuffix(player) : (groups != null && groups.length > 0 ? groups[0] : ""));
            if (fallback == null) {
                fallback = "";
            }

            if (vaultPart != VaultHook.VaultPart.PREFIX && vaultPart != VaultHook.VaultPart.SUFFIX) {
                List<String> list = new ArrayList();
                if (!fallback.isEmpty()) {
                    list.add(fallback);
                }

                if (groups != null) {
                    String[] var6 = groups;
                    int var7 = groups.length;

                    for(int var8 = 0; var8 < var7; ++var8) {
                        String group = var6[var8];
                        String part = vaultPart == VaultHook.VaultPart.PREFIX ? this.chat.getGroupPrefix(player.getWorld(), group) : (vaultPart == VaultHook.VaultPart.SUFFIX ? this.chat.getGroupSuffix(player.getWorld(), group) : group);
                        if (part != null && !part.isEmpty() && !list.contains(part)) {
                            list.add(part);
                        }
                    }
                }

                return StringUtils.join(list, vaultPart == VaultHook.VaultPart.GROUP ? ", " : "");
            } else {
                return fallback;
            }
        }
    }

    static enum VaultPart {
        PREFIX,
        SUFFIX,
        GROUP;

        private VaultPart() {
        }
    }
}