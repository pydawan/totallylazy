package com.googlecode.totallylazy;

import com.googlecode.totallylazy.iterators.NodeIterator;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import static com.googlecode.totallylazy.Runnables.VOID;

public class Xml {
    public static final Escaper DEFAULT_ESCAPER = new Escaper().
            withRule('&', "&amp;").
            withRule('<', "&lt;").
            withRule('>', "&gt;").
            withRule('\'', "&#39;").
            withRule('"', "&quot;").
            withRule(Strings.unicodeControlOrUndefinedCharacter(), toXmlEntity());

    public static String selectContents(final Node node, final String expression) {
        try {
            return contents(internalSelectNodes(node, expression));
        } catch (XPathExpressionException e) {
            try {
                return (String) xpath().evaluate(expression, node, XPathConstants.STRING);
            } catch (XPathExpressionException ignore) {
                throw LazyException.lazyException(e);
            }
        }
    }

    public static Sequence<Node> selectNodes(final Node node, final String expression) {
        try {
            return internalSelectNodes(node, expression);
        } catch (XPathExpressionException e) {
            throw LazyException.lazyException(e);
        }
    }

    public static Number selectNumber(final Node node, final String expression) {
        try {
            return (Number) xpath().evaluate(expression, node, XPathConstants.NUMBER);
        } catch (XPathExpressionException e) {
            throw LazyException.lazyException(e);
        }
    }

    public static boolean matches(final Node node, final String expression) {
        try {
            return (Boolean) xpath().evaluate(expression, node, XPathConstants.BOOLEAN);
        } catch (XPathExpressionException e) {
            throw LazyException.lazyException(e);
        }
    }

    private static Sequence<Node> internalSelectNodes(final Node node, final String expression) throws XPathExpressionException {
        return sequence((NodeList) xpath().evaluate(expression, node, XPathConstants.NODESET));
    }

    public static Option<Node> selectNode(final Node node, final String expression) {
        return selectNodes(node, expression).headOption();
    }

    public static Sequence<Element> selectElements(final Node node, final String expression) {
        return selectNodes(node, expression).safeCast(Element.class);
    }

    public static Option<Element> selectElement(final Node node, final String expression) {
        return selectElements(node, expression).headOption();
    }

    public static XPath xpath() {
        return XPathFactory.newInstance().newXPath();
    }

    public static Sequence<Node> sequence(final NodeList nodes) {
        return new Sequence<Node>() {
            public Iterator<Node> iterator() {
                return new NodeIterator(nodes);
            }
        };
    }

    public static String contents(Sequence<Node> nodes) {
        return nodes.map(contents()).toString("");
    }

    public static Function1<Element, Void> removeAttribute(final String name) {
        return new Function1<Element, Void>() {
            public Void call(Element element) throws Exception {
                element.removeAttribute(name);
                return VOID;
            }
        };
    }

    public static Function1<Node, String> contents() {
        return new Function1<Node, String>() {
            public String call(Node node) throws Exception {
                return contents(node);
            }
        };
    }

    public static String contents(Node node) throws Exception {
        if (node instanceof Attr) {
            return contents((Attr) node);
        }
        if (node instanceof CharacterData) {
            return contents((CharacterData) node);
        }
        if (node instanceof Element) {
            return contents((Element) node);
        }
        throw new UnsupportedOperationException("Unknown node type " + node.getClass());
    }

    public static String contents(CharacterData characterData) {
        return characterData.getData();
    }

    public static String contents(Attr attr) {
        return attr.getValue();
    }

    public static String contents(Element element) throws Exception {
        return sequence(element.getChildNodes()).map(new Callable1<Node, String>() {
            public String call(Node node) throws Exception {
                if (node instanceof Element) {
                    return asString((Element) node);
                }
                return contents(node);
            }
        }).toString("");

    }

    public static String asString(Element element) throws Exception {
        Transformer transformer = transformer();
        StringWriter writer = new StringWriter();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(element), new StreamResult(writer));
        return writer.toString();
    }

    @SuppressWarnings("unchecked")
    public static Transformer transformer() throws TransformerConfigurationException {
        return internalTransformer();
    }

    public static Transformer transformer(Pair<String, Object>... attributes) throws TransformerConfigurationException {
        return internalTransformer(attributes);
    }

    private static Transformer internalTransformer(Pair<String, Object>... attributes) throws TransformerConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        for (Pair<String, Object> attribute : attributes) {
            transformerFactory.setAttribute(attribute.first(), attribute.second());
        }
        return transformerFactory.newTransformer();
    }

    public static Document document(byte[] bytes) {
        try {
            return document(new String(bytes, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw LazyException.lazyException(e);
        }
    }

    public static Document document(String xml) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setEntityResolver(ignoreEntities());
            documentBuilder.setErrorHandler(null);
            return documentBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
        } catch (Exception e) {
            throw LazyException.lazyException(e);
        }
    }

    private static EntityResolver ignoreEntities() {
        return new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                return new InputSource(new StringReader(""));
            }
        };
    }

    public static Sequence<Node> remove(final Node root, final String expression) {
        return remove(selectNodes(root, expression));
    }

    public static Sequence<Node> remove(final Sequence<Node> nodes) {
        return nodes.map(remove()).realise();
    }

    private static Function1<Node, Node> remove() {
        return new Function1<Node, Node>() {
            public Node call(Node node) throws Exception {
                return node.getParentNode().removeChild(node);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static String format(final Node node) throws Exception {
        return format(node, Pair.<String, Object>pair("indent-number", 4));
    }

    public static String format(final Node node, final Pair<String, Object>... attributes) throws Exception {
        Transformer transformer = transformer(attributes);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.toString();
    }

    public static String escape(Object value) {
        return DEFAULT_ESCAPER.
                escape(value);
    }

    public static Function1<Object, String> escape() {
        return new Function1<Object, String>() {
            public String call(Object value) throws Exception {
                return escape(value);
            }
        };
    }

    public static Function1<Character, String> toXmlEntity() {
        return new Function1<Character, String>() {
            public String call(Character character) throws Exception {
                return String.format("&#%s;", Integer.toString(character, 10));
            }
        };
    }

}
