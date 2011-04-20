package net.fortytwo.rdfagents.messaging.push;

import net.fortytwo.rdfagents.model.Agent;
import net.fortytwo.rdfagents.messaging.Role;

/**
 * User: josh
 * Date: 2/25/11
 * Time: 7:41 PM
 */
public abstract class Sender<N> extends Role {
    public Sender(Agent agent) {
        super(agent);
    }

    public abstract void send(Notification<N> note);

    public abstract void send(Notification<N> note,
                              NotificationCallback callback);

    /**
     * A handler for "not understood" responses from the recipient of the notification.
     */
    public interface NotificationCallback {
        public enum Outcome {NOT_UNDERSTOOD, INVALID_RESPONSE}

        /**
         * Indicates failure of the query request.
         *
         * @param outcome     the type of response
         * @param explanation an accompanying message
         */
        void response(Outcome outcome,
                      String explanation);
    }
}
