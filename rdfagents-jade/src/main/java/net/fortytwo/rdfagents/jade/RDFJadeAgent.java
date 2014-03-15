package net.fortytwo.rdfagents.jade;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import net.fortytwo.rdfagents.RDFAgents;
import net.fortytwo.rdfagents.messaging.CancellationCallback;
import net.fortytwo.rdfagents.messaging.Commitment;
import net.fortytwo.rdfagents.messaging.ConsumerCallback;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.MessageNotUnderstoodException;
import net.fortytwo.rdfagents.messaging.MessageRejectedException;
import net.fortytwo.rdfagents.messaging.query.QueryProvider;
import net.fortytwo.rdfagents.messaging.subscribe.PubsubProvider;
import net.fortytwo.rdfagents.messaging.subscribe.UpdateHandler;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.ErrorExplanation;
import net.fortytwo.rdfagents.model.RDFContentLanguage;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RDFJadeAgent extends Agent {
    private static final Logger LOGGER = Logger.getLogger(RDFJadeAgent.class.getName());

    private final Map<String, ConsumerCallback<Dataset>> queryCallbacks
            = new HashMap<String, ConsumerCallback<Dataset>>();
    private final Map<String, CancellationCallback> queryCancellationCallbacks
            = new HashMap<String, CancellationCallback>();
    private final Map<String, ConsumerCallback<Dataset>> subscriptionCallbacks
            = new HashMap<String, ConsumerCallback<Dataset>>();
    private final Map<String, CancellationCallback> subscriptionCancellationCallbacks
            = new HashMap<String, CancellationCallback>();

    private AgentId self;
    private MessageFactory messageFactory;
    private QueryProvider<Value, Dataset> queryProvider;
    private PubsubProvider<Value, Dataset> pubsubProvider;

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
                            final AgentId server,
                            final ConsumerCallback<Dataset> callback) {
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

                    sendMessage(m);
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
                            final AgentId server,
                            final CancellationCallback callback) {
        return new Task() {
            public void execute() {
                try {
                    ConsumerCallback<Dataset> qc = queryCallbacks.get(conversationId);

                    if (null == qc) {
                        LOGGER.info("attempted to cancel Query conversation "
                                + conversationId + ", which has already concluded or does not exist");
                    } else {
                        queryCallbacks.remove(conversationId);

                        ACLMessage m = messageFactory.requestQueryCancellation(self, server, conversationId);

                        setConversationId(m.getConversationId());

                        queryCancellationCallbacks.put(m.getConversationId(), callback);

                        sendMessage(m);
                    }
                } catch (Throwable e) {
                    forgetConversation(conversationId);
                    callback.localFailure(new LocalFailure(e));
                }
            }
        };
    }

    public Task subscribe(final Value topic,
                          final AgentId publisher,
                          final ConsumerCallback<Dataset> callback) {
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

                    sendMessage(m);
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
                                   final AgentId server,
                                   final CancellationCallback callback) {
        return new Task() {
            public void execute() {
                try {
                    ConsumerCallback<Dataset> qc = subscriptionCallbacks.get(conversationId);

                    if (null == qc) {
                        LOGGER.info("attempted to cancel Subscribe conversation "
                                + conversationId + ", which has already concluded or does not exist");
                    } else {
                        subscriptionCallbacks.remove(conversationId);

                        ACLMessage m = messageFactory.requestSubscriptionCancellation(self, server, conversationId);

                        setConversationId(m.getConversationId());

                        subscriptionCancellationCallbacks.put(m.getConversationId(), callback);

                        sendMessage(m);
                    }
                } catch (Throwable e) {
                    forgetConversation(conversationId);
                    callback.localFailure(new LocalFailure(e));
                }
            }
        };
    }

    public void setQueryProvider(final QueryProvider<Value, Dataset> queryProvider) {
        this.queryProvider = queryProvider;
    }

    public void setPubsubProvider(final PubsubProvider<Value, Dataset> pubsubProvider) {
        this.pubsubProvider = pubsubProvider;
    }

    public void setup() {
        // Accept objects through the object-to-agent communication
        // channel, with a maximum size of 10 queued objects
        setEnabledO2ACommunication(true, 10);

        // Notify blocked threads that the agent is ready and that
        // object-to-agent communication is enabled
        Object[] args = getArguments();
        if (args.length == 2) {
            Wrapper w = (Wrapper) args[1];
            w.setJadeAgent(this);
            messageFactory = w.messageFactory;
            self = w.self;

            // issue signal after setting self in wrapper (above)
            RDFAgentsPlatformImpl.CondVar latch = (RDFAgentsPlatformImpl.CondVar) args[0];
            latch.signal();
        } else {
            throw new IllegalStateException();
        }

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage m = myAgent.receive();
                if (null != m) {
                    receiveMessage(m);
                    //System.out.println("I (" + self.getName() + ") have received this message: " + m);

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

        StringBuilder sb = new StringBuilder("initialized agent <").append(self.getUri()).append("> with address(es) ");
        boolean first = true;
        for (URI s : self.getTransportAddresses()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }

            sb.append("<").append(s).append(">");
        }
        System.out.println(sb.toString());

        /*
        ACLMessage m = new ACLMessage(ACLMessage.INFORM);
        AID receiver = new AID();
        receiver.setName("urn:agent:twitlogic@twitlogic.fortytwo.net");
        // Note: no /acc here (the message doesn't even reach the server that way)
        receiver.addAddresses("xmpp://patabot.2@jabber.org");
        m.setSender(messageFactory.toAID(self));
        m.addReceiver(receiver);
        m.setContent("test message #1");
        sendMessage(m);

        m = new ACLMessage(ACLMessage.INFORM);
        receiver = new AID();
        receiver.setName("urn:agent:agent1@rdfagents.fortytwo.net");
        // Note: no /acc here (the message doesn't even reach the server that way)
        receiver.addAddresses("xmpp://patabot.1@jabber.org");
        m.setSender(messageFactory.toAID(self));
        m.addReceiver(receiver);
        m.setContent("test message #2");
        sendMessage(m);
        //*/
    }

    private void forgetConversation(final String id) {
        if (null != id) {
            queryCallbacks.remove(id);
            queryCancellationCallbacks.remove(id);
            subscriptionCallbacks.remove(id);
            subscriptionCancellationCallbacks.remove(id);

            try {
                if (null != queryProvider) {
                    queryProvider.cancel(id);
                }
                if (null != pubsubProvider) {
                    pubsubProvider.cancel(id);
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
        private final AgentId self;
        private final MessageFactory messageFactory;

        public Wrapper(final AgentId self,
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
        // Failure and not-understood messages are exempt from validity checks (so as to avoid feedback loops).
        if (ACLMessage.FAILURE == m.getPerformative()) {
            handleFailure(m);
        } else if (ACLMessage.NOT_UNDERSTOOD == m.getPerformative()) {
            handleNotUnderstood(m);
        } else {

            AgentId sender;
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
                        default:
                            throw new MessageNotUnderstoodException("unexpected performative (code): " + performative);
                    }
                } else {
                    throw new MessageNotUnderstoodException("unexpected protocol: " + protocol);
                }
            } catch (MessageNotUnderstoodException e) {
                ErrorExplanation exp = new ErrorExplanation(ErrorExplanation.Type.ExternalError, e.getMessage());
                sendMessage(messageFactory.notUnderstood(m, self, sender, exp));
                forgetConversation(m.getConversationId());
            } catch (MessageRejectedException e) {
                sendMessage(messageFactory.failure(self, sender, m, e.getExplanation()));
                forgetConversation(m.getConversationId());
            } catch (LocalFailure e) {
                ErrorExplanation exp = new ErrorExplanation(ErrorExplanation.Type.InternalError, e.getMessage());
                sendMessage(messageFactory.notUnderstood(m, self, sender, exp));
                forgetConversation(m.getConversationId());
            }
        }
    }

    private void handleFailure(final ACLMessage m) {
        ConsumerCallback<Dataset> qc = queryCallbacks.get(m.getConversationId());
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

    private void handleNotUnderstood(final ACLMessage m) {
        try {
            LOGGER.warning("received a not-understood message: " + m);
        } finally {
            forgetConversation(m.getConversationId());
        }
    }

    private void handleQueryRequest(final ACLMessage m,
                                    final AgentId sender) throws MessageNotUnderstoodException, LocalFailure, MessageRejectedException {
        assertRDFAgentsOntologyContent(m);

        if (null == queryProvider) {
            throw new MessageRejectedException(new ErrorExplanation(
                    ErrorExplanation.Type.NotImplemented, "this agent does not implement the query server role"));
        }

        Value v = messageFactory.extractDescribeQuery(m);

        Commitment c = queryProvider.considerQueryRequest(m.getConversationId(), v, sender);

        switch (c.getDecision()) {
            case AGREE_AND_NOTIFY:
                sendMessage(messageFactory.agreeToAnswerQuery(self, sender, m));
                sendMessage(messageFactory.informOfQueryResult(self, sender, m, queryProvider.answer(v), RDFContentLanguage.RDF_NQUADS));
                break;
            case AGREE_SILENTLY:
                sendMessage(messageFactory.informOfQueryResult(self, sender, m, queryProvider.answer(v), RDFContentLanguage.RDF_NQUADS));
                break;
            case REFUSE:
                sendMessage(messageFactory.refuseToAnswerQuery(self, sender, m, c.getExplanation()));
                break;
            default:
                throw new LocalFailure("unexpected decision: " + c.getDecision());
        }
    }

    private void handleQueryResult(final ACLMessage m) throws MessageRejectedException, MessageNotUnderstoodException, LocalFailure {
        ConsumerCallback<Dataset> callback = getQueryCallback(m);

        try {
            Dataset answer = messageFactory.extractDataset(m);
            callback.success(answer);
        } finally {
            forgetConversation(m.getConversationId());
        }
    }

    private void handleUpdate(final ACLMessage m) throws MessageRejectedException, MessageNotUnderstoodException, LocalFailure {
        ConsumerCallback<Dataset> callback = getSubscriptionCallback(m);

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
                                   final AgentId sender) throws MessageNotUnderstoodException {
        try {
            ErrorExplanation exp = new ErrorExplanation(ErrorExplanation.Type.NotImplemented, "cancellation of queries is not yet supported");
            sendMessage(messageFactory.failToCancelQuery(self, sender, m, exp));
        } finally {
            forgetConversation(m.getConversationId());
        }
    }

    private void handleCancelSubscription(final ACLMessage m,
                                          final AgentId sender) throws MessageNotUnderstoodException {
        try {
            ErrorExplanation exp = new ErrorExplanation(ErrorExplanation.Type.NotImplemented, "cancellation of subscriptions is not yet supported");
            sendMessage(messageFactory.failToCancelSubscription(self, sender, m, exp));
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
                                           final AgentId sender) throws MessageNotUnderstoodException, LocalFailure, MessageRejectedException {
        assertRDFAgentsOntologyContent(m);

        if (null == pubsubProvider) {
            throw new MessageRejectedException(new ErrorExplanation(
                    ErrorExplanation.Type.NotImplemented, "this agent does not implement the publisher role"));
        }

        Value v = messageFactory.extractDescribeQuery(m);

        UpdateHandler<Dataset> handler = new UpdateHandler<Dataset>() {
            @Override
            public void handle(final Dataset result) throws LocalFailure {

                try {
                    sendMessage(messageFactory.informOfSubscriptionUpdate(self, sender, m, result, RDFContentLanguage.RDF_NQUADS));
                } catch (MessageRejectedException e) {
                    LOGGER.severe("message rejected after update already produced (this shouldn't happen)");
                    forgetConversation(m.getConversationId());
                    pubsubProvider.cancel(m.getConversationId());
                } catch (MessageNotUnderstoodException e) {
                    LOGGER.severe("message not understood after update already produced (this shouldn't happen)");
                    forgetConversation(m.getConversationId());
                    pubsubProvider.cancel(m.getConversationId());
                }
            }
        };
        Commitment c = pubsubProvider.considerSubscriptionRequest(m.getConversationId(), v, sender, handler);

        switch (c.getDecision()) {
            case AGREE_AND_NOTIFY:
                sendMessage(messageFactory.agreeToSubcriptionRequest(self, sender, m));

                //sendMessage(messageFactory.informOfQueryResult(self, sender, m, queryServer.answer(v), RDFContentLanguage.RDF_NQUADS));

                break;
            case REFUSE:
                sendMessage(messageFactory.refuseSubscriptionRequest(self, sender, m, c.getExplanation()));
                break;
            default:
                throw new LocalFailure("unexpected decision: " + c.getDecision());
        }
    }

    private ConsumerCallback<Dataset> getQueryCallback(ACLMessage m) throws MessageRejectedException {
        ConsumerCallback<Dataset> callback = queryCallbacks.get(m.getConversationId());

        if (null == callback) {
            throw new MessageRejectedException(
                    new ErrorExplanation(
                            ErrorExplanation.Type.InteractionExplired,
                            "no such conversation: " + m.getConversationId()));
        }

        return callback;
    }

    private ConsumerCallback<Dataset> getSubscriptionCallback(ACLMessage m) throws MessageRejectedException {
        ConsumerCallback<Dataset> callback = subscriptionCallbacks.get(m.getConversationId());

        if (null == callback) {
            throw new MessageRejectedException(
                    new ErrorExplanation(
                            ErrorExplanation.Type.InteractionExplired,
                            "no such conversation: " + m.getConversationId()));
        }

        return callback;
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

    private void receiveMessage(final ACLMessage m) {
        System.out.println("### Message received ###########################################################\n"
                + m + "\n################################################################################");
        /*if (m.getPerformative() != ACLMessage.INFORM) {
            ACLMessage reply = m.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent("just a reply");
            sendMessage(reply);
        }*/
    }

    private void sendMessage(final ACLMessage m) {
        send(m);
        System.out.println("### Message sent ###############################################################\n"
                + m + "\n################################################################################");

        /*
        if (m.getPerformative() != ACLMessage.INFORM) {
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            m.setSender(messageFactory.toAID(self));
            m.addReceiver(m.get);
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent("this, too");
            sendMessage(reply);
        } //*/
    }
}
