package net.fortytwo.rdfagents.messaging.pubsub;

import java.util.Collection;

/**
 * ... for the publish/subscribe messaging pattern.
 *
 * User: josh
 * Date: 2/22/11
 * Time: 4:49 PM
 */
public interface Subscriber {
    Collection<Subscription> getSubscriptions();
}
