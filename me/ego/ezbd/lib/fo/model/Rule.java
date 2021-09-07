package me.ego.ezbd.lib.fo.model;

import java.io.File;

public interface Rule {
    String getUid();

    File getFile();

    boolean onOperatorParse(String[] var1);
}