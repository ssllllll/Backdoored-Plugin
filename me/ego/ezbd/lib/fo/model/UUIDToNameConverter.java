package me.ego.ezbd.lib.fo.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.Callable;

public class UUIDToNameConverter implements Callable<String> {
    private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private final Gson gson = new Gson();
    private final UUID uuid;

    public String call() throws Exception {
        HttpURLConnection connection = (HttpURLConnection)(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + this.uuid.toString().replace("-", ""))).openConnection();
        JsonObject response = (JsonObject)this.gson.fromJson(new InputStreamReader(connection.getInputStream()), JsonObject.class);
        String name = response.get("name").getAsString();
        if (name == null) {
            return "";
        } else {
            String cause = response.get("cause").getAsString();
            String errorMessage = response.get("errorMessage").getAsString();
            if (cause != null && cause.length() > 0) {
                throw new IllegalStateException(errorMessage);
            } else {
                return name;
            }
        }
    }

    public UUIDToNameConverter(UUID uuid) {
        this.uuid = uuid;
    }
}