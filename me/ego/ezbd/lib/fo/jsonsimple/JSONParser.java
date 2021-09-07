package me.ego.ezbd.lib.fo.jsonsimple;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import me.ego.ezbd.lib.fo.exception.FoException;

public class JSONParser {
    private static final JSONParser instance = new JSONParser();
    private static final int S_INIT = 0;
    private static final int S_IN_FINISHED_VALUE = 1;
    private static final int S_IN_OBJECT = 2;
    private static final int S_IN_ARRAY = 3;
    private static final int S_PASSED_PAIR_KEY = 4;
    private static final int S_IN_PAIR_VALUE = 5;
    private static final int S_END = 6;
    private static final int S_IN_ERROR = -1;
    private Stack<Object> handlerStatusStack;
    private final Yylex lexer = new Yylex((Reader)null);
    private Yytoken token;
    private int status = 0;

    public JSONParser() {
    }

    private final void nextToken() throws JSONParseException, IOException {
        this.token = this.lexer.yylex();
        if (this.token == null) {
            this.token = new Yytoken(-1, (Object)null);
        }

    }

    private final void init(Stack<Object> statusStack, Stack<Object> valueStack) {
        if (this.token.type == 0) {
            this.status = 1;
            statusStack.push(this.status);
            valueStack.push(this.token.value);
        } else if (this.token.type == 1) {
            this.status = 2;
            statusStack.push(this.status);
            valueStack.push(new JSONObject());
        } else if (this.token.type == 3) {
            this.status = 3;
            statusStack.push(this.status);
            valueStack.push(new JSONArray());
        } else {
            this.status = -1;
        }

    }

    private final Object inFinishedValue(Stack<Object> valueStack) throws JSONParseException {
        if (this.token.type == -1) {
            return valueStack.pop();
        } else {
            throw new JSONParseException(this.getPosition(), 1, this.token);
        }
    }

    private final void inObject(Stack<Object> statusStack, Stack<Object> valueStack) {
        if (this.token.type == 0) {
            if (this.token.value instanceof String) {
                String key = (String)this.token.value;
                valueStack.push(key);
                this.status = 4;
                statusStack.push(this.status);
            } else {
                this.status = -1;
            }
        } else if (this.token.type == 2) {
            if (valueStack.size() > 1) {
                statusStack.pop();
                valueStack.pop();
                this.status = (Integer)statusStack.peek();
            } else {
                this.status = 1;
            }
        } else if (this.token.type != 5) {
            this.status = -1;
        }

    }

    private final void inPassedPairKey(Stack<Object> statusStack, Stack<Object> valueStack) {
        String key;
        Map parent;
        if (this.token.type == 0) {
            statusStack.pop();
            key = (String)valueStack.pop();
            parent = (Map)valueStack.peek();
            parent.put(key, this.token.value);
            this.status = (Integer)statusStack.peek();
        } else if (this.token.type == 3) {
            statusStack.pop();
            key = (String)valueStack.pop();
            parent = (Map)valueStack.peek();
            List<Object> newArray = new JSONArray();
            parent.put(key, newArray);
            this.status = 3;
            statusStack.push(this.status);
            valueStack.push(newArray);
        } else if (this.token.type == 1) {
            statusStack.pop();
            key = (String)valueStack.pop();
            parent = (Map)valueStack.peek();
            Map<Object, Object> newObject = new JSONObject();
            parent.put(key, newObject);
            this.status = 2;
            statusStack.push(this.status);
            valueStack.push(newObject);
        } else if (this.token.type != 6) {
            this.status = -1;
        }

    }

