package org.my;

public class Exp4jException extends RuntimeException {
    private final String token;

    public Exp4jException(String message) {
        super(message);
        this.token = message;
    }
    public Exp4jException(String token, Integer pos) {
        super(buildPositionMsg(pos, token));
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public static class ParseException extends Exp4jException {
        public ParseException(String token) { super(token); }
        public ParseException(String token, Integer pos) {
            super(String.format("unexpected '%s' at pos %d", token, pos));
        }
    }

    public static class EvaluationException extends Exp4jException {
        public EvaluationException(String message) { super(message); }
        public EvaluationException(String token, Integer pos) { super(token, pos); }
    }

    private static String buildPositionMsg(Integer pos, String token) {
        if (token != null) {
            return String.format("position at %s, token '%s'", pos, token);
        } else {
            return String.format("position at %s", pos);
        }
    }
}
