package net.fortytwo.rdfagents.jade;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.ErrorExplanation;
import net.fortytwo.rdfagents.RDFAgents;
import net.fortytwo.rdfagents.RDFAgentsTestCase;
import net.fortytwo.rdfagents.model.RDFContentLanguage;
import net.fortytwo.rdfagents.data.RecursiveDescribeQuery;
import net.fortytwo.rdfagents.data.DatasetQuery;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class MessageFactoryTest extends RDFAgentsTestCase {

    public void testPoseQuery() throws Exception {
        ACLMessage query;
        Value subject;

        query = messageFactory.poseQuery(sender, receiver, resourceX);
        assertIsQuery(query);
        assertEquals("((any ?dataset (describes ?dataset (resource :uri http://example.org/resourceX))))",
                query.getContent());
        subject = messageFactory.extractDescribeQuery(query);
        assertTrue(subject instanceof URI);
        assertEquals("http://example.org/resourceX", subject.stringValue());
        assertNull(query.getUserDefinedParameter(RDFAgents.RDFAGENTS_ACCEPT_PARAMETER));

        query = messageFactory.poseQuery(sender, receiver, plainLiteralX);
        assertIsQuery(query);
        assertEquals("((any ?dataset (describes ?dataset (literal :label \"Don't panic.\"))))",
                query.getContent());
        subject = messageFactory.extractDescribeQuery(query);
        assertTrue(subject instanceof Literal);
        assertEquals("Don't panic.", ((Literal) subject).getLabel());
        assertNull(((Literal) subject).getLanguage());
        assertNull(((Literal) subject).getDatatype());
        assertNull(query.getUserDefinedParameter(RDFAgents.RDFAGENTS_ACCEPT_PARAMETER));

        query = messageFactory.poseQuery(sender, receiver, typedLiteralX);
        assertIsQuery(query);
        assertEquals("((any ?dataset (describes ?dataset (literal :label \"Don't panic.\" :datatype " +
                "(resource :uri http://www.w3.org/2001/XMLSchema#string)))))", query.getContent());
        subject = messageFactory.extractDescribeQuery(query);
        assertTrue(subject instanceof Literal);
        assertEquals("Don't panic.", ((Literal) subject).getLabel());
        assertNull(((Literal) subject).getLanguage());
        assertEquals(XMLSchema.STRING, ((Literal) subject).getDatatype());
        assertNull(query.getUserDefinedParameter(RDFAgents.RDFAGENTS_ACCEPT_PARAMETER));

        query = messageFactory.poseQuery(sender, receiver, languageLiteralX);
        assertIsQuery(query);
        assertEquals("((any ?dataset (describes ?dataset (literal :label \"Don't panic.\" :language en))))",
                query.getContent());
        subject = messageFactory.extractDescribeQuery(query);
        assertTrue(subject instanceof Literal);
        assertEquals("Don't panic.", ((Literal) subject).getLabel());
        assertEquals("en", ((Literal) subject).getLanguage());
        assertNull(((Literal) subject).getDatatype());
        assertNull(query.getUserDefinedParameter(RDFAgents.RDFAGENTS_ACCEPT_PARAMETER));

        query = messageFactory.poseQuery(sender, receiver, resourceX, RDFContentLanguage.RDF_NQUADS);
        assertEquals("rdf-nquads", query.getUserDefinedParameter(RDFAgents.RDFAGENTS_ACCEPT_PARAMETER));

        query = messageFactory.poseQuery(
                sender, receiver, resourceX, RDFContentLanguage.RDF_NQUADS, RDFContentLanguage.RDF_TRIG);
        assertEquals("rdf-nquads; rdf-trig", query.getUserDefinedParameter(RDFAgents.RDFAGENTS_ACCEPT_PARAMETER));
    }

    public void testRefuseToAnswerQuery() throws Exception {
        ACLMessage query = messageFactory.poseQuery(sender, receiver, resourceX);
        ErrorExplanation ex = new ErrorExplanation(ErrorExplanation.Type.Unavailable, "I prefer not to.");
        ACLMessage r = messageFactory.refuseToAnswerQuery(receiver, sender, query, ex);
        //System.out.println(r);

        assertIsReply(r, query);
        assertEquals(ACLMessage.REFUSE, r.getPerformative());
        assertHasExplanation(r, "((unavailable \"I prefer not to.\"))");
        ErrorExplanation exp = messageFactory.extractErrorExplanation(r);
        assertEquals(ErrorExplanation.Type.Unavailable, exp.getType());
        assertEquals("I prefer not to.", exp.getMessage());
    }

    public void testAgreeToAnswerQuery() throws Exception {
        ACLMessage query = messageFactory.poseQuery(sender, receiver, resourceX);
        ACLMessage r = messageFactory.agreeToAnswerQuery(receiver, sender, query);
        System.out.println(r);

        assertIsReply(r, query);
        assertEquals(ACLMessage.AGREE, r.getPerformative());
        assertNull(r.getContent());
    }

    public void testInformOfQueryResult() throws Exception {
        ACLMessage query, result;
        DatasetQuery q = new RecursiveDescribeQuery(ARTHUR, sail);

        query = messageFactory.poseQuery(sender, receiver, ARTHUR);
        result = messageFactory.informOfQueryResult(
                receiver, sender, query, q.evaluate(), RDFContentLanguage.RDF_TRIG);
        //System.out.println(result);
        assertIsReply(result, query);
        assertEquals(ACLMessage.INFORM_REF, result.getPerformative());
        assertEquals("fipa-query", result.getProtocol());
        assertHasRDFContent(result);
        // TODO: validate content
        assertEquals("rdf-trig", result.getLanguage());

        query = messageFactory.poseQuery(sender, receiver, ARTHUR, RDFContentLanguage.RDF_NQUADS);
        result = messageFactory.informOfQueryResult(
                receiver, sender, query, q.evaluate(), RDFContentLanguage.RDF_TRIG);
        //System.out.println(result);
        assertIsReply(result, query);
        assertEquals(ACLMessage.INFORM_REF, result.getPerformative());
        assertEquals("fipa-query", result.getProtocol());
        assertHasRDFContent(result);
        // TODO: validate content
        assertEquals("rdf-nquads", result.getLanguage());
    }

    public void testFailToInformOfQueryResult() throws Exception {
        ACLMessage query = messageFactory.poseQuery(sender, receiver, ARTHUR, RDFContentLanguage.RDF_NQUADS);
        ErrorExplanation ex = new ErrorExplanation(
                ErrorExplanation.Type.NotImplemented, "N-Quads format is not supported.");
        ACLMessage fail = messageFactory.failToInformOfQueryResult(receiver, sender, query, ex);

        assertIsReply(fail, query);
        assertEquals(ACLMessage.FAILURE, fail.getPerformative());
        assertEquals("fipa-query", fail.getProtocol());
        assertHasExplanation(fail, "((not-implemented \"N-Quads format is not supported.\"))");
        ErrorExplanation exp = messageFactory.extractErrorExplanation(fail);
        assertEquals(ErrorExplanation.Type.NotImplemented, exp.getType());
        assertEquals("N-Quads format is not supported.", exp.getMessage());
    }

    public void testRequestQueryCancellation() throws Exception {
        ACLMessage query = messageFactory.poseQuery(sender, receiver, resourceX);
        ACLMessage c = messageFactory.requestQueryCancellation(sender, receiver, query.getConversationId());

        assertIsCancellationRequest(c, query);
        assertEquals("fipa-query", c.getProtocol());
    }

    public void testConfirmQueryCancellation() throws Exception {
        ACLMessage query = messageFactory.poseQuery(sender, receiver, resourceX);
        ACLMessage c = messageFactory.requestQueryCancellation(sender, receiver, query.getConversationId());
        ACLMessage confirm = messageFactory.confirmQueryCancellation(receiver, sender, c);

        assertIsReply(confirm, c);
        assertEquals("fipa-query", confirm.getProtocol());
        assertEquals(ACLMessage.CONFIRM, confirm.getPerformative());
    }

    public void testFailToCancelQuery() throws Exception {
        ACLMessage query = messageFactory.poseQuery(sender, receiver, resourceX);
        ACLMessage c = messageFactory.requestQueryCancellation(sender, receiver, query.getConversationId());
        ErrorExplanation ex = new ErrorExplanation(ErrorExplanation.Type.InteractionExplired, "I don't remember you.");
        ACLMessage fail = messageFactory.failToCancelQuery(receiver, sender, c, ex);

        assertIsReply(fail, c);
        assertEquals("fipa-query", fail.getProtocol());
        assertEquals(ACLMessage.FAILURE, fail.getPerformative());
        assertHasExplanation(fail, "((interaction-expired \"I don't remember you.\"))");
        ErrorExplanation exp = messageFactory.extractErrorExplanation(fail);
        assertEquals(ErrorExplanation.Type.InteractionExplired, exp.getType());
        assertEquals("I don't remember you.", exp.getMessage());
    }

    public void testRequestSubscription() throws Exception {
        ACLMessage request;

        request = messageFactory.requestSubscription(sender, receiver, resourceX);
        assertIsSubscriptionRequest(request);
        assertEquals("((any ?dataset (describes ?dataset" +
                " (resource :uri http://example.org/resourceX))))", request.getContent());
        Value subject = messageFactory.extractDescribeQuery(request);
        assertTrue(subject instanceof URI);
        assertEquals("http://example.org/resourceX", subject.stringValue());
        assertNull(request.getUserDefinedParameter(RDFAgents.RDFAGENTS_ACCEPT_PARAMETER));

        // Note: more extensive tests of queries are to be found in testPoseQuery.
        // There's no need to repeat them here.

        request = messageFactory.requestSubscription(sender, receiver, resourceX, RDFContentLanguage.RDF_NQUADS);
        assertEquals("rdf-nquads", request.getUserDefinedParameter(RDFAgents.RDFAGENTS_ACCEPT_PARAMETER));

        request = messageFactory.requestSubscription(
                sender, receiver, resourceX, RDFContentLanguage.RDF_NQUADS, RDFContentLanguage.RDF_TRIG);
        assertEquals("rdf-nquads; rdf-trig", request.getUserDefinedParameter(RDFAgents.RDFAGENTS_ACCEPT_PARAMETER));
        //System.out.println(request);
    }

    public void testRefuseSubscriptionRequest() throws Exception {
        ACLMessage request = messageFactory.requestSubscription(sender, receiver, resourceX);
        ErrorExplanation ex = new ErrorExplanation(
                ErrorExplanation.Type.NotImplemented, "Subscriptions are not supported.");
        ACLMessage r = messageFactory.refuseSubscriptionRequest(receiver, sender, request, ex);
        System.out.println(r);

        assertIsReply(r, request);
        assertEquals(ACLMessage.REFUSE, r.getPerformative());
        assertHasExplanation(r, "((not-implemented \"Subscriptions are not supported.\"))");
        ErrorExplanation exp = messageFactory.extractErrorExplanation(r);
        assertEquals(ErrorExplanation.Type.NotImplemented, exp.getType());
        assertEquals("Subscriptions are not supported.", exp.getMessage());
    }

    public void testAgreeToSubscriptionRequest() throws Exception {
        ACLMessage request = messageFactory.requestSubscription(sender, receiver, resourceX);
        ACLMessage r = messageFactory.agreeToSubcriptionRequest(receiver, sender, request);
        System.out.println(r);

        assertIsReply(r, request);
        assertEquals(ACLMessage.AGREE, r.getPerformative());
        assertNull(r.getContent());
    }

    public void testInformOfSubscriptionUpdate() throws Exception {
        ACLMessage request, result;
        DatasetQuery q = new RecursiveDescribeQuery(ARTHUR, sail);

        request = messageFactory.requestSubscription(sender, receiver, ARTHUR);
        result = messageFactory.informOfSubscriptionUpdate(
                receiver, sender, request, q.evaluate(), RDFContentLanguage.RDF_TRIG);
        //System.out.println(result);
        assertIsReply(result, request);
        assertEquals(ACLMessage.INFORM_REF, result.getPerformative());
        assertEquals("fipa-subscribe", result.getProtocol());
        assertHasRDFContent(result);
        // TODO: validate content
        assertEquals("rdf-trig", result.getLanguage());

        request = messageFactory.requestSubscription(sender, receiver, ARTHUR, RDFContentLanguage.RDF_NQUADS);
        result = messageFactory.informOfSubscriptionUpdate(
                receiver, sender, request, q.evaluate(), RDFContentLanguage.RDF_TRIG);
        //System.out.println(result);
        assertIsReply(result, request);
        assertEquals(ACLMessage.INFORM_REF, result.getPerformative());
        assertEquals("fipa-subscribe", result.getProtocol());
        assertHasRDFContent(result);
        // TODO: validate content
        assertEquals("rdf-nquads", result.getLanguage());
    }

    public void testFailToInformOfSubscriptionUpdate() throws Exception {
        ACLMessage request = messageFactory.requestSubscription(sender, receiver, resourceX);
        ErrorExplanation ex = new ErrorExplanation(
                ErrorExplanation.Type.NotImplemented, "N-Quads format is not supported.");
        ACLMessage fail = messageFactory.failToInformOfSubscriptionUpdate(receiver, sender, request, ex);

        assertIsReply(fail, request);
        assertEquals(ACLMessage.FAILURE, fail.getPerformative());
        assertEquals("fipa-subscribe", fail.getProtocol());
        assertHasExplanation(fail, "((not-implemented \"N-Quads format is not supported.\"))");
        ErrorExplanation exp = messageFactory.extractErrorExplanation(fail);
        assertEquals(ErrorExplanation.Type.NotImplemented, exp.getType());
        assertEquals("N-Quads format is not supported.", exp.getMessage());
    }

    public void testRequestSubscriptionCancellation() throws Exception {
        ACLMessage request = messageFactory.requestSubscription(sender, receiver, resourceX);
        ACLMessage c = messageFactory.requestSubscriptionCancellation(sender, receiver, request.getConversationId());

        assertIsCancellationRequest(c, request);
        assertEquals("fipa-subscribe", c.getProtocol());
    }

    public void testConfirmSubscriptionCancellation() throws Exception {
        ACLMessage request = messageFactory.requestSubscription(sender, receiver, resourceX);
        ACLMessage c = messageFactory.requestSubscriptionCancellation(sender, receiver, request.getConversationId());
        ACLMessage confirm = messageFactory.confirmSubscriptionCancellation(receiver, sender, c);

        assertIsReply(confirm, c);
        assertEquals("fipa-subscribe", confirm.getProtocol());
        assertEquals(ACLMessage.CONFIRM, confirm.getPerformative());
    }

    public void testFailToCancelSubscription() throws Exception {
        ACLMessage request = messageFactory.requestSubscription(sender, receiver, resourceX);
        ACLMessage c = messageFactory.requestSubscriptionCancellation(sender, receiver, request.getConversationId());
        ErrorExplanation ex = new ErrorExplanation(ErrorExplanation.Type.InteractionExplired, "I don't remember you.");
        ACLMessage fail = messageFactory.failToCancelSubscription(receiver, sender, c, ex);

        assertIsReply(fail, c);
        assertEquals("fipa-subscribe", fail.getProtocol());
        assertEquals(ACLMessage.FAILURE, fail.getPerformative());
        assertHasExplanation(fail, "((interaction-expired \"I don't remember you.\"))");
        ErrorExplanation exp = messageFactory.extractErrorExplanation(fail);
        assertEquals(ErrorExplanation.Type.InteractionExplired, exp.getType());
        assertEquals("I don't remember you.", exp.getMessage());
    }

    private void assertFromAndTo(final ACLMessage message,
                                 final AgentId sender,
                                 final AgentId receiver) {
        matchAID(sender, message.getSender());
        int c = 0;
        jade.util.leap.Iterator iter = message.getAllIntendedReceiver();
        while (iter.hasNext()) {
            c++;
            iter.next();
        }
        assertEquals(1, c);
        matchAID(receiver, (AID) message.getAllIntendedReceiver().next());
    }

    private void assertIsQuery(final ACLMessage message) {
        //System.out.println(message);
        assertTrue(message.getConversationId().length() > 0);
        assertEquals("fipa-query", message.getProtocol());
        assertHasOntologyContent(message);
        assertEquals(ACLMessage.QUERY_REF, message.getPerformative());
        assertFromAndTo(message, sender, receiver);
    }

    private void assertIsSubscriptionRequest(final ACLMessage message) {
        assertTrue(message.getConversationId().length() > 0);
        assertEquals("fipa-subscribe", message.getProtocol());
        assertHasOntologyContent(message);
        assertEquals(ACLMessage.SUBSCRIBE, message.getPerformative());
        assertFromAndTo(message, sender, receiver);
    }

    private void assertHasOntologyContent(final ACLMessage message) {
        assertEquals("fipa-sl2", message.getLanguage());
        assertEquals("rdfagents", message.getOntology());
    }

    private void assertHasRDFContent(final ACLMessage message) {
        RDFContentLanguage language = RDFContentLanguage.getByName(message.getLanguage());
        assertNotNull(language);
        assertNull(message.getOntology());
    }

    private void assertHasExplanation(final ACLMessage message,
                                      final String ex) {
        assertHasOntologyContent(message);
        assertEquals(ex, message.getContent());
    }

    private void assertIsReply(final ACLMessage message,
                               final ACLMessage replyTo) {
        assertFromAndTo(replyTo, sender, receiver);
        assertFromAndTo(message, receiver, sender);
        assertTrue(message.getConversationId().length() > 0);
        assertEquals(replyTo.getConversationId(), message.getConversationId());
    }

    private void assertIsCancellationRequest(final ACLMessage message,
                                             final ACLMessage originalRequest) {
        assertFromAndTo(message, sender, receiver);
        assertEquals(ACLMessage.CANCEL, message.getPerformative());
        assertEquals(originalRequest.getProtocol(), message.getProtocol());
        assertEquals(originalRequest.getConversationId(), message.getConversationId());
    }

    private void matchAID(final AgentId expected,
                          final AID actual) {
        assertEquals(expected.getName(), actual.getName());

        Set<String> expectedAddresses = new HashSet<String>();
        for (URI u : expected.getTransportAddresses()) {
            expectedAddresses.add(u.toString());
        }

        assertEquals(expectedAddresses.size(), actual.getAddressesArray().length);
        for (String s : actual.getAddressesArray()) {
            assertTrue(expectedAddresses.contains(s));
        }
    }
}
