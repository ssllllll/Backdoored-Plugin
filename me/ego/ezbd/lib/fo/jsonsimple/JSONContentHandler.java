package me.ego.ezbd.lib.fo.jsonsimple;

import java.io.IOException;

public interface JSONContentHandler {
    void startJSON() throws JSONParseException, IOException;

    void endJSON() throws JSONParseException, IOException;

    boolean startObject() throws JSONParseException, IOException;

    boolean endObject() throws JSONParseException, IOException;

    boolean startObjectEntry(String var1) throws JSONParseException, IOException;

    boolean endObjectEntry() throws JSONParseException, IOException;

    boolean startArray() throws JSONParseException, IOException;

    boolean endArray() throws JSONParseException, IOException;

    boolean primitive(Object var1) throws JSONParseException, IOException;
}
