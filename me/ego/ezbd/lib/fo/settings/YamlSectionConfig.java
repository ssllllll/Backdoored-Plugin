package me.ego.ezbd.lib.fo.settings;

import java.util.Objects;
import me.ego.ezbd.lib.fo.Valid;

public abstract class YamlSectionConfig extends YamlConfig {
    private String localPathPrefix = "";

    protected YamlSectionConfig(String sectionPrefix) {
        super.pathPrefix(sectionPrefix);
    }

    public final boolean isSectionValid() {
        return this.getObject("") != null;
    }

    public final void deleteSection() {
        this.save("", (Object)null);
    }

    public final String getSection() {
        return this.getPathPrefix();
    }

    protected final void pathPrefix(String localPathPrefix) {
        if (localPathPrefix != null) {
            Valid.checkBoolean(!localPathPrefix.endsWith("."), "Path prefix must not end with a dot: " + localPathPrefix, new Object[0]);
        }

        this.localPathPrefix = localPathPrefix != null && !localPathPrefix.isEmpty() ? localPathPrefix : null;
    }

    protected final String formPathPrefix(String myPath) {
        String path = "";
        if (this.getPathPrefix() != null && !this.getPathPrefix().isEmpty()) {
            path = path + this.getPathPrefix() + ".";
        }

        if (this.localPathPrefix != null && !this.localPathPrefix.isEmpty()) {
            path = path + this.localPathPrefix + ".";
        }

        path = path + myPath;
        return path.endsWith(".") ? path.substring(0, path.length() - 1) : path;
    }

    public String toString() {
        return "YamlSection{file=" + this.getFileName() + ", section=" + super.getPathPrefix() + ", local path=" + this.localPathPrefix + "}";
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof YamlSectionConfig)) {
            return false;
        } else {
            YamlSectionConfig c = (YamlSectionConfig)obj;
            return c.getFileName().equals(this.getFileName()) && c.getPathPrefix().equals(this.getPathPrefix()) && Objects.deepEquals(c.localPathPrefix, this.localPathPrefix);
        }
    }
}