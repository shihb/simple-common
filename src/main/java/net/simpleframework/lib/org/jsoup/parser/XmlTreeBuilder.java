package net.simpleframework.lib.org.jsoup.parser;

import java.util.Iterator;
import java.util.List;

import net.simpleframework.lib.org.jsoup.helper.Validate;
import net.simpleframework.lib.org.jsoup.nodes.Comment;
import net.simpleframework.lib.org.jsoup.nodes.DocumentType;
import net.simpleframework.lib.org.jsoup.nodes.Element;
import net.simpleframework.lib.org.jsoup.nodes.Node;
import net.simpleframework.lib.org.jsoup.nodes.TextNode;
import net.simpleframework.lib.org.jsoup.nodes.XmlDeclaration;

/**
 * @author Jonathan Hedley
 */
public class XmlTreeBuilder extends TreeBuilder {
	@Override
	protected void initialiseParse(final String input, final String baseUri,
			final ParseErrorList errors) {
		super.initialiseParse(input, baseUri, errors);
		stack.add(doc); // place the document onto the stack. differs from
								// HtmlTreeBuilder (not on stack)
	}

	@Override
	protected boolean process(final Token token) {
		// start tag, end tag, doctype, comment, character, eof
		switch (token.type) {
		case StartTag:
			insert(token.asStartTag());
			break;
		case EndTag:
			popStackToClose(token.asEndTag());
			break;
		case Comment:
			insert(token.asComment());
			break;
		case Character:
			insert(token.asCharacter());
			break;
		case Doctype:
			insert(token.asDoctype());
			break;
		case EOF: // could put some normalisation here if desired
			break;
		default:
			Validate.fail("Unexpected token type: " + token.type);
		}
		return true;
	}

	private void insertNode(final Node node) {
		currentElement().appendChild(node);
	}

	Element insert(final Token.StartTag startTag) {
		final Tag tag = Tag.valueOf(startTag.name());
		// todo: wonder if for xml parsing, should treat all tags as unknown?
		// because it's not html.
		final Element el = new Element(tag, baseUri, startTag.attributes);
		insertNode(el);
		if (startTag.isSelfClosing()) {
			tokeniser.acknowledgeSelfClosingFlag();
			if (!tag.isKnownTag()) {
				tag.setSelfClosing();
			}
		} else {
			stack.add(el);
		}
		return el;
	}

	void insert(final Token.Comment commentToken) {
		final Comment comment = new Comment(commentToken.getData(), baseUri);
		Node insert = comment;
		if (commentToken.bogus) { // xml declarations are emitted as bogus
											// comments (which is right for html, but not
											// xml)
			final String data = comment.getData();
			if (data.length() > 1 && (data.startsWith("!") || data.startsWith("?"))) {
				final String declaration = data.substring(1);
				insert = new XmlDeclaration(declaration, comment.baseUri(), data.startsWith("!"));
			}
		}
		insertNode(insert);
	}

	void insert(final Token.Character characterToken) {
		final Node node = new TextNode(characterToken.getData(), baseUri);
		insertNode(node);
	}

	void insert(final Token.Doctype d) {
		final DocumentType doctypeNode = new DocumentType(d.getName(), d.getPublicIdentifier(),
				d.getSystemIdentifier(), baseUri);
		insertNode(doctypeNode);
	}

	/**
	 * If the stack contains an element with this tag's name, pop up the stack to
	 * remove the first occurrence. If not found, skips.
	 * 
	 * @param endTag
	 */
	private void popStackToClose(final Token.EndTag endTag) {
		final String elName = endTag.name();
		Element firstFound = null;

		Iterator<Element> it = stack.descendingIterator();
		while (it.hasNext()) {
			final Element next = it.next();
			if (next.nodeName().equals(elName)) {
				firstFound = next;
				break;
			}
		}
		if (firstFound == null) {
			return; // not found, skip
		}

		it = stack.descendingIterator();
		while (it.hasNext()) {
			final Element next = it.next();
			if (next == firstFound) {
				it.remove();
				break;
			} else {
				it.remove();
			}
		}
	}

	List<Node> parseFragment(final String inputFragment, final String baseUri,
			final ParseErrorList errors) {
		initialiseParse(inputFragment, baseUri, errors);
		runParser();
		return doc.childNodes();
	}
}
