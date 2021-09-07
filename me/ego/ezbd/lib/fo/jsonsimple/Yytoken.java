package me.ego.ezbd.lib.fo.jsonsimple;

public class Yytoken {
    public static final int TYPE_VALUE = 0;
    public static final int TYPE_LEFT_BRACE = 1;
    public static final int TYPE_RIGHT_BRACE = 2;
    public static final int TYPE_LEFT_SQUARE = 3;
    public static final int TYPE_RIGHT_SQUARE = 4;
    public static final int TYPE_COMMA = 5;
    public static final int TYPE_COLON = 6;
    public static final int TYPE_EOF = -1;
    public int type;
    public Object value;

    public Yytoken(int type, Object value) {
        this.type = type;
        this.value = value;
    }
}