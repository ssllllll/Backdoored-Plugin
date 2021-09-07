package me.ego.ezbd.lib.fo.remain.nbt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class NBTFile extends NBTCompound {
    private final File file;
    private Object nbt;

    public NBTFile(File file) throws IOException {
        super((NBTCompound)null, (String)null);
        if (file == null) {
            throw new NullPointerException("File can't be null!");
        } else {
            this.file = file;
            if (file.exists()) {
                FileInputStream inputsteam = new FileInputStream(file);
                this.nbt = NBTReflectionUtil.readNBT(inputsteam);
            } else {
                this.nbt = WrapperObject.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
                this.save();
            }

        }
    }

    public void save() throws IOException {
        try {
            this.getWriteLock().lock();
            if (!this.file.exists()) {
                this.file.getParentFile().mkdirs();
                if (!this.file.createNewFile()) {
                    throw new IOException("Unable to create file at " + this.file.getAbsolutePath());
                }
            }

            FileOutputStream outStream = new FileOutputStream(this.file);
            NBTReflectionUtil.writeNBT(this.nbt, outStream);
        } finally {
            this.getWriteLock().unlock();
        }

    }

    public File getFile() {
        return this.file;
    }

    public Object getCompound() {
        return this.nbt;
    }

    protected void setCompound(Object compound) {
        this.nbt = compound;
    }
}