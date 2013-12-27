package telerik.academy.agora.reader;

import java.nio.CharBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

public class RssFeedHandler extends DefaultHandler {

	private static final String TAG = "RssFeedHandler";

	// List of items parsed
	private List<RssItem> rssItems;
	// We have a local reference to an object which is constructed while parser
	// is working on an item tag
	// Used to reference item while parsing
	private RssItem currentItem;
	// We have four indicators which are used to differentiate whether a tag
	// guid, title, description or pubDate is being processed by the parser
	// Parsing guid indicator
	private boolean parsingGuid;
	// Parsing title indicator
	private boolean parsingTitle;
	// Parsing description indicator
	private boolean parsingDescription;
	// Parsing pubDate indicator
	private boolean parsingPubDate;

	public RssFeedHandler() {
		rssItems = new ArrayList<RssItem>();
	}

	// We have an access method which returns a list of items that are read from
	// the RSS feed. This method will be called when parsing is done.
	public List<RssItem> getItems() {
		return rssItems;
	}

	// The StartElement method creates an empty RssItem object when an item
	// start tag is being processed. When a guid, title, description or pubDate
	// tag
	// are being processed appropriate indicators are set to true.
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if ("item".equals(qName)) {
			currentItem = new RssItem();
		} else if ("guid".equals(qName)) {
			parsingGuid = true;
		} else if ("title".equals(qName)) {
			parsingTitle = true;
		} else if ("description".equals(qName)) {
			parsingDescription = true;
		} else if ("pubDate".equals(qName)) {
			parsingPubDate = true;
		}
	}

	// The EndElement method adds the current RssItem to the list when a closing
	// item tag is processed. It sets appropriate indicators to false - when
	// guid, title, description and pubDate closing tags are processed
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if ("item".equals(qName)) {
			rssItems.add(currentItem);
			currentItem = null;
		} else if ("guid".equals(qName)) {
			parsingGuid = false;
		} else if ("title".equals(qName)) {
			parsingTitle = false;
		} else if ("description".equals(qName)) {
			parsingDescription = false;
		} else if ("pubDate".equals(qName)) {
			parsingPubDate = false;
		}
	}

	// Characters method fills current RssItem object with data when guid,
	// title, description and pubDate tag content is being processed
	@SuppressLint("SimpleDateFormat")
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (parsingGuid) {
			if (currentItem != null) {
				CharSequence source = CharBuffer.wrap(ch);
				String statusId = TextUtils.substring(source,
						TextUtils.lastIndexOf(source, '/') + 1, length);
				int id = Integer.parseInt(statusId);
				currentItem.setId(id);
				parsingGuid = false;
			}
		} else if (parsingTitle) {
			if (currentItem != null) {
				CharSequence source = CharBuffer.wrap(ch);
				String username = TextUtils.substring(source, 0,
						TextUtils.indexOf(source, ':'));
				currentItem.setUsername(username);
				parsingTitle = false;
			}
		} else if (parsingDescription) {
			if (currentItem != null) {
				currentItem.setText(new String(ch, start, length));
				parsingDescription = false;
			}
		} else if (parsingPubDate) {
			if (currentItem != null) {
				SimpleDateFormat format = new SimpleDateFormat(
						"dd MMM yyyy HH:mm:ss");
				Date createdAt;
				try {
					CharSequence source = CharBuffer.wrap(ch);
					String date = TextUtils.substring(source,
							TextUtils.indexOf(source, ',') + 2,
							TextUtils.indexOf(source, '-') - 2);

					createdAt = format.parse(date);
					currentItem.setCreatedAt(createdAt.getTime());
					parsingPubDate = false;
				} catch (ParseException e) {
					Log.e(TAG, "Failed to parse pubDate", e);
				}
			}
		}
	}
}
