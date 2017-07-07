package com.github.eciuca.tools.nexuscleaner.xml;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.function.Function;

public class ParseUtil {
    public static String extractLatestVersion(InputStream xmlInput) {
        return parseXmlInput(xmlInput, SaxHandler::latestVersion);
    }

    public static String extractLatestVersion(String xmlInput) {
        return parseXmlInput(xmlInput, SaxHandler::latestVersion);
    }

    public static List<String> extractVersions(InputStream xmlInput) {
        return parseXmlInput(xmlInput, SaxHandler::getVersions);
    }

    public static List<String> extractVersions(String xmlInput) {
        return parseXmlInput(xmlInput, SaxHandler::getVersions);
    }

    private static <R> R parseXmlInput(Object xmlInput, Function<SaxHandler, R> handlerMethod) {
        try {
            SaxHandler handler = new SaxHandler();

            if (xmlInput instanceof InputStream) {
                newSaxParser().parse((InputStream) xmlInput, handler);
            } else if (xmlInput instanceof String) {
                newSaxParser().parse(new InputSource(new StringReader((String) xmlInput)), handler);
            }

            return handlerMethod.apply(handler);

        } catch (IOException ex) {
            throw new RuntimeException("SAX parser could not open stream: " + ex.getMessage(), ex);
        } catch (SAXException e) {
            throw new RuntimeException("Could not create SAX parser: " + e.getMessage(), e);
        }
    }

    private static SAXParser newSaxParser() {
        try {
            return SAXParserFactory.newInstance().newSAXParser();
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException("Could not create SAX parser: " + e.getMessage(), e);
        }
    }
}