    private final void inArray(Stack<Object> statusStack, Stack<Object> valueStack) {
        List val;
        if (this.token.type == 0) {
            val = (List)valueStack.peek();
            val.add(this.token.value);
        } else if (this.token.type == 4) {
            if (valueStack.size() > 1) {
                statusStack.pop();
                valueStack.pop();
                this.status = (Integer)statusStack.peek();
            } else {
                this.status = 1;
            }
        } else if (this.token.type == 1) {
            val = (List)valueStack.peek();
            Map<Object, Object> newObject = new JSONObject();
            val.add(newObject);
            this.status = 2;
            statusStack.push(this.status);
            valueStack.push(newObject);
        } else if (this.token.type == 3) {
            val = (List)valueStack.peek();
            List<Object> newArray = new JSONArray();
            val.add(newArray);
            this.status = 3;
            statusStack.push(this.status);
            valueStack.push(newArray);
        } else if (this.token.type != 5) {
            this.status = -1;
        }

    }

    public void reset() {
        this.token = null;
        this.status = 0;
        this.handlerStatusStack = null;
    }

    public void reset(Reader reader) {
        this.lexer.yyreset(reader);
        this.reset();
    }

    public int getPosition() {
        return this.lexer.getPosition();
    }

    public Object parse(String json) throws JSONParseException {
        try {
            StringReader reader = new StringReader(json);
            Throwable var3 = null;

            Object var4;
            try {
                var4 = this.parse((Reader)reader);
            } catch (Throwable var14) {
                var3 = var14;
                throw var14;
            } finally {
                if (reader != null) {
                    if (var3 != null) {
                        try {
                            reader.close();
                        } catch (Throwable var13) {
                            var3.addSuppressed(var13);
                        }
                    } else {
                        reader.close();
                    }
                }

            }

            return var4;
        } catch (IOException var16) {
            throw new FoException(var16);
        }
    }

    public Object parse(Reader reader) throws IOException, JSONParseException {
        this.reset(reader);
        Stack<Object> statusStack = new Stack();
        Stack valueStack = new Stack();

        do {
            this.nextToken();
            if (this.status == 0) {
                this.init(statusStack, valueStack);
            } else {
                if (this.status == 1) {
                    return this.inFinishedValue(valueStack);
                }

                if (this.status == 2) {
                    this.inObject(statusStack, valueStack);
                } else if (this.status == 4) {
                    this.inPassedPairKey(statusStack, valueStack);
                } else if (this.status == 3) {
                    this.inArray(statusStack, valueStack);
                }
            }

            if (this.status == -1) {
                throw new JSONParseException(this.getPosition(), 1, this.token);
            }
        } while(this.token.type != -1);

        throw new JSONParseException(this.getPosition(), 1, this.token);
    }

    public void parse(String string, JSONContentHandler contentHandler) throws JSONParseException {
        this.parse(string != null ? string.trim() : null, contentHandler, false);
    }

    public void parse(String string, JSONContentHandler contentHandler, boolean resume) throws JSONParseException {
        try {
            StringReader reader = new StringReader(string != null ? string.trim() : null);
            Throwable var5 = null;

            try {
                this.parse((Reader)reader, contentHandler, resume);
            } catch (Throwable var15) {
                var5 = var15;
                throw var15;
            } finally {
                if (reader != null) {
                    if (var5 != null) {
                        try {
                            reader.close();
                        } catch (Throwable var14) {
                            var5.addSuppressed(var14);
                        }
                    } else {
                        reader.close();
                    }
                }

            }

        } catch (IOException var17) {
            throw new JSONParseException(-1, 2, var17);
        }
    }

    public void parse(Reader reader, JSONContentHandler contentHandler) throws IOException, JSONParseException {
        this.parse(reader, contentHandler, false);
    }

