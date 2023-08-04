package kz.greetgo.sandboxserver.migration.xml_parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

public abstract class SaxHandler extends DefaultHandler {

    private final List<String> pathList = new ArrayList<>();

    protected String path() {
        return "/" + String.join("/", pathList);
    }

    private StringBuilder currentValue = null;

    protected String text() {
        StringBuilder x = currentValue;
        return x == null ? "" : x.toString();
    }

    protected abstract void startTag(Attributes attributes) throws Exception;

    protected abstract void endTag() throws Exception;

    @Override
    public final void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        pathList.add(qName);
        currentValue = null;
        try {
            startTag(attributes);
        } catch (SAXException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("w55V70avQC :: ", e);
        }
    }

    @Override
    public final void characters(char[] ch, int start, int length) {
        StringBuilder x = currentValue;
        if (x == null) x = currentValue = new StringBuilder();
        String text = new String(ch, start, length).trim();
        x.append(text);
    }

    @Override
    public final void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            endTag();
        } catch (SAXException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("323U71Sht0 :: ", e);
        }
        pathList.remove(pathList.size() - 1);
        currentValue = null;
    }
}
