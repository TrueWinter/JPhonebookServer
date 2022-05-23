package dev.truewinter.jphonebookserver;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

public class XMLDocument {
    private XMLDocumentType type;
    private XPath xPath;
    private Document document;
    private Element root;
    private boolean isRoot;
    private boolean rootTitleSet;
    private String urlRoot;

    public XMLDocument(XMLDocumentType type, boolean isRoot, String urlRoot) throws ParserConfigurationException {
        this.type = type;
        this.isRoot = isRoot;
        this.urlRoot = urlRoot;

        this.xPath = XPathFactory.newInstance().newXPath();
        this.document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        this.root = document.createElement(type.toString());

        if (!XMLDocumentType.MENU.equals(type)) {
            isRoot = false;
        }

        if (isRoot) {
            Element titleElem = document.createElement("Title");
            titleElem.setTextContent("Root Menu");
            root.appendChild(titleElem);

            rootTitleSet = true;
        }
    }

    public void addDirectoryReference(Directory directory) {
        if (!isRoot && !rootTitleSet) {
            Element titleElem = document.createElement("Title");
            titleElem.setTextContent(directory.getName());
            root.appendChild(titleElem);
        }

        addMenuItem(directory.getName(), urlRoot + directory.getName() + "_Menu.xml");
    }

    public void addDirectoryContactsLink(Directory directory) {
        if (!isRoot && !rootTitleSet) {
            Element titleElem = document.createElement("Title");
            titleElem.setTextContent(directory.getName());
            root.appendChild(titleElem);
        }

        addMenuItem(directory.getName(), urlRoot + directory.getName() + "_Contacts.xml");
    }

    public void addContactsFromDirectory(Directory directory) throws Exception {
        if (isRoot || XMLDocumentType.MENU.equals(this.type)) {
            return;
        }

        Element titleElem = document.createElement("Title");
        titleElem.setTextContent(directory.getName());
        root.appendChild(titleElem);

        Element promptElem = document.createElement("Prompt");
        promptElem.setTextContent(directory.getName());
        root.appendChild(promptElem);

        Database database = Database.getInstance();
        List<Contact> contacts = database.getAllContactsInDirectory(directory.getId());

        for (Contact contact : contacts) {
            addDirectoryEntry(contact);
        }
    }

    private void addMenuItem(String name, String url) {
        Element menuItem = document.createElement("MenuItem");

        Element menuName = document.createElement("Name");
        menuName.setTextContent(name);

        Element menuUrl = document.createElement("URL");
        menuUrl.setTextContent(url);

        menuItem.appendChild(menuName);
        menuItem.appendChild(menuUrl);

        root.appendChild(menuItem);
    }

    public void addDirectoryEntry(Contact contact) {
        Element directoryEntry = document.createElement("DirectoryEntry");

        Element name = document.createElement("Name");
        name.setTextContent(contact.getName());
        directoryEntry.appendChild(name);

        if (!contact.getTelephone().isBlank()) {
            Element telephone = document.createElement("Telephone");
            telephone.setTextContent(contact.getTelephone());
            directoryEntry.appendChild(telephone);
        }

        if (!contact.getMobile().isBlank()) {
            Element mobile = document.createElement("Mobile");
            mobile.setTextContent(contact.getMobile());
            directoryEntry.appendChild(mobile);
        }

        if (!contact.getOther().isBlank()) {
            Element other = document.createElement("Other");
            other.setTextContent(contact.getOther());
            directoryEntry.appendChild(other);
        }

        Element ring = document.createElement("Ring");
        ring.setTextContent(String.valueOf(contact.getRing()));
        directoryEntry.appendChild(ring);

        if (!contact.getGroupName().isBlank()) {
            Element group = document.createElement("Group");
            group.setTextContent(contact.getGroupName());
            directoryEntry.appendChild(group);
        }

        root.appendChild(directoryEntry);
    }

    public void build() {
        document.appendChild(root);
    }

    // https://stackoverflow.com/questions/31029735/set-xml-encoding
    public String toString() {
        DOMImplementationLS domImplementationLS =
                (DOMImplementationLS) document.getImplementation();
        LSSerializer lsSerializer =
                domImplementationLS.createLSSerializer();
        LSOutput lsOutput = domImplementationLS.createLSOutput();
        lsOutput.setEncoding("UTF-8");
        Writer stringWriter = new StringWriter();
        lsOutput.setCharacterStream(stringWriter);
        lsSerializer.write(document, lsOutput);
        return stringWriter.toString();
    }

    public enum XMLDocumentType {
        MENU("FanvilIPPhoneMenu"),
        DIRECTORY("FanvilIPPhoneDirectory");

        private String type;

        XMLDocumentType(String root) {
            this.type = root;
        }

        public boolean equalsType(String type) {
            return this.type.equals(type);
        }

        public String toString() {
            return this.type;
        }

        public static XMLDocumentType fromString(String type) {
            for (XMLDocumentType t : XMLDocumentType.values()) {
                if (t.type.equalsIgnoreCase(type)) {
                    return t;
                }
            }

            return null;
        }
    }
}
