package me.ego.ezbd.lib.fo.jsonsimple;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public class JSONFormatter {
    private static final String CRLF = "\r\n";
    private static final String LF = "\n";
    private int indent = 1;
    private char indentCharacter;
    private String lineBreak = "\n";

    public JSONFormatter() {
        this.setUseTabs(true);
    }

    private final void writeIndent(int level, Writer writer) throws IOException {
        for(int currentLevel = 0; currentLevel < level; ++currentLevel) {
            for(int indent = 0; indent < this.indent; ++indent) {
                writer.write(this.indentCharacter);
            }
        }

    }

    public void setUseCRLF(boolean crlf) {
        this.lineBreak = crlf ? "\r\n" : "\n";
    }

    public void setUseTabs(boolean tabs) {
        this.indentCharacter = (char)(tabs ? 9 : 32);
    }

    public void setIndent(int indent) {
        this.indent = indent;
    }

    public void format(Reader reader, Writer writer) throws IOException {
        int level = 0;
        boolean inString = false;
        int read = true;
        char lastChar = 0;

        while(true) {
            int read;
            while((read = reader.read()) != -1) {
                char character = (char)read;
                if (character == '"') {
                    inString = !inString || lastChar == '\\';
                }

                if (!inString) {
                    label59: {
                        if (character != '{' && character != '[') {
                            if (character != '}' && character != ']') {
                                if (character == ',') {
                                    writer.write(character);
                                    writer.write(this.lineBreak);
                                    this.writeIndent(level, writer);
                                    continue;
                                }

                                if (character == ':') {
                                    writer.write(character);
                                    writer.write(32);
                                    continue;
                                }
                                break label59;
                            }

                            writer.write(this.lineBreak);
                            --level;
                            this.writeIndent(level, writer);
                            writer.write(character);
                            continue;
                        }

                        writer.write(character);
                        writer.write(this.lineBreak);
                        ++level;
                        this.writeIndent(level, writer);
                        continue;
                    }
                }

                writer.write(character);
                lastChar = character;
            }

            return;
        }
    }

    public String format(String json) {
        try {
            StringReader reader = new StringReader(json);
            Throwable var3 = null;

            Object var6;
            try {
                StringWriter writer = new StringWriter();
                Throwable var5 = null;

                try {
                    this.format(reader, writer);
                    var6 = writer.toString();
                } catch (Throwable var31) {
                    var6 = var31;
                    var5 = var31;
                    throw var31;
                } finally {
                    if (writer != null) {
                        if (var5 != null) {
                            try {
                                writer.close();
                            } catch (Throwable var30) {
                                var5.addSuppressed(var30);
                            }
                        } else {
                            writer.close();
                        }
                    }

                }
            } catch (Throwable var33) {
                var3 = var33;
                throw var33;
            } finally {
                if (reader != null) {
                    if (var3 != null) {
                        try {
                            reader.close();
                        } catch (Throwable var29) {
                            var3.addSuppressed(var29);
                        }
                    } else {
                        reader.close();
                    }
                }

            }

            return (String)var6;
        } catch (IOException var35) {
            return null;
        }
    }

    public void minimize(Reader reader, Writer writer) throws IOException {
        boolean inString = false;
        char lastChar = 0;

        char character;
        int read;
        for(boolean var5 = true; (read = reader.read()) != -1; lastChar = character) {
            character = (char)read;
            if (character != '\n' && character != '\t' && character != '\r' && character != '\b' && character != 0 && character != '\f') {
                if (character == '"') {
                    inString = !inString || lastChar == '\\';
                }

                if (character != ' ' || inString) {
                    writer.write(character);
                }
            }
        }

    }

    public String minimize(String json) {
        try {
            StringReader reader = new StringReader(json);
            Throwable var3 = null;

            Object var6;
            try {
                StringWriter writer = new StringWriter();
                Throwable var5 = null;

                try {
                    this.minimize(reader, writer);
                    var6 = writer.toString();
                } catch (Throwable var31) {
                    var6 = var31;
                    var5 = var31;
                    throw var31;
                } finally {
                    if (writer != null) {
                        if (var5 != null) {
                            try {
                                writer.close();
                            } catch (Throwable var30) {
                                var5.addSuppressed(var30);
                            }
                        } else {
                            writer.close();
                        }
                    }

                }
            } catch (Throwable var33) {
                var3 = var33;
                throw var33;
            } finally {
                if (reader != null) {
                    if (var3 != null) {
                        try {
                            reader.close();
                        } catch (Throwable var29) {
                            var3.addSuppressed(var29);
                        }
                    } else {
                        reader.close();
                    }
                }

            }

            return (String)var6;
        } catch (IOException var35) {
            return null;
        }
    }

    public boolean usesCRLF() {
        return "\r\n".equals(this.lineBreak);
    }

    public boolean usesTabs() {
        return this.indentCharacter == '\t';
    }

    public int getIndent() {
        return this.indent;
    }
}