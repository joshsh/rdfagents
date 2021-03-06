package net.fortytwo.rdfagents.jade;

import jade.content.ContentManager;
import jade.content.abs.AbsConcept;
import jade.content.abs.AbsContentElement;
import jade.content.abs.AbsIRE;
import jade.content.abs.AbsObject;
import jade.content.abs.AbsPredicate;
import jade.content.abs.AbsTerm;
import jade.content.abs.AbsVariable;
import jade.content.lang.Codec;
import jade.content.lang.sl.SL2Vocabulary;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import net.fortytwo.rdfagents.RDFAgents;
import net.fortytwo.rdfagents.data.DatasetFactory;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.MessageNotUnderstoodException;
import net.fortytwo.rdfagents.messaging.MessageRejectedException;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.ErrorExplanation;
import net.fortytwo.rdfagents.model.RDFContentLanguage;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class MessageFactory {
    private static final Logger logger = Logger.getLogger(MessageFactory.class.getName());

    private final Random random = new Random();

    private final ValueFactory valueFactory;
    private final DatasetFactory datasetFactory;

    private final ContentManager contentManager;
    private final SLCodec sl2Codec;
    private final Ontology rdfAgentsOntology;

    public MessageFactory(final DatasetFactory datasetFactory) {
        this.datasetFactory = datasetFactory;
        valueFactory = datasetFactory.getValueFactory();

        contentManager = new ContentManager();

        // We need SL-2 for IdentifyingReferenceExpressions
        // Note: the boolean argument seems to be important; the SL level of the codec appears to be downgraded to 1 if
        // a value of true is used.
        sl2Codec = new SLCodec(2, false);
        contentManager.registerLanguage(sl2Codec);
        rdfAgentsOntology = RDFAgentsOntology.getInstance();
        contentManager.registerOntology(rdfAgentsOntology, RDFAgents.RDFAGENTS_ONTOLOGY_NAME);
    }

    public Value extractDescribeQuery(final ACLMessage message) throws MessageNotUnderstoodException, LocalFailure {
        AbsContentElement el = extractAbsContent(message);

        // Note: this check is sufficient, as there is currently only one kind of IRE in RDFAgents
        if (!(el instanceof AbsIRE)) {
            throw new MessageNotUnderstoodException(
                    "expected IdentifyingReferenceExpression was not found in content: " + message.getContent());
        }

        try {
            AbsIRE any = (AbsIRE) el;
            String name1 = any.getVariable().getName();
            AbsPredicate pred = any.getProposition();
            AbsTerm d = pred.getAbsTerm(RDFAgentsOntology.DESCRIBES_DATASET);
            String name2 = ((AbsVariable) d).getName();
            if (!name1.equals(name2)) {
                throw new MessageNotUnderstoodException(
                        "variable names do not match in query: " + message.getContent());
            }
            AbsTerm term = pred.getAbsTerm(RDFAgentsOntology.DESCRIBES_SUBJECT);
            String typeName = term.getTypeName();
            switch (typeName) {
                case RDFAgentsOntology.RESOURCE:
                    return toResource(term, message);
                case RDFAgentsOntology.LITERAL:
                    return toLiteral(term, message);
                default:
                    throw new MessageNotUnderstoodException(
                            "resource of unexpected type in query: " + message.getContent());
            }
        } catch (NullPointerException | ClassCastException e) {
            throw new MessageNotUnderstoodException(
                    "invalid content for this type of message: " + message.getContent());
        }
    }

    public ErrorExplanation extractErrorExplanation(final ACLMessage message)
            throws MessageNotUnderstoodException, LocalFailure {

        AbsContentElement el = extractAbsContent(message);

        if (!(el instanceof AbsPredicate)) {
            throw new MessageNotUnderstoodException("expected explanation predicate was not found in content: "
                    + message.getContent());
        }

        AbsPredicate exp = (AbsPredicate) el;
        ErrorExplanation.Type type = ErrorExplanation.Type.getByFipaName(exp.getTypeName());
        if (null == type) {
            throw new MessageNotUnderstoodException("unexpected explanation type '"
                    + exp.getTypeName() + " in content: " + message.getContent());
        }

        try {
            String msg = exp.getAbsTerm(RDFAgentsOntology.EXPLANATION_MESSAGE).toString();
            return new ErrorExplanation(type, msg);
        } catch (NullPointerException | ClassCastException e) {
            throw new MessageNotUnderstoodException(
                    "invalid content for this type of message: " + message.getContent());
        }
    }

    public Dataset extractDataset(final ACLMessage message) throws MessageNotUnderstoodException, LocalFailure {
        RDFContentLanguage language = getRDFContentLanguage(message);
        assertRDFAgentsProtocolWithConvoId(message);

        String content = message.getContent();
        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
        try {
            Dataset sendersDataset = datasetFactory.parse(in, language);

            return datasetFactory.receiveDataset(sendersDataset, fromAID(message.getSender()));
        } catch (DatasetFactory.InvalidRDFContentException e) {
            throw new MessageNotUnderstoodException(e.getMessage());
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                throw new LocalFailure(e);
            }
        }
    }

    public ACLMessage notUnderstood(final ACLMessage replyTo,
                                    final AgentId sender,
                                    final AgentId intendedReceiver,
                                    final ErrorExplanation explanation) {
        ACLMessage message = createReplyTo(replyTo, sender, intendedReceiver, ACLMessage.NOT_UNDERSTOOD);

        try {
            addExplanation(message, explanation);
        } catch (LocalFailure e) {
            logger.severe("failed to generate not-understood message: " + e);
        }

        return message;
    }

    public ACLMessage failure(final AgentId sender,
                              final AgentId intendedReceiver,
                              final ACLMessage request,
                              final ErrorExplanation explanation) {

        ACLMessage message = createReplyTo(request, sender, intendedReceiver, ACLMessage.FAILURE);

        try {
            addExplanation(message, explanation);
        } catch (LocalFailure e) {
            logger.severe("failed to generate failure message: " + e);
        }

        return message;
    }

    public ACLMessage poseQuery(final AgentId sender,
                                final AgentId intendedReceiver,
                                final Value subject,
                                final RDFContentLanguage... acceptLanguages) throws LocalFailure {
        return requestForInfo(
                sender,
                intendedReceiver,
                subject,
                FIPANames.InteractionProtocol.FIPA_QUERY,
                ACLMessage.QUERY_REF,
                acceptLanguages);
    }

    public ACLMessage refuseToAnswerQuery(final AgentId sender,
                                          final AgentId intendedReceiver,
                                          final ACLMessage replyTo,
                                          final ErrorExplanation explanation)
            throws MessageNotUnderstoodException, LocalFailure {

        validateMessage(replyTo, FIPANames.InteractionProtocol.FIPA_QUERY, ACLMessage.QUERY_REF);

        ACLMessage message = createReplyTo(replyTo, sender, intendedReceiver, ACLMessage.REFUSE);

        addExplanation(message, explanation);

        return message;
    }

    public ACLMessage agreeToAnswerQuery(final AgentId sender,
                                         final AgentId intendedReceiver,
                                         final ACLMessage replyTo) throws MessageNotUnderstoodException {
        validateMessage(replyTo, FIPANames.InteractionProtocol.FIPA_QUERY, ACLMessage.QUERY_REF);

        // TODO: add content describing the "query-ref" action to which this agent is agreeing

        return createReplyTo(replyTo, sender, intendedReceiver, ACLMessage.AGREE);
    }

    public ACLMessage informOfQueryResult(final AgentId sender,
                                          final AgentId intendedReceiver,
                                          final ACLMessage replyTo,
                                          final Dataset dataset,
                                          final RDFContentLanguage defaultLanguage)
            throws MessageNotUnderstoodException, LocalFailure, MessageRejectedException {

        validateMessage(replyTo, FIPANames.InteractionProtocol.FIPA_QUERY, ACLMessage.QUERY_REF);

        return createAssertionalMessage(sender, intendedReceiver, dataset, defaultLanguage, replyTo);
    }

    public ACLMessage failToInformOfQueryResult(final AgentId sender,
                                                final AgentId intendedReceiver,
                                                final ACLMessage request,
                                                final ErrorExplanation explanation)
            throws MessageNotUnderstoodException {

        validateMessage(request, FIPANames.InteractionProtocol.FIPA_QUERY, ACLMessage.QUERY_REF);

        ACLMessage message = createReplyTo(request, sender, intendedReceiver, ACLMessage.FAILURE);

        try {
            addExplanation(message, explanation);
        } catch (LocalFailure e) {
            logger.severe("failed to generate failure message: " + e);
        }

        return message;
    }

    public ACLMessage requestQueryCancellation(final AgentId sender,
                                               final AgentId intendedReceiver,
                                               final String conversationId) {
        //validateMessage(query, FIPANames.InteractionProtocol.FIPA_QUERY, ACLMessage.QUERY_REF);

        ACLMessage message = new ACLMessage(ACLMessage.CANCEL);
        message.setSender(toAID(sender));
        message.addReceiver(toAID(intendedReceiver));
        message.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
        message.setConversationId(conversationId);

        return message;
    }

    public ACLMessage confirmQueryCancellation(final AgentId sender,
                                               final AgentId intendedReceiver,
                                               final ACLMessage request)
            throws MessageRejectedException, MessageNotUnderstoodException {

        validateMessage(request, FIPANames.InteractionProtocol.FIPA_QUERY, ACLMessage.CANCEL);

        return createReplyTo(request, sender, intendedReceiver, ACLMessage.CONFIRM);
    }

    public ACLMessage failToCancelQuery(final AgentId sender,
                                        final AgentId intendedReceiver,
                                        final ACLMessage request,
                                        final ErrorExplanation explanation) throws MessageNotUnderstoodException {
        validateMessage(request, FIPANames.InteractionProtocol.FIPA_QUERY, ACLMessage.CANCEL);

        ACLMessage message = createReplyTo(request, sender, intendedReceiver, ACLMessage.FAILURE);

        try {
            addExplanation(message, explanation);
        } catch (LocalFailure e) {
            logger.severe("failed to generate failure message: " + e);
        }

        return message;
    }

    public ACLMessage requestSubscription(final AgentId sender,
                                          final AgentId intendedReceiver,
                                          final Value subject,
                                          final RDFContentLanguage... acceptLanguages) throws LocalFailure {
        return requestForInfo(
                sender,
                intendedReceiver,
                subject,
                FIPANames.InteractionProtocol.FIPA_SUBSCRIBE,
                ACLMessage.SUBSCRIBE,
                acceptLanguages);
    }

    public ACLMessage refuseSubscriptionRequest(final AgentId sender,
                                                final AgentId intendedReceiver,
                                                final ACLMessage replyTo,
                                                final ErrorExplanation explanation)
            throws MessageNotUnderstoodException, LocalFailure {

        validateMessage(replyTo, FIPANames.InteractionProtocol.FIPA_SUBSCRIBE, ACLMessage.SUBSCRIBE);

        ACLMessage message = createReplyTo(replyTo, sender, intendedReceiver, ACLMessage.REFUSE);

        addExplanation(message, explanation);

        return message;
    }

    public ACLMessage agreeToSubcriptionRequest(final AgentId sender,
                                                final AgentId intendedReceiver,
                                                final ACLMessage replyTo)
            throws MessageRejectedException, MessageNotUnderstoodException {

        validateMessage(replyTo, FIPANames.InteractionProtocol.FIPA_SUBSCRIBE, ACLMessage.SUBSCRIBE);

        // TODO: add content describing the "subscribe" action to which this agent is agreeing

        return createReplyTo(replyTo, sender, intendedReceiver, ACLMessage.AGREE);
    }

    public ACLMessage informOfSubscriptionUpdate(final AgentId sender,
                                                 final AgentId intendedReceiver,
                                                 final ACLMessage replyTo,
                                                 final Dataset dataset,
                                                 final RDFContentLanguage defaultLanguage)
            throws MessageRejectedException, MessageNotUnderstoodException, LocalFailure {

        validateMessage(replyTo, FIPANames.InteractionProtocol.FIPA_SUBSCRIBE, ACLMessage.SUBSCRIBE);

        return createAssertionalMessage(sender, intendedReceiver, dataset, defaultLanguage, replyTo);
    }

    public ACLMessage failToInformOfSubscriptionUpdate(final AgentId sender,
                                                       final AgentId intendedReceiver,
                                                       final ACLMessage request,
                                                       final ErrorExplanation explanation)
            throws MessageNotUnderstoodException {

        validateMessage(request, FIPANames.InteractionProtocol.FIPA_SUBSCRIBE, ACLMessage.SUBSCRIBE);

        ACLMessage message = createReplyTo(request, sender, intendedReceiver, ACLMessage.FAILURE);

        try {
            addExplanation(message, explanation);
        } catch (LocalFailure e) {
            logger.severe("failed to generate failure message: " + e);
        }

        return message;
    }

    public ACLMessage requestSubscriptionCancellation(final AgentId sender,
                                                      final AgentId intendedReceiver,
                                                      final String conversationId) {
        //validateMessage(subscribe, FIPANames.InteractionProtocol.FIPA_SUBSCRIBE, ACLMessage.SUBSCRIBE);

        ACLMessage message = new ACLMessage(ACLMessage.CANCEL);
        message.setSender(toAID(sender));
        message.addReceiver(toAID(intendedReceiver));
        message.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
        message.setConversationId(conversationId);

        return message;
    }

    public ACLMessage confirmSubscriptionCancellation(final AgentId sender,
                                                      final AgentId intendedReceiver,
                                                      final ACLMessage request)
            throws MessageRejectedException, MessageNotUnderstoodException {

        validateMessage(request, FIPANames.InteractionProtocol.FIPA_SUBSCRIBE, ACLMessage.CANCEL);

        return createReplyTo(request, sender, intendedReceiver, ACLMessage.CONFIRM);
    }

    public ACLMessage failToCancelSubscription(final AgentId sender,
                                               final AgentId intendedReceiver,
                                               final ACLMessage request,
                                               final ErrorExplanation explanation)
            throws MessageNotUnderstoodException {

        validateMessage(request, FIPANames.InteractionProtocol.FIPA_SUBSCRIBE, ACLMessage.CANCEL);

        ACLMessage message = createReplyTo(request, sender, intendedReceiver, ACLMessage.FAILURE);

        try {
            addExplanation(message, explanation);
        } catch (LocalFailure e) {
            logger.severe("failed to generate failure message: " + e);
        }

        return message;
    }


    private Resource toResource(AbsTerm term, ACLMessage message) throws MessageNotUnderstoodException {
        String iri = term.getAbsObject(RDFAgentsOntology.RESOURCE_IRI).toString();
        if (!isValidIRI(iri)) {
            throw new MessageNotUnderstoodException(
                    "invalid IRI reference '" + iri + "' in query: " + message.getContent());
        }

        try {
            return valueFactory.createIRI(iri);
        } catch (IllegalArgumentException e) {
            throw new MessageNotUnderstoodException("illegal IRI in query: " + message.getContent());
        }
    }

    private Literal toLiteral(AbsTerm term, ACLMessage message) throws MessageNotUnderstoodException {
        String label = term.getAbsObject(RDFAgentsOntology.LITERAL_LABEL).toString();
        AbsObject languageObj = term.getAbsObject(RDFAgentsOntology.LITERAL_LANGUAGE);
        AbsObject datatypeObj = term.getAbsObject(RDFAgentsOntology.LITERAL_DATATYPE);

        try {
            if (null != languageObj) {
                String lang = languageObj.toString();
                return valueFactory.createLiteral(label, lang);
            }

            if (null != datatypeObj) {
                String iri = datatypeObj.getAbsObject(RDFAgentsOntology.RESOURCE_IRI).toString();
                if (!isValidIRI(iri)) {
                    throw new MessageNotUnderstoodException(
                            "invalid datatype IRI '" + iri + "' in query: " + message.getContent());
                }
                return valueFactory.createLiteral(label, valueFactory.createIRI(iri));
            }

            return valueFactory.createLiteral(label);
        } catch (IllegalArgumentException e) {
            throw new MessageNotUnderstoodException(
                    "illegal literal value in query: " + message.getContent());
        }
    }

    private AbsContentElement extractAbsContent(final ACLMessage message)
            throws MessageNotUnderstoodException, LocalFailure {

        assertSLContent(message);
        assertRDFAgentsProtocolWithConvoId(message);

        try {
            return contentManager.extractAbsContent(message);
        } catch (Codec.CodecException e) {
            // TODO: this is always "our fault", right?
            throw new LocalFailure(e);
        } catch (OntologyException e) {
            // TODO: is this message sufficiently informative?
            throw new MessageNotUnderstoodException(e.getMessage());
        }
    }

    private String getContentLanguage(final ACLMessage message) throws MessageNotUnderstoodException {
        assertHasContent(message);

        String l = message.getLanguage();

        if (null == l) {
            throw new MessageNotUnderstoodException("missing content language for message");
        }

        return l;
    }

    private RDFContentLanguage getRDFContentLanguage(final ACLMessage message) throws MessageNotUnderstoodException {
        String l = getContentLanguage(message);

        RDFContentLanguage language = RDFContentLanguage.getByName(l);

        if (null == language) {
            throw new MessageNotUnderstoodException("unknown or unsupported RDF content language: " + l);
        }

        if (message.getPerformative() != ACLMessage.INFORM_REF) {
            throw new MessageNotUnderstoodException(
                    "unexpected performative (expected " + ACLMessage.INFORM_REF + ")");
        }

        return language;
    }

    private void assertHasContent(final ACLMessage message) throws MessageNotUnderstoodException {
        if (null == message.getContent() || 0 == message.getContent().length()) {
            throw new MessageNotUnderstoodException("missing content");
        }
    }

    private void assertSLContent(final ACLMessage message) throws MessageNotUnderstoodException {
        String l = getContentLanguage(message);
        if (!l.equals(FIPANames.ContentLanguage.FIPA_SL2)) {
            throw new MessageNotUnderstoodException("wrong content language for this type of message (found " + l
                    + ", expected " + FIPANames.ContentLanguage.FIPA_SL2);
        }

        String o = message.getOntology();
        if (null == o) {
            throw new MessageNotUnderstoodException("missing ontology value (expected 'rdfagents')");
        }
        if (!o.equals(RDFAgents.RDFAGENTS_ONTOLOGY_NAME)) {
            throw new MessageNotUnderstoodException("unexpected ontology value (expected 'rdfagents'): '" + o + "'");
        }
    }

    private void assertRDFAgentsProtocolWithConvoId(final ACLMessage message) throws MessageNotUnderstoodException {
        String protocol = message.getProtocol();
        if (null == protocol) {
            throw new MessageNotUnderstoodException("missing protocol value (expecting '" +
                    RDFAgents.Protocol.Query.getFipaName() + "' or '" +
                    RDFAgents.Protocol.Subscribe.getFipaName() + "'");
        }
        if (null == RDFAgents.Protocol.getByName(protocol)) {
            throw new MessageNotUnderstoodException("unexpected protocol value (expecting '" +
                    RDFAgents.Protocol.Query.getFipaName() + "' or '" +
                    RDFAgents.Protocol.Subscribe.getFipaName() + "': " + protocol);
        }

        if (null == message.getConversationId()) {
            throw new MessageNotUnderstoodException("missing conversation id");
        }
    }

    private void addExplanation(final ACLMessage message,
                                final ErrorExplanation ex) throws LocalFailure {
        message.setLanguage(sl2Codec.getName());
        message.setOntology(rdfAgentsOntology.getName());

        try {
            AbsPredicate exp = new AbsPredicate(ex.getType().getFipaName());
            exp.set(RDFAgentsOntology.EXPLANATION_MESSAGE, ex.getMessage());

            contentManager.fillContent(message, exp);
        } catch (Codec.CodecException | OntologyException e) {
            throw new LocalFailure(e);
        }
    }

    private boolean isValidIRI(final String iri) {
        try {
            // TODO: replace this with a more formal criterion based on the IRI spec
            valueFactory.createIRI(iri);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private ACLMessage createReplyTo(final ACLMessage replyTo,
                                     final AgentId sender,
                                     final AgentId intendedReceiver,
                                     final int performative) {
        ACLMessage message = new ACLMessage(performative);
        // Note: for now, using the return AID provided in the original message.
        message.setSender(toAID(sender));
        //message.addReceiver(toAID(intendedReceiver));
        message.addReceiver(replyTo.getSender());
        message.setProtocol(replyTo.getProtocol());
        message.setConversationId(replyTo.getConversationId());
        return message;
    }

    private void validateMessage(final ACLMessage message,
                                 final String protocol,
                                 final int performative) throws MessageNotUnderstoodException {
        if (null == message.getConversationId()) {
            throw new MessageNotUnderstoodException("missing conversation ID");
        }

        if (null == message.getProtocol() || 0 == message.getProtocol().length()) {
            throw new MessageNotUnderstoodException("missing protocol");
        }

        if (!message.getProtocol().equals(protocol)) {
            throw new MessageNotUnderstoodException("unexpected protocol: " + message.getProtocol());
        }

        if (performative != message.getPerformative()) {
            throw new MessageNotUnderstoodException("unexpected performative (code): " + performative);
        }
    }

    private ACLMessage createAssertionalMessage(final AgentId sender,
                                                final AgentId intendedReceiver,
                                                final Dataset dataset,
                                                final RDFContentLanguage defaultLanguage,
                                                final ACLMessage replyTo)
            throws MessageNotUnderstoodException, LocalFailure, MessageRejectedException {

        ACLMessage message = createReplyTo(replyTo, sender, intendedReceiver, ACLMessage.INFORM_REF);

        RDFContentLanguage language = chooseRDFContentLanguage(replyTo, defaultLanguage);
        message.setLanguage(language.getFipaName());

        /*
        System.out.println("original dataset:");
        try {
            datasetFactory.write(System.out, dataset, RDFContentLanguage.RDF_TRIG);
        } catch (LocalFailure localFailure) {
            localFailure.printStackTrace();
        }  //*/

        Dataset safe = datasetFactory.renameGraphs(dataset);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            // note: if the Dataset is empty, Jade will omit the content key/value pair from the message entirely
            datasetFactory.write(out, safe, language);
            //message.setByteSequenceContent(out.toByteArray());
            message.setContent(out.toString());
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                throw new LocalFailure("internal I/O error while writing RDF content entity: " + e.getMessage());
            }
        }

        return message;
    }

    private RDFContentLanguage chooseRDFContentLanguage(final ACLMessage request,
                                                        final RDFContentLanguage defaultLanguage)
            throws MessageNotUnderstoodException, MessageRejectedException {

        String langs = request.getUserDefinedParameter(RDFAgents.RDFAGENTS_ACCEPT_PARAMETER);
        if (null == langs) {
            return defaultLanguage;
        } else {
            for (String l : langs.split("[;]")) {
                l = l.trim();
                if (0 == l.length()) {
                    throw new MessageNotUnderstoodException(
                            "invalid '" + RDFAgents.RDFAGENTS_ACCEPT_PARAMETER + "' value: " + langs);
                }

                RDFContentLanguage g = RDFContentLanguage.getByName(l);
                // TODO: allow the implementation to support arbitrary content languages
                // (i.e. make the enum into a class with a registry)
                if (null == g) {
                    throw new MessageNotUnderstoodException("unknown RDF content language: " + l);
                }

                // TODO: language preference (as opposed to first-match)?
                if (datasetFactory.getSupportedLanguages().contains(g)) {
                    return g;
                }
            }

            throw new MessageRejectedException(
                    new ErrorExplanation(ErrorExplanation.Type.NotImplemented,
                            "specified RDF content languages are not supported: " + langs));
        }
    }

    private AbsConcept valueToConcept(final Value value) {
        if (value instanceof IRI) {
            return iriToConcept((IRI) value);
        } else if (value instanceof Literal) {
            return literalToConcept((Literal) value);
        } else {
            throw new IllegalArgumentException("resource is of an unexpected class: " + value);
        }
    }

    private AbsConcept iriToConcept(final IRI value) {
        AbsConcept c = new AbsConcept(RDFAgentsOntology.RESOURCE);
        c.set(RDFAgentsOntology.RESOURCE_IRI, value.stringValue());

        return c;
    }

    private AbsConcept literalToConcept(final Literal value) {
        AbsConcept c = new AbsConcept(RDFAgentsOntology.LITERAL);

        c.set(RDFAgentsOntology.LITERAL_LABEL, value.getLabel());

        if (value.getLanguage().isPresent()) {
            c.set(RDFAgentsOntology.LITERAL_LANGUAGE, value.getLanguage().get());
        }

        if (null != value.getDatatype()) {
            c.set(RDFAgentsOntology.LITERAL_DATATYPE, iriToConcept(value.getDatatype()));
        }

        return c;
    }

    private ACLMessage requestForInfo(final AgentId sender,
                                      final AgentId intendedReceiver,
                                      final Value subject,
                                      final String protocol,
                                      final int performative,
                                      final RDFContentLanguage... acceptLanguages) throws LocalFailure {

        ACLMessage message = new ACLMessage(performative);
        message.setSender(toAID(sender));
        message.addReceiver(toAID(intendedReceiver));
        message.setProtocol(protocol);
        message.setConversationId(createConversationId());
        message.setLanguage(sl2Codec.getName());
        message.setOntology(rdfAgentsOntology.getName());

        if (0 < acceptLanguages.length) {
            boolean first = true;
            StringBuilder sb = new StringBuilder();
            for (RDFContentLanguage l : acceptLanguages) {
                if (first) {
                    first = false;
                } else {
                    sb.append("; ");
                }

                sb.append(l.getFipaName());
            }

            message.addUserDefinedParameter(RDFAgents.RDFAGENTS_ACCEPT_PARAMETER, sb.toString());
        }

        try {
            AbsConcept s = valueToConcept(subject);
            AbsPredicate describes = new AbsPredicate(RDFAgentsOntology.DESCRIBES);
            AbsVariable d = new AbsVariable("dataset", RDFAgentsOntology.VALUE);
            describes.set(RDFAgentsOntology.DESCRIBES_DATASET, d);
            describes.set(RDFAgentsOntology.DESCRIBES_SUBJECT, s);

            AbsIRE any = new AbsIRE(SL2Vocabulary.ANY);
            any.setVariable(d);
            any.setProposition(describes);

            contentManager.fillContent(message, any);
        } catch (Codec.CodecException | OntologyException e) {
            throw new LocalFailure(e);
        }

        return message;
    }

    private String createConversationId() {
        // Local name will be a UUID (without the dashes).
        byte[] bytes = new byte[32];

        for (int i = 0; i < 32; i++) {
            int c = random.nextInt(16);
            bytes[i] = (byte) ((c > 9) ? c - 10 + 'a' : c + '0');
        }

        return new String(bytes);
    }

    public AID toAID(final AgentId ref) {
        AID a = new AID();
        a.setName(ref.getName());
        for (IRI u : ref.getTransportAddresses()) {
            a.addAddresses(u.toString());
        }

        return a;
    }

    public AgentId fromAID(final AID s) throws MessageNotUnderstoodException {
        if (null == s.getName()) {
            throw new MessageNotUnderstoodException("missing agent name in message");
        }

        if (!RDFAgents.isValidIRI(s.getName())) {
            throw new MessageNotUnderstoodException("agent name is not a legal IRI: " + s.getName());
        }

        IRI name = valueFactory.createIRI(s.getName());

        Collection<IRI> addresses = new LinkedList<>();
        for (String address : s.getAddressesArray()) {
            if (null == address) {
                throw new MessageNotUnderstoodException("null sender's transport address in message");
            }

            if (!RDFAgents.isValidIRI(address)) {
                logger.info("sender's address could not be converted to an IRI: " + address);
            } else {
                addresses.add(valueFactory.createIRI(address));
            }
        }

        IRI[] iris = new IRI[addresses.size()];
        int i = 0;
        for (IRI u : addresses) {
            iris[i++] = u;
        }
        return new AgentId(name, iris);
    }
}
