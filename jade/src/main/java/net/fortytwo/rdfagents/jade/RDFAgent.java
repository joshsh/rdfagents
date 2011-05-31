package net.fortytwo.rdfagents.jade;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import net.fortytwo.rdfagents.messaging.FailureException;
import net.fortytwo.rdfagents.messaging.query.QueryClient;
import net.fortytwo.rdfagents.model.AgentReference;
import org.openrdf.model.Value;

/**
 * User: josh
 * Date: 5/27/11
 * Time: 10:18 PM
 */
public class RDFAgent extends Agent {

    private AgentReference self;
    private MessageFactory messageFactory;

    public interface Task {
        void execute();
    }

    public Task submitQuery(final Value resource,
                            final AgentReference server,
                            final QueryClient.QueryCallback callback) {
        return new Task() {
            public void execute() {
                ACLMessage m = null;

                try {
                    m = messageFactory.poseQuery(self, server, resource);
                } catch (FailureException e) {
                    callback.localFailure(e);
                }

                System.out.println("issuing a query for " + resource);
                System.out.println("--> " + m);
            }
        };
    }

/*
    public ACLMessage cancelQuery(final ACLMessage request,
                                  final AgentReference server,
                                  final QueryClient.CancellationCallback callback) {
        addTask(new Task() {
            public void execute() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }

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

    public void setup() {
        // Accept objects through the object-to-agent communication
        // channel, with a maximum size of 10 queued objects
        setEnabledO2ACommunication(true, 10);

        // Notify blocked threads that the agent is ready and that
        // object-to-agent communication is enabled
        Object[] args = getArguments();
        if (args.length == 2) {
            RDFAgentsPlatform.CondVar latch = (RDFAgentsPlatform.CondVar) args[0];
            latch.signal();

            Wrapper w = (Wrapper) args[1];
            w.setAgent(this);
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
                    System.out.println("got this message: " + m);
                    /*
                    ACLMessage r = m.createReply();
                    r.setLanguage(m.getLanguage());
                    r.setContent(m.getContent());
                    send(r); */
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

        private RDFAgent agent;

        public RDFAgent getAgent() {
            return agent;
        }

        public void setAgent(RDFAgent agent) {
            this.agent = agent;
        }
    }
}
