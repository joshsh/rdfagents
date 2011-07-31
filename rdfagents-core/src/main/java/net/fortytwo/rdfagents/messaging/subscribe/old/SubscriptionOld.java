package net.fortytwo.rdfagents.messaging.subscribe.old;

import net.fortytwo.rdfagents.model.RDFAgent;

import java.util.Date;

/**
 * A relationship indicating a certain subscriber's interest in receiving notifications matching a given topic from a given publisher,
 * as well as the publisher's commitment to providing those notifications.
 * <p/>
 * ...topic-based (as opposed to content-based) filtering
 * ...similar to PubSubHubbub and XMPP Publish-Subscribe (XEP-0060)...
 * <p/>
 * T: the class of topics
 * N: the class of notifications
 * <p/>
 * TODO: subscription (lease) renewal  -- see PubSubHubbub spec, section 6.1
 *
 * @param <N> a class of notifications
 * @param <T> a class of topic descriptors
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract class SubscriptionOld<N, T> {
    public enum Status {
        INACTIVE,
        PENDING,
        ACTIVE
    }

    private final RDFAgent publisher;
    private final T topic;
    private final NotificationHandler<N> noteHandler;
    private final EndOfStreamHandler eosHandler;

    private long activeSince;
    private long notificationsReceived = 0;

    private Status status = Status.INACTIVE;

    /**
     * Constructs a new subscription.
     * After construction, the activate() method must be called in order to establish a connection with the publisher and begin receiving notifications.
     *
     * @param publisher   the agent from which the client would like to receive notifications
     * @param topic       a topic descriptor for the subscription, defining the notifications in which the client is interested
     * @param noteHandler a callback to handle notifications received from the publisher matching the given topic
     */
    public SubscriptionOld(final RDFAgent publisher,
                           final T topic,
                           final NotificationHandler<N> noteHandler) {
        this.publisher = publisher;
        this.topic = topic;
        this.noteHandler = noteHandler;
        this.eosHandler = null;
    }

    /**
     * Constructs a new subscription.
     * After construction, the activate() method must be called in order to establish a connection with the publisher and begin receiving notifications.
     *
     * @param publisher   the agent from which the client would like to receive notifications
     * @param topic       a topic descriptor for the subscription, defining the notifications in which the client is interested
     * @param noteHandler a callback to handle notifications received from the publisher matching the given topic
     */
    public SubscriptionOld(final RDFAgent publisher,
                           final T topic,
                           final NotificationHandler<N> noteHandler,
                           final EndOfStreamHandler eosHandler) {
        this.publisher = publisher;
        this.topic = topic;
        this.noteHandler = noteHandler;
        this.eosHandler = eosHandler;
    }

    private void setStatus(Status status) {
        if (Status.ACTIVE == status) {
            if (Status.ACTIVE != this.status) {
                activeSince = new Date().getTime();
            }
        } else {
            notificationsReceived = 0;
        }

        this.status = status;
    }

    protected void receiveNotification(final N note) {
        if (Status.ACTIVE == status) {
            notificationsReceived++;
            noteHandler.receive(note);
        }
    }

    /**
     * @return the agent from which the client would like to receive notifications
     */
    public RDFAgent getPublisher() {
        return publisher;
    }

    /**
     * @return a topic descriptor for the subscription, defining the notifications in which the client is interested
     */
    public T getTopic() {
        return topic;
    }

    /**
     * @return the current status of this subscription
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Submits a subscription request to the publisher.
     * This is meant to either establish a new subscription, or extend the lease of an existing subscription.
     * Upon invocation, if the subscription's status is INACTIVE, it advances to PENDING.
     * If it is already ACTIVE, it remains ACTIVE.
     * When a response from the publisher is received, this subscription either becomes ACTIVE or reverts to INACTIVE.
     */
    public abstract void activate();

    /**
     * Submits a subscription request to the publisher.
     * Upon invocation, this subscription's status advances to PENDING.
     * When a response from the publisher is received, this subscription either becomes ACTIVE or reverts to an INACTIVE state.
     *
     * @param handler a callback to handle the publisher's response to the subscription request
     */
    public abstract void activate(SubscriptionCallback handler);

    /**
     * Cancels this subscription, notifying the publisher.
     * Upon invocation, this subscription's status reverts to INACTIVE, and any subsequent notifications are ignored.
     */
    public abstract void cancel();

    /**
     * Cancels this subscription, notifying the publisher.
     * Upon invocation, this subscription's status reverts to INACTIVE, and any subsequent notifications are ignored.
     *
     * @param handler a callback to handle the publisher's response to the cancellation request
     */
    public abstract void cancel(CancellationCallback handler);

    /**
     * A handler for notifications received from the publisher
     *
     * @param <N> the class of notifications
     */
    public interface NotificationHandler<N> {
        void receive(N note);
    }

    /**
     * A handler for the end of the stream of notifications, indicated with a failure message.
     */
    public interface EndOfStreamHandler {
        /**
         * Indicates the end of the stream.
         * By the time this method is called, the subscription should already be in the INACTIVE state.
         * This callback serves an informative purpose, and has no effect on the subscription.
         */
        void failed();
    }

    /**
     * A handler for the publisher's response to a subscription request.
     * This callback serves an informative purpose, and has no effect on the subscription.
     */
    public interface SubscriptionCallback {
        /**
         * Indicates that the publisher has accepted the subscription request and will send notifications matching the given topic.
         */
        void accepted();

        /**
         * Indicates that the publisher has refused the subscription request.
         */
        void refused();

        /**
         * Indicates that the subscription request has failed due to a server error.
         */
        void failed();
    }

    /**
     * A handler for the publisher's response to a subscription cancellation.
     * This callback serves an informative purpose, and has no effect on the subscription.
     */
    public interface CancellationCallback {
        /**
         * Indicates that the publisher has acknowledged the cancellation request and will stop sending notifications matching the given topic.
         */
        void acknowledged();

        /**
         * Indicates that the cancellation request has failed due to a server error.
         */
        void failed();
    }

    /**
     * @return the number of milliseconds since this subscription became ACTIVE, according to the system clock.
     *         If this subscription is not currently active, a value of 0 is returned.
     */
    public long getUptime() {
        return Status.ACTIVE == status
                ? new Date().getTime() - activeSince
                : 0;
    }

    /**
     * @return the number of notifications which have been received since this subscription (last) became active.
     * This value is subject to overflow if (very) many notifications are received.
     */
    public long getReceivedCount() {
        return notificationsReceived;
    }
}
