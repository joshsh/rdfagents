package net.fortytwo.rdfagents.messaging.subscribe.old;

/**
 * ...note: a subscriber deals with "topics", while a publisher deals with "events" (which may affect topics)...
 * ...a notification is a difference/delta for a given topic based on the latest of a series of events...
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public interface PublisherOld<E, N> {
    void notification(N notification);

    void event(E event);

   // boolean
}
