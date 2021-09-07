package me.ego.ezbd.lib.fo.model;

import java.util.Iterator;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.remain.CompSound;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class SimpleSound {
    @NonNull
    private Sound sound;
    private float volume;
    private float pitch;
    private boolean randomPitch;
    private boolean enabled;

    public SimpleSound(Sound sound, float volume, float pitch) {
        this(sound, volume, pitch, false, true);
    }

    public SimpleSound(Sound sound, float volume) {
        this(sound, volume, 1.0F, true, true);
    }

    public SimpleSound(String line) {
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.randomPitch = false;
        this.enabled = true;
        Valid.checkNotNull(line);
        if ("none".equals(line)) {
            this.sound = CompSound.CLICK.getSound();
            this.volume = 0.0F;
            this.enabled = false;
        } else {
            String[] values = line.contains(", ") ? line.split(", ") : line.split(" ");

            try {
                this.sound = CompSound.convert(values[0]);
            } catch (IllegalArgumentException var5) {
                Common.throwError(var5, new String[]{"Sound '" + values[0] + "' does not exists (in your Minecraft version)!", "Notice: Sound names has changed as per 1.9. See:", "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html"});
            }

            if (values.length == 1) {
                this.volume = 1.0F;
                this.pitch = 1.5F;
            } else {
                Valid.checkBoolean(values.length == 3, "Malformed sound type, use format: 'sound' OR 'sound volume pitch'. Got: " + line, new Object[0]);
                Valid.checkNotNull(this.sound, "Unable to parse sound from: " + line);
                String volumeRaw = values[1];
                String pitchRaw = values[2];
                this.volume = Float.parseFloat(volumeRaw);
                if ("random".equals(pitchRaw)) {
                    this.pitch = 1.0F;
                    this.randomPitch = true;
                } else {
                    this.pitch = Float.parseFloat(pitchRaw);
                }

            }
        }
    }

    public void play(Iterable<Player> players) {
        if (this.enabled) {
            Iterator var2 = players.iterator();

            while(var2.hasNext()) {
                Player player = (Player)var2.next();
                this.play(player);
            }
        }

    }

    public void play(Player player) {
        if (this.enabled) {
            Valid.checkNotNull(this.sound);
            player.playSound(player.getLocation(), this.sound, this.volume, this.getPitch());
        }

    }

    public void play(Location location) {
        if (this.enabled) {
            Valid.checkNotNull(this.sound);
            location.getWorld().playSound(location, this.sound, this.volume, this.getPitch());
        }

    }

    public float getPitch() {
        return this.randomPitch ? (float)Math.random() : this.pitch;
    }

    public String toString() {
        return this.enabled ? this.sound + " " + this.volume + " " + (this.randomPitch ? "random" : this.pitch) : "none";
    }

    @NonNull
    public Sound getSound() {
        return this.sound;
    }

    public float getVolume() {
        return this.volume;
    }

    public boolean isRandomPitch() {
        return this.randomPitch;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    private SimpleSound(@NonNull Sound sound, float volume, float pitch, boolean randomPitch, boolean enabled) {
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.randomPitch = false;
        this.enabled = true;
        if (sound == null) {
            throw new NullPointerException("sound is marked non-null but is null");
        } else {
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
            this.randomPitch = randomPitch;
            this.enabled = enabled;
        }
    }
}