package net.fortytwo.rdfagents.jade;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import net.fortytwo.rdfagents.RDFAgents;
import net.fortytwo.rdfagents.messaging.CancellationCallback;
import net.fortytwo.rdfagents.messaging.Commitment;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.MessageNotUnderstoodException;
import net.fortytwo.rdfagents.messaging.MessageRejectedException;
import net.fortytwo.rdfagents.messaging.QueryCallback;
import net.fortytwo.rdfagents.messaging.query.QueryServer;
import net.fortytwo.rdfagents.messaging.subscribe.Publisher;
import net.fortytwo.rdfagents.messaging.subscribe.UpdateHandler;
import net.fortytwo.rdfagents.model.AgentReference;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.ErrorExplanation;
import net.fortytwo.rdfagents.model.RDFContentLanguage;
import org.openrdf.model.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: 5/27/11
 * Time: 10:18 PM
 */
public class RDFJadeAgent extends Agent {
    private static final Logger LOGGER = Logger.getLogger(RDFJadeAgent.class.getName());

    private final Map<String, QueryCallback<Dataset>> queryCallbacks
            = new HashMap<String, QueryCallback<Dataset>>();
    private final Map<String, CancellationCallback> queryCancellationCallbacks
            = new HashMap<String, CancellationCallback>();
    private final Map<String, QueryCallback<Dataset>> subscriptionCallbacks
            = new HashMap<String, QueryCallback<Dataset>>();
    private final Map<String, CancellationCallback> subscriptionCancellationCallbacks
            = new HashMap<String, CancellationCallback>();

    private AgentReference self;
    private MessageFactory messageFactory;
    private QueryServer<Value, Dataset> queryServer;
    private Publisher<Value, Dataset> publisher;

    public abstract class Task {
        private String conversationId;

        public Task() {
        }

        public abstract void execute();

        public String getConversationId() {
            return conversationId;
        }

        public void setConversationId(final String id) {
            this.conversationId = id;
        }
    }

    public Task submitQuery(final Value resource,
                            final AgentReference server,
                            final QueryCallback<Dataset> callback) {
        return new Task() {
            public void execute() {
                String conversationId = null;

                try {
                    ACLMessage m;

                    m = messageFactory.poseQuery(self, server, resource);

                    conversationId = m.getConversationId();
                    setConversationId(conversationId);

                    System.out.println("issuing a query for " + resource);
                    //System.out.println("--> " + m);

                    queryCallbacks.put(conversationId, callback);

                    send(m);
                } catch (LocalFailure e) {
                    forgetConversation(conversationId);
                    callback.localFailure(e);
                } catch (Throwable e) {
                    forgetConversation(conversationId);
                    callback.localFailure(new LocalFailure(e));
                }
            }
        };
    }

    public Task cancelQuery(final String conversationId,
                            final AgentReference server,
                            final CancellationCallback callback) {
        return new Task() {
            public void execute() {
                try {
                    QueryCallback<Dataset> qc = queryCallbacks.get(conversationId);

                    if (null == qc) {
                        LOGGER.info("attempted to cancel Query conversation "
                                + conversationId + ", which has already concluded or does not exist");
                    } else {
                        queryCallbacks.remove(conversationId);

                        ACLMessage m = messageFactory.requestQueryCancellation(self, server, conversationId);

                        setConversationId(m.getConversationId());

                        queryCancellationCallbacks.put(m.getConversationId(), callback);

                        send(m);
                    }
                } catch (Throwable e) {
                    forgetConversation(conversationId);
                    callback.localFailure(new LocalFailure(e));
                }
            }
        };
    }

    public Task subscribe(final Value topic,
                          final AgentReference publisher,
                          final QueryCallback<Dataset> callback) {
        return new Task() {
            public void execute() {
                String conversationId = null;

                try {
                    ACLMessage m;

                    m = messageFactory.requestSubscription(self, publisher, topic);

                    conversationId = m.getConversationId();
                    setConversationId(conversationId);

                    System.out.println("issuing a subscription request for " + topic);
                    //System.out.println("--> " + m);

                    subscriptionCallbacks.put(conversationId, callback);

                    send(m);
                } catch (LocalFailure e) {
                    forgetConversation(conversationId);
                    callback.localFailure(e);
                } catch (Throwable e) {
                    forgetConversation(conversationId);
                    callback.localFailure(new LocalFailure(e));
                }
            }
        };
    }

