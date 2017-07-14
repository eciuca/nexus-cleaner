package com.github.eciuca.tools.nexuscleaner.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SaxHandler extends DefaultHandler {

    public static final String LATEST = "latest";

    public static final String VERSION = "version";

    private String latestVersion;

    private List<String> versions = new LinkedList<>();

    private boolean inLatestElement;

    private boolean inVersionElement;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (qName) {
            case LATEST:
                inLatestElement = true;
                break;
            case VERSION:
                inVersionElement = true;
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case LATEST:
                inLatestElement = false;
                break;
            case VERSION:
                inVersionElement = false;
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (inLatestElement) {
            latestVersion = new String(ch, start, length).trim();
        } else if (inVersionElement) {
            versions.add(new String(ch, start, length).trim());
        }
    }

    @Override
    public void endDocument() throws SAXException {
        Collections.reverse(versions);
    }

    public String latestVersion() {
        return latestVersion;
    }

    public List<String> getVersions() {
        return versions;
    }

}
