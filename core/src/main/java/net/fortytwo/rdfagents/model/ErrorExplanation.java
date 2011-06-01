package net.fortytwo.rdfagents.model;

/**
 * An error explanation to be used in a FIPA not-understood, failure, or refuse message.
 * See the <a href="http://fortytwo.net/2011/rdfagents/spec#errors">RDFAgents specification</a>.
 * User: josh
 * Date: 5/24/11
 * Time: 6:27 PM
 */
public class ErrorExplanation {
    private final Type type;
    private final String message;

    public enum Type {
        ExternalError("external-error"),
        InteractionExplired("interaction-expired"),
        InternalError("internal-error"),
        InvalidMessage("invalid-message"),
        NotImplemented("not-implemented"),
        Unavailable("unavailable");

        private final String fipaName;

        private Type(final String fipaName) {
            this.fipaName = fipaName;
        }

        public String getFipaName() {
            return fipaName;
        }

        public static Type getByFipaName(final String fipaName) {
            for (Type t : values()) {
                if (t.fipaName.equals(fipaName)) {
                    return t;
                }
            }

            return null;
        }
    }

    public ErrorExplanation(final Type type,
                            final String message) {
        this.type = type;
        this.message = message;

        if (null == type) {
            throw new IllegalArgumentException("null type");
        }

        if (null == message) {
            throw new IllegalArgumentException("null message");
        }

        if (0 == message.length()) {
            throw new IllegalArgumentException("empty message");
        }
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String toString() {
        return type + "(" + message + ")";
    }
}
