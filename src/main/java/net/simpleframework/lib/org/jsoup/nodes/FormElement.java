package net.simpleframework.lib.org.jsoup.nodes;

import java.util.ArrayList;
import java.util.List;

import net.simpleframework.lib.org.jsoup.Connection;
import net.simpleframework.lib.org.jsoup.Jsoup;
import net.simpleframework.lib.org.jsoup.helper.HttpConnection;
import net.simpleframework.lib.org.jsoup.helper.Validate;
import net.simpleframework.lib.org.jsoup.parser.Tag;
import net.simpleframework.lib.org.jsoup.select.Elements;

/**
 * A HTML Form Element provides ready access to the form fields/controls that
 * are associated with it. It also allows a form to easily be submitted.
 */
public class FormElement extends Element {
	private final Elements elements = new Elements();

	/**
	 * Create a new, standalone form element.
	 * 
	 * @param tag
	 *           tag of this element
	 * @param baseUri
	 *           the base URI
	 * @param attributes
	 *           initial attributes
	 */
	public FormElement(final Tag tag, final String baseUri, final Attributes attributes) {
		super(tag, baseUri, attributes);
	}

	/**
	 * Get the list of form control elements associated with this form.
	 * 
	 * @return form controls associated with this element.
	 */
	public Elements elements() {
		return elements;
	}

	/**
	 * Add a form control element to this form.
	 * 
	 * @param element
	 *           form control to add
	 * @return this form element, for chaining
	 */
	public FormElement addElement(final Element element) {
		elements.add(element);
		return this;
	}

	/**
	 * Prepare to submit this form. A Connection object is created with the
	 * request set up from the form values. You can then set up other options
	 * (like user-agent, timeout, cookies), then execute it.
	 * 
	 * @return a connection prepared from the values of this form.
	 * @throws IllegalArgumentException
	 *            if the form's absolute action URL cannot be determined. Make
	 *            sure you pass the document's base URI when parsing.
	 */
	public Connection submit() {
		final String action = hasAttr("action") ? absUrl("action") : baseUri();
		Validate
				.notEmpty(action,
						"Could not determine a form action URL for submit. Ensure you set a base URI when parsing.");
		final Connection.Method method = attr("method").toUpperCase().equals("POST") ? Connection.Method.POST
				: Connection.Method.GET;

		final Connection con = Jsoup.connect(action).data(formData()).method(method);

		return con;
	}

	/**
	 * Get the data that this form submits. The returned list is a copy of the
	 * data, and changes to the contents of the list will not be reflected in the
	 * DOM.
	 * 
	 * @return a list of key vals
	 */
	public List<Connection.KeyVal> formData() {
		final ArrayList<Connection.KeyVal> data = new ArrayList<Connection.KeyVal>();

		// iterate the form control elements and accumulate their values
		for (final Element el : elements) {
			if (!el.tag().isFormSubmittable()) {
				continue; // contents are form listable, superset of submitable
			}
			final String name = el.attr("name");
			if (name.length() == 0) {
				continue;
			}

			if ("select".equals(el.tagName())) {
				final Elements options = el.select("option[selected]");
				for (final Element option : options) {
					data.add(HttpConnection.KeyVal.create(name, option.val()));
				}
			} else {
				data.add(HttpConnection.KeyVal.create(name, el.val()));
			}
		}
		return data;
	}

	@Override
	public boolean equals(final Object o) {
		return super.equals(o);
	}
}
