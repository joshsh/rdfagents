package net.fortytwo.rdfagents.messaging.pubsub;

/**
 * ...note: a subscriber deals with "topics", while a publisher deals with "events" (which may affect topics)...
 * ...a notification is a difference/delta for a given topic based on the latest of a series of events...
 *
 * User: josh
 * Date: 2/22/11
 * Time: 4:49 PM
 */
public interface Publisher<E, N> {
    void notification(N notification);

    void event(E event);

   // boolean
}
