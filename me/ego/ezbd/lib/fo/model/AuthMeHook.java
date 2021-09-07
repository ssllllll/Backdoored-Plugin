package me.ego.ezbd.lib.fo.model;

import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.entity.Player;

class AuthMeHook {
    AuthMeHook() {
    }

    boolean isLogged(Player player) {
        try {
            AuthMeApi instance = AuthMeApi.getInstance();
            return instance.isAuthenticated(player);
        } catch (Throwable var3) {
            return false;
        }
    }
}
