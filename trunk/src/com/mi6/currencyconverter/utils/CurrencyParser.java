package com.mi6.currencyconverter.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.mi6.currencyconverter.dto.CurrencyDetails;

import android.util.Log;

public class CurrencyParser {

	private static final String tag = "CurrencyParser";
	private static final String FILE_EXTENSION= ".png";
	
	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;
	private final HashMap<String, String> map;
	private final List<CurrencyDetails> list;

	public CurrencyParser() {
		this.list = new ArrayList<CurrencyDetails>();
		this.map = new HashMap<String, String>();
	}

	private String getNodeValue(NamedNodeMap map, String key) {
		String nodeValue = null;
		Node node = map.getNamedItem(key);
		if (node != null) {
			nodeValue = node.getNodeValue();
		}
		return nodeValue;
	}

	public List<CurrencyDetails> getList() {
		return this.list;
	}

	public String getFlag(String currency) {
		return (String) this.map.get(currency);
	}

	/**
	 * Parse XML file containing body part X/Y/Description
	 * 
	 * @param inStream
	 */
	public void parse(InputStream inStream) {
		try {
			// TODO: after we must do a cache of this XML!!!!
			this.factory = DocumentBuilderFactory.newInstance();
			this.builder = this.factory.newDocumentBuilder();
			this.builder.isValidating();
			Document doc = this.builder.parse(inStream, null);

			doc.getDocumentElement().normalize();

			NodeList currencyList = doc.getElementsByTagName("currency");
			final int length = currencyList.getLength();

			for (int i = 0; i < length; i++) {
				final NamedNodeMap attr = currencyList.item(i).getAttributes();
				final String currencyCode = getNodeValue(attr, "code");
				final String currencyName = getNodeValue(attr, "name");
				final String countryName = getNodeValue(attr, "country");
				final String flag = getNodeValue(attr, "flag");
				

				// Construct Country object
				CurrencyDetails country = new CurrencyDetails(currencyCode, currencyName,
						countryName, flag + FILE_EXTENSION);
				
				// Add to list
				this.list.add(country);
				
				// Creat Map countrname-abbrev
				this.map.put(currencyCode, flag);
				Log.d(tag, country.toString());
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
}
