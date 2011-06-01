package net.fortytwo.rdfagents.jade;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import net.fortytwo.rdfagents.RDFAgents;
import net.fortytwo.rdfagents.data.RDFContentLanguage;
import net.fortytwo.rdfagents.messaging.FailureException;
import net.fortytwo.rdfagents.messaging.query.QueryClient;
import net.fortytwo.rdfagents.messaging.query.QueryServer;
import net.fortytwo.rdfagents.model.AgentReference;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.ErrorExplanation;
import org.openrdf.model.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: 5/27/11
 * Time: 10:18 PM
 */
public class RDFAgentJade extends Agent {
    private static final Logger LOGGER = Logger.getLogger(RDFAgentJade.class.getName());

    private final Map<String, QueryClient.QueryCallback<Dataset>> queryCallbacks
            = new HashMap<String, QueryClient.QueryCallback<Dataset>>();
    private final Map<String, QueryClient.CancellationCallback> cancellationCallbacks
            = new HashMap<String, QueryClient.CancellationCallback>();

    private AgentReference self;
    private MessageFactory messageFactory;
    private QueryServer<Value, Dataset> queryServer;

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
                            final QueryClient.QueryCallback<Dataset> callback) {
        return new Task() {
            public void execute() {
                ACLMessage m;

                try {
                    m = messageFactory.poseQuery(self, server, resource);
                } catch (FailureException e) {
                    callback.localFailure(e);
                    return;
                }

                //this.conversationId = m.getConversationId();
                setConversationId(m.getConversationId());

                System.out.println("issuing a query for " + resource);
                System.out.println("--> " + m);

                queryCallbacks.put(m.getConversationId(), callback);

                send(m);
            }
        };
    }

    public Task cancelQuery(final String conversationId,
                            final AgentReference server,
                            final QueryClient.CancellationCallback callback) {
        return new Task() {
            public void execute() {
                QueryClient.QueryCallback<Dataset> qc = queryCallbacks.get(conversationId);

                if (null == qc) {
                    LOGGER.info("attempted to cancel query conversation "
                            + conversationId + ", which has already concluded or does not exist");
                } else {
                    queryCallbacks.remove(conversationId);

                    ACLMessage m;

                    try {
                        m = messageFactory.requestQueryCancellation(self, server, conversationId);
                    } catch (FailureException e) {
                        callback.localFailure(e);
                        return;
                    }

                    setConversationId(m.getConversationId());

                    cancellationCallbacks.put(m.getConversationId(), callback);

                    send(m);
                }
            }
        };
    }

    /*
    public ACLMessage submitSubscriptionRequest(final Value resource,
                                                final AgentReference server,
                                                final QueryClient.QueryCallback callback) {
        addTask(new Task() {
            public void execute() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }

    public ACLMessage cancelSubscriptionRequest(final ACLMessage request,
                                                final AgentReference server,
                                                final QueryClient.CancellationCallback callback) {
        addTask(new Task() {
            public void execute() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }
    */

    public void setQueryServer(final QueryServer<Value, Dataset> queryServer) {
        this.queryServer = queryServer;
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
            w.setAgentJade(this);
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
                        forgetConversation(m);
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

    private void forgetConversation(final ACLMessage m) {
        String id = m.getConversationId();
        if (null != id) {
            queryCallbacks.remove(id);
            cancellationCallbacks.remove(id);
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

        private RDFAgentJade agentJade;

        public RDFAgentJade getAgentJade() {
            return agentJade;
        }

        public void setAgentJade(final RDFAgentJade agentJade) {
            this.agentJade = agentJade;
        }
    }

    private void handleMessage(final ACLMessage m) throws NotUnderstoodException {
        AgentReference sender = messageFactory.fromAID(m.getSender());

        try {
            if (null == m.getConversationId()) {
                throw new NotUnderstoodException("missing conversation ID");
            }

            int performative = m.getPerformative();
            String protocol = m.getProtocol();

            if (null == protocol || 0 == protocol.length()) {
                throw new NotUnderstoodException("missing protocol");
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
                        throw new NotUnderstoodException("unexpected performative (code): " + performative);
                }
            } else if (protocol.equals(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE)) {
                // TODO
                throw new IllegalStateException("not yet implemented...");
            } else {
                throw new NotUnderstoodException("unexpected protocol: " + protocol);
            }
        } catch (NotUnderstoodException e) {
            ErrorExplanation exp = new ErrorExplanation(ErrorExplanation.Type.ExternalError, e.getMessage());
            send(messageFactory.notUnderstood(m, self, sender, exp));
            forgetConversation(m);
        }
    }

    private void handleFailure(final ACLMessage m) throws NotUnderstoodException {
        try {
            ErrorExplanation exp = null;
            try {
                exp = messageFactory.extractErrorExplanation(m);
            } catch (FailureException e) {
                LOGGER.severe("exception while extracting error explanation: " + e + "\n" + RDFAgents.stackTraceToString(e));
                return;
            }

            QueryClient.QueryCallback<Dataset> qc = queryCallbacks.get(m.getConversationId());
            QueryClient.CancellationCallback cc = cancellationCallbacks.get(m.getConversationId());

            // TODO: if there are both query and cancellation callbacks, then this is a little strange

            if (null != qc) {
                qc.remoteFailure(exp);
            }

            if (null != cc) {
                cc.remoteFailure(exp);
            }

        } finally {
            forgetConversation(m);
        }
    }

    private void handleQueryRequest(final ACLMessage m,
                                    final AgentReference sender) throws NotUnderstoodException {
        try {
            assertRDFAgentsOntologyContent(m);
            Value v = messageFactory.extractDescribeQuery(m);

            QueryServer.Commitment c = queryServer.considerQueryRequest(v, sender);

            switch (c.decision) {
                case ANSWER_WITH_CONFIRMATION:
                    send(messageFactory.agreeToAnswerQuery(self, sender, m));
                    send(messageFactory.informOfQueryResult(self, sender, m, queryServer.answer(v), RDFContentLanguage.RDF_NQUADS));
                    break;
                case ANSWER_WITHOUT_CONFIRMATION:
                    send(messageFactory.informOfQueryResult(self, sender, m, queryServer.answer(v), RDFContentLanguage.RDF_NQUADS));
                    break;
                case REFUSE:
                    send(messageFactory.refuseToAnswerQuery(self, sender, m, c.explanation));
                    break;
            }
        } catch (FailureException e) {
            send(messageFactory.failToInformOfQueryResult(self, sender, m, e.getExplanation()));
            forgetConversation(m);
        }
    }

    private void handleQueryResult(final ACLMessage m) throws NotUnderstoodException {
        try {
            Dataset answer = messageFactory.extractDataset(m);
            getQueryCallback(m).success(answer);
            removeQueryCallback(m);
        } catch (FailureException e) {
            try {
                getQueryCallback(m).localFailure(e);
                forgetConversation(m);
            } catch (FailureException e1) {
                LOGGER.severe("error while generating failure message: " + e1);
            }
        }
    }

    private void handleQueryAccepted(final ACLMessage m) {
        try {
            getQueryCallback(m).agreed();
        } catch (FailureException e) {
            try {
                getQueryCallback(m).localFailure(e);
                forgetConversation(m);
            } catch (FailureException e1) {
                LOGGER.severe("error while generating failure message: " + e1);
            }
        }
    }

    private void handleQueryRefused(final ACLMessage m) throws NotUnderstoodException {
        try {
            ErrorExplanation exp = messageFactory.extractErrorExplanation(m);
            getQueryCallback(m).refused(exp);
            removeQueryCallback(m);
        } catch (FailureException e) {
            try {
                getQueryCallback(m).localFailure(e);
                forgetConversation(m);
            } catch (FailureException e1) {
                LOGGER.severe("error while generating failure message: " + e1);
            }
        }
    }

    private void handleCancelQuery(final ACLMessage m,
                                   final AgentReference sender) throws NotUnderstoodException {
        ErrorExplanation exp = new ErrorExplanation(ErrorExplanation.Type.NotImplemented, "cancellation of queries is not yet supported");
        send(messageFactory.failToCancelQuery(self, sender, m, exp));
    }

    private void handleQueryCancellationConfirmed(final ACLMessage m) {
        try {
            getQueryCancellationCallback(m).success();
            removeQueryCancellationCallback(m);
        } catch (FailureException e) {
            try {
                getQueryCallback(m).localFailure(e);
                forgetConversation(m);
            } catch (FailureException e1) {
                LOGGER.severe("error while generating failure message: " + e1);
            }
        }
    }

    private QueryClient.QueryCallback<Dataset> getQueryCallback(ACLMessage m) throws FailureException {
        QueryClient.QueryCallback<Dataset> callback = queryCallbacks.get(m.getConversationId());

        if (null == callback) {
            throw new FailureException(
                    new ErrorExplanation(
                            ErrorExplanation.Type.InteractionExplired,
                            "no such conversation: " + m.getConversationId()));
        }

        return callback;
    }

    private void removeQueryCallback(final ACLMessage m) {
        queryCallbacks.remove(m.getConversationId());
    }

    private QueryClient.CancellationCallback getQueryCancellationCallback(ACLMessage m) throws FailureException {
        QueryClient.CancellationCallback callback = cancellationCallbacks.get(m.getConversationId());

        if (null == callback) {
            throw new FailureException(
                    new ErrorExplanation(
                            ErrorExplanation.Type.InteractionExplired,
                            "no such conversation: " + m.getConversationId()));
        }

        return callback;
    }

    private void removeQueryCancellationCallback(final ACLMessage m) {
        cancellationCallbacks.remove(m.getConversationId());
    }

    private void assertRDFAgentsOntologyContent(final ACLMessage m) throws NotUnderstoodException {
        String ontology = m.getOntology();
        if (null == ontology) {
            throw new NotUnderstoodException("missing ontology parameter");
        }
    }
}
