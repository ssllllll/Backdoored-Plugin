package me.ego.ezbd.lib.fo.jsonsimple;

public class JSONParseException extends Exception {
    private static final long serialVersionUID = -7880698968187728547L;
    public static final int ERROR_UNEXPECTED_CHAR = 0;
    public static final int ERROR_UNEXPECTED_TOKEN = 1;
    public static final int ERROR_UNEXPECTED_EXCEPTION = 2;
    private final int errorType;
    private final Object unexpectedObject;
    private final int position;

    public JSONParseException(int errorType) {
        this(-1, errorType, (Object)null);
    }

    public JSONParseException(int errorType, Object unexpectedObject) {
        this(-1, errorType, unexpectedObject);
    }

    public JSONParseException(int position, int errorType, Object unexpectedObject) {
        this.position = position;
        this.errorType = errorType;
        this.unexpectedObject = unexpectedObject;
    }

    public int getErrorType() {
        return this.errorType;
    }

    public int getPosition() {
        return this.position;
    }

    public Object getUnexpectedObject() {
        return this.unexpectedObject;
    }

    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        switch(this.errorType) {
        case 0:
            builder.append("Unexpected character (").append(this.unexpectedObject).append(") at position ").append(this.position).append(".");
            break;
        case 1:
            builder.append("Unexpected token ").append(this.unexpectedObject).append(" at position ").append(this.position).append(".");
            break;
        case 2:
            builder.append("Unexpected exception at position ").append(this.position).append(": ").append(this.unexpectedObject);
            break;
        default:
            builder.append("Unkown error at position ").append(this.position).append(".");
        }

        return builder.toString();
    }
}