    public Task cancelSubscription(final String conversationId,
                                   final AgentReference server,
                                   final CancellationCallback callback) {
        return new Task() {
            public void execute() {
                try {
                    QueryCallback<Dataset> qc = subscriptionCallbacks.get(conversationId);

                    if (null == qc) {
                        LOGGER.info("attempted to cancel Subscribe conversation "
                                + conversationId + ", which has already concluded or does not exist");
                    } else {
                        subscriptionCallbacks.remove(conversationId);

                        ACLMessage m = messageFactory.requestSubscriptionCancellation(self, server, conversationId);

                        setConversationId(m.getConversationId());

                        subscriptionCancellationCallbacks.put(m.getConversationId(), callback);

                        send(m);
                    }
                } catch (Throwable e) {
                    forgetConversation(conversationId);
                    callback.localFailure(new LocalFailure(e));
                }
            }
        };
    }

    public void setQueryServer(final QueryServer<Value, Dataset> queryServer) {
        this.queryServer = queryServer;
    }

    public void setPublisher(final Publisher<Value, Dataset> publisher) {
        this.publisher = publisher;
    }

    public void setup() {
        // Accept objects through the object-to-agent communication
        // channel, with a maximum size of 10 queued objects
        setEnabledO2ACommunication(true, 10);

        // Notify blocked threads that the agent is ready and that
        // object-to-agent communication is enabled
        Object[] args = getArguments();
        if (args.length == 2) {
            RDFAgentsPlatformImpl.CondVar latch = (RDFAgentsPlatformImpl.CondVar) args[0];
            latch.signal();

            Wrapper w = (Wrapper) args[1];
            w.setJadeAgent(this);
            messageFactory = w.messageFactory;
            self = w.self;
        } else {
            throw new IllegalStateException();
        }

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage m = myAgent.receive();
                if (null != m) {
                    System.out.println("I (" + self.getName() + ") have received this message: " + m);

                    try {
                        handleMessage(m);
                    } catch (Throwable t) {
                        forgetConversation(m.getConversationId());
                        LOGGER.severe("error while sending message: " + t + "\n" + RDFAgents.stackTraceToString(t));
                    }
                } else {
                    block();
                }
            }
        });

        // Add a suitable cyclic behaviour...
        addBehaviour(new CyclicBehaviour() {

            public void action() {
                // Retrieve the first object in the queue and print it on
                // the standard output
                Object obj = getO2AObject();
                if (obj != null) {
                    System.out.println("Got an object from the queue: [" + obj + "]");

                    if (obj instanceof Task) {
                        ((Task) obj).execute();
                    }
                } else
                    block();
            }
        });
    }

    private void forgetConversation(final String id) {
        if (null != id) {
            queryCallbacks.remove(id);
            queryCancellationCallbacks.remove(id);
            subscriptionCallbacks.remove(id);
            subscriptionCancellationCallbacks.remove(id);

            try {
                if (null != queryServer) {
                    queryServer.cancel(id);
                }
                if (null != publisher) {
                    publisher.cancel(id);
                }
            } catch (LocalFailure e) {
                LOGGER.severe("failed to cancel expired conversations (stack trace follows)\n" + RDFAgents.stackTraceToString(e));
            }
        }
    }

    public void takeDown() {
        // Disables the object-to-agent communication channel, thus
        // waking up all waiting threads
        setEnabledO2ACommunication(false, 0);
    }

    public static class Wrapper {
        private final AgentReference self;
        private final MessageFactory messageFactory;

        public Wrapper(final AgentReference self,
                       final MessageFactory messageFactory) {
            this.self = self;
            this.messageFactory = messageFactory;
        }

        private RDFJadeAgent jadeAgent;

        public RDFJadeAgent getJadeAgent() {
            return jadeAgent;
        }

        public void setJadeAgent(final RDFJadeAgent jadeAgent) {
            this.jadeAgent = jadeAgent;
        }
    }

    private void handleMessage(final ACLMessage m) {
        AgentReference sender = null;
        try {
            sender = messageFactory.fromAID(m.getSender());
        } catch (MessageNotUnderstoodException e) {
            // Since it may not be possible to contact the sender, just swallow the error.
            LOGGER.warning("message is invalid, cannot reply: " + e.getMessage());
            return;
        }

        try {
            if (null == m.getConversationId()) {
                throw new MessageNotUnderstoodException("missing conversation ID");
            }

            int performative = m.getPerformative();
            String protocol = m.getProtocol();

            if (null == protocol || 0 == protocol.length()) {
                throw new MessageNotUnderstoodException("missing protocol");
            }

            if (protocol.equals(FIPANames.InteractionProtocol.FIPA_QUERY)) {
                switch (performative) {
                    case ACLMessage.QUERY_REF:
                        handleQueryRequest(m, sender);
                        break;
                    case ACLMessage.INFORM_REF:
                        handleQueryResult(m);
                        break;
                    case ACLMessage.REFUSE:
                        handleQueryRefused(m);
                        break;
                    case ACLMessage.AGREE:
                        handleQueryAccepted(m);
                        break;
                    case ACLMessage.CANCEL:
                        handleCancelQuery(m, sender);
                        break;
                    case ACLMessage.CONFIRM:
                        handleQueryCancellationConfirmed(m);
                        break;
                    case ACLMessage.FAILURE:
                        handleFailure(m);
                        break;
                    default:
                        throw new MessageNotUnderstoodException("unexpected performative (code): " + performative);
                }
            } else if (protocol.equals(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE)) {
                switch (performative) {
                    case ACLMessage.SUBSCRIBE:
                        handleSubscriptionRequest(m, sender);
                        break;
                    case ACLMessage.INFORM_REF:
                        handleUpdate(m);
                        break;
                    case ACLMessage.REFUSE:
                        handleSubscriptionRefused(m);
                        break;
                    case ACLMessage.AGREE:
                        handleSubscriptionAccepted(m);
                        break;
                    case ACLMessage.CANCEL:
                        handleCancelSubscription(m, sender);
                        break;
                    case ACLMessage.CONFIRM:
                        handleSubscriptionCancellationConfirmed(m);
                        break;
                    case ACLMessage.FAILURE:
                        handleFailure(m);
                        break;
                    default:
                        throw new MessageNotUnderstoodException("unexpected performative (code): " + performative);
                }
            } else {
                throw new MessageNotUnderstoodException("unexpected protocol: " + protocol);
            }
        } catch (MessageNotUnderstoodException e) {
            ErrorExplanation exp = new ErrorExplanation(ErrorExplanation.Type.ExternalError, e.getMessage());
            send(messageFactory.notUnderstood(m, self, sender, exp));
            forgetConversation(m.getConversationId());
        } catch (MessageRejectedException e) {
            send(messageFactory.failure(self, sender, m, e.getExplanation()));
            forgetConversation(m.getConversationId());
        } catch (LocalFailure e) {
            ErrorExplanation exp = new ErrorExplanation(ErrorExplanation.Type.InternalError, e.getMessage());
            send(messageFactory.notUnderstood(m, self, sender, exp));
            forgetConversation(m.getConversationId());
        }
    }

    private void handleFailure(final ACLMessage m) {
        QueryCallback<Dataset> qc = queryCallbacks.get(m.getConversationId());
        CancellationCallback cc = queryCancellationCallbacks.get(m.getConversationId());

        try {
            try {
                ErrorExplanation exp = messageFactory.extractErrorExplanation(m);

                // TODO: if there are both query and cancellation callbacks, then this is a little strange

                if (null != qc) {
                    qc.remoteFailure(exp);
                }

                if (null != cc) {
                    cc.remoteFailure(exp);
                }
            } catch (MessageNotUnderstoodException e) {
                // No need to send a message back to the offending agent (and possibly get in a exception war)
                LOGGER.warning("failure message not understood: " + e.getMessage());
            } catch (LocalFailure e) {
                // TODO: again, pushing the error to both callbacks is strange (although sure to get the message across)

                if (null != qc) {
                    qc.localFailure(e);
                }

                if (null != cc) {
                    cc.localFailure(e);
                }
            }
        } finally {
            forgetConversation(m.getConversationId());
        }
    }

    private void handleQueryRequest(final ACLMessage m,
                                    final AgentReference sender) throws MessageNotUnderstoodException, LocalFailure, MessageRejectedException {
        assertRDFAgentsOntologyContent(m);

        if (null == queryServer) {
            throw new MessageRejectedException(new ErrorExplanation(
                    ErrorExplanation.Type.NotImplemented, "this agent does not implement the query server role"));
        }

        Value v = messageFactory.extractDescribeQuery(m);

        Commitment c = queryServer.considerQueryRequest(m.getConversationId(), v, sender);

        switch (c.getDecision()) {
            case AGREE_WITH_CONFIRMATION:
                send(messageFactory.agreeToAnswerQuery(self, sender, m));
                send(messageFactory.informOfQueryResult(self, sender, m, queryServer.answer(v), RDFContentLanguage.RDF_NQUADS));
                break;
            case AGREE_WITHOUT_CONFIRMATION:
                send(messageFactory.informOfQueryResult(self, sender, m, queryServer.answer(v), RDFContentLanguage.RDF_NQUADS));
                break;
            case REFUSE:
                send(messageFactory.refuseToAnswerQuery(self, sender, m, c.getExplanation()));
                break;
            default:
                throw new LocalFailure("unexpected decision: " + c.getDecision());
        }
    }

    private void handleQueryResult(final ACLMessage m) throws MessageRejectedException, MessageNotUnderstoodException, LocalFailure {
        QueryCallback<Dataset> callback = getQueryCallback(m);

        try {
            Dataset answer = messageFactory.extractDataset(m);
            callback.success(answer);
        } finally {
            forgetConversation(m.getConversationId());
        }
    }

    private void handleUpdate(final ACLMessage m) throws MessageRejectedException, MessageNotUnderstoodException, LocalFailure {
        QueryCallback<Dataset> callback = getSubscriptionCallback(m);

        Dataset answer = messageFactory.extractDataset(m);
        callback.success(answer);
    }

    private void handleQueryAccepted(final ACLMessage m) throws MessageRejectedException {
        getQueryCallback(m).agreed();
    }

    private void handleSubscriptionAccepted(final ACLMessage m) throws MessageRejectedException {
        getSubscriptionCallback(m).agreed();
    }

    private void handleQueryRefused(final ACLMessage m) throws MessageNotUnderstoodException, LocalFailure, MessageRejectedException {
        try {
            ErrorExplanation exp = messageFactory.extractErrorExplanation(m);
            getQueryCallback(m).refused(exp);
        } finally {
            forgetConversation(m.getConversationId());
        }
    }

    private void handleSubscriptionRefused(final ACLMessage m) throws MessageNotUnderstoodException, LocalFailure, MessageRejectedException {
        try {
            ErrorExplanation exp = messageFactory.extractErrorExplanation(m);
            getSubscriptionCallback(m).refused(exp);
        } finally {
            forgetConversation(m.getConversationId());
        }
    }

    private void handleCancelQuery(final ACLMessage m,
                                   final AgentReference sender) throws MessageNotUnderstoodException {
        try {
            ErrorExplanation exp = new ErrorExplanation(ErrorExplanation.Type.NotImplemented, "cancellation of queries is not yet supported");
            send(messageFactory.failToCancelQuery(self, sender, m, exp));
        } finally {
            forgetConversation(m.getConversationId());
        }
    }

    private void handleCancelSubscription(final ACLMessage m,
                                          final AgentReference sender) throws MessageNotUnderstoodException {
        try {
            ErrorExplanation exp = new ErrorExplanation(ErrorExplanation.Type.NotImplemented, "cancellation of subscriptions is not yet supported");
            send(messageFactory.failToCancelSubscription(self, sender, m, exp));
        } finally {
            forgetConversation(m.getConversationId());
        }
    }

    private void handleQueryCancellationConfirmed(final ACLMessage m) throws MessageRejectedException {
        try {
            getQueryCancellationCallback(m, true).success();
            removeQueryCancellationCallback(m);
        } finally {
            forgetConversation(m.getConversationId());
        }
    }

    private void handleSubscriptionCancellationConfirmed(final ACLMessage m) throws MessageRejectedException {
        try {
            getQueryCancellationCallback(m, false).success();
            removeQueryCancellationCallback(m);
        } finally {
            forgetConversation(m.getConversationId());
        }
    }

    private void handleSubscriptionRequest(final ACLMessage m,
                                           final AgentReference sender) throws MessageNotUnderstoodException, LocalFailure, MessageRejectedException {
        assertRDFAgentsOntologyContent(m);

        if (null == publisher) {
            throw new MessageRejectedException(new ErrorExplanation(
                    ErrorExplanation.Type.NotImplemented, "this agent does not implement the publisher role"));
        }

        Value v = messageFactory.extractDescribeQuery(m);

        UpdateHandler<Dataset> handler = new UpdateHandler<Dataset>() {
            @Override
            public void handle(final Dataset result) throws LocalFailure {

                try {
                    send(messageFactory.informOfSubscriptionUpdate(self, sender, m, result, RDFContentLanguage.RDF_NQUADS));
                } catch (MessageRejectedException e) {
                    LOGGER.severe("message rejected after update already produced (this shouldn't happen)");
                    forgetConversation(m.getConversationId());
                    publisher.cancel(m.getConversationId());
                } catch (MessageNotUnderstoodException e) {
                    LOGGER.severe("message not understood after update already produced (this shouldn't happen)");
                    forgetConversation(m.getConversationId());
                    publisher.cancel(m.getConversationId());
                }
            }
        };
        Commitment c = publisher.considerSubscriptionRequest(m.getConversationId(), v, sender, handler);

        switch (c.getDecision()) {
            case AGREE_WITH_CONFIRMATION:
                send(messageFactory.agreeToSubcriptionRequest(self, sender, m));

                //send(messageFactory.informOfQueryResult(self, sender, m, queryServer.answer(v), RDFContentLanguage.RDF_NQUADS));

                break;
            case REFUSE:
                send(messageFactory.refuseSubscriptionRequest(self, sender, m, c.getExplanation()));
                break;
            default:
                throw new LocalFailure("unexpected decision: " + c.getDecision());
        }
    }

    private QueryCallback<Dataset> getQueryCallback(ACLMessage m) throws MessageRejectedException {
        QueryCallback<Dataset> callback = queryCallbacks.get(m.getConversationId());

        if (null == callback) {
            throw new MessageRejectedException(
                    new ErrorExplanation(
                            ErrorExplanation.Type.InteractionExplired,
                            "no such conversation: " + m.getConversationId()));
        }

        return callback;
    }

    private QueryCallback<Dataset> getSubscriptionCallback(ACLMessage m) throws MessageRejectedException {
        QueryCallback<Dataset> callback = subscriptionCallbacks.get(m.getConversationId());

        if (null == callback) {
            throw new MessageRejectedException(
                    new ErrorExplanation(
                            ErrorExplanation.Type.InteractionExplired,
                            "no such conversation: " + m.getConversationId()));
        }

        return callback;
    }

    private void removeQueryCallback(final ACLMessage m) {
        queryCallbacks.remove(m.getConversationId());
    }

    private CancellationCallback getQueryCancellationCallback(final ACLMessage m,
                                                              final boolean queryVsSubscribe) throws MessageRejectedException {
        CancellationCallback callback = queryVsSubscribe
                ? queryCancellationCallbacks.get(m.getConversationId())
                : subscriptionCancellationCallbacks.get(m.getConversationId());

        if (null == callback) {
            throw new MessageRejectedException(
                    new ErrorExplanation(
                            ErrorExplanation.Type.InteractionExplired,
                            "no such conversation: " + m.getConversationId()));
        }

        return callback;
    }

    private void removeQueryCancellationCallback(final ACLMessage m) {
        queryCancellationCallbacks.remove(m.getConversationId());
    }

    private void assertRDFAgentsOntologyContent(final ACLMessage m) throws MessageNotUnderstoodException {
        String ontology = m.getOntology();
        if (null == ontology) {
            throw new MessageNotUnderstoodException("missing ontology parameter");
        }
    }
}