    public void parse(Reader reader, JSONContentHandler contentHandler, boolean resume) throws IOException, JSONParseException {
        if (!resume) {
            this.reset(reader);
            this.handlerStatusStack = new Stack();
        } else if (this.handlerStatusStack == null) {
            this.reset(reader);
            this.handlerStatusStack = new Stack();
        }

        Stack statusStack = this.handlerStatusStack;

        while(true) {
            try {
                if (this.status == 0) {
                    contentHandler.startJSON();
                    this.nextToken();
                    if (this.token.type == 0) {
                        this.status = 1;
                        statusStack.push(this.status);
                        if (!contentHandler.primitive(this.token.value)) {
                            return;
                        }
                    } else if (this.token.type == 1) {
                        this.status = 2;
                        statusStack.push(this.status);
                        if (!contentHandler.startObject()) {
                            return;
                        }
                    } else if (this.token.type == 3) {
                        this.status = 3;
                        statusStack.push(this.status);
                        if (!contentHandler.startArray()) {
                            return;
                        }
                    } else {
                        this.status = -1;
                    }
                } else if (this.status == 1) {
                    this.nextToken();
                    if (this.token.type == -1) {
                        contentHandler.endJSON();
                        this.status = 6;
                        return;
                    }

                    this.status = -1;
                } else if (this.status == 2) {
                    this.nextToken();
                    if (this.token.type == 0) {
                        if (this.token.value instanceof String) {
                            String key = (String)this.token.value;
                            this.status = 4;
                            statusStack.push(this.status);
                            if (!contentHandler.startObjectEntry(key)) {
                                return;
                            }
                        } else {
                            this.status = -1;
                        }
                    } else if (this.token.type == 2) {
                        if (statusStack.size() > 1) {
                            statusStack.pop();
                            this.status = (Integer)statusStack.peek();
                        } else {
                            this.status = 1;
                        }

                        if (!contentHandler.endObject()) {
                            return;
                        }
                    } else if (this.token.type != 5) {
                        this.status = -1;
                    }
                } else if (this.status == 4) {
                    this.nextToken();
                    if (this.token.type == 0) {
                        statusStack.pop();
                        this.status = (Integer)statusStack.peek();
                        if (!contentHandler.primitive(this.token.value) || !contentHandler.endObjectEntry()) {
                            return;
                        }
                    } else if (this.token.type == 3) {
                        statusStack.pop();
                        statusStack.push(5);
                        this.status = 3;
                        statusStack.push(this.status);
                        if (!contentHandler.startArray()) {
                            return;
                        }
                    } else if (this.token.type == 1) {
                        statusStack.pop();
                        statusStack.push(5);
                        this.status = 2;
                        statusStack.push(this.status);
                        if (!contentHandler.startObject()) {
                            return;
                        }
                    } else if (this.token.type != 6) {
                        this.status = -1;
                    }
                } else if (this.status == 5) {
                    statusStack.pop();
                    this.status = (Integer)statusStack.peek();
                    if (!contentHandler.endObjectEntry()) {
                        return;
                    }
                } else if (this.status == 3) {
                    this.nextToken();
                    if (this.token.type == 0) {
                        if (!contentHandler.primitive(this.token.value)) {
                            return;
                        }
                    } else if (this.token.type == 4) {
                        if (statusStack.size() > 1) {
                            statusStack.pop();
                            this.status = (Integer)statusStack.peek();
                        } else {
                            this.status = 1;
                        }

                        if (!contentHandler.endArray()) {
                            return;
                        }
                    } else if (this.token.type == 1) {
                        this.status = 2;
                        statusStack.push(this.status);
                        if (!contentHandler.startObject()) {
                            return;
                        }
                    } else if (this.token.type == 3) {
                        this.status = 3;
                        statusStack.push(this.status);
                        if (!contentHandler.startArray()) {
                            return;
                        }
                    } else if (this.token.type != 5) {
                        this.status = -1;
                    }
                } else if (this.status == 6) {
                    return;
                }

                if (this.status == -1) {
                    throw new JSONParseException(this.getPosition(), 1, this.token);
                }

                if (this.token.type != -1) {
                    continue;
                }
            } catch (JSONParseException | RuntimeException | Error | IOException var6) {
                this.status = -1;
                throw var6;
            }

            this.status = -1;
            throw new JSONParseException(this.getPosition(), 1, this.token);
        }
    }

    public static JSONParser getInstance() {
        return instance;
    }
}