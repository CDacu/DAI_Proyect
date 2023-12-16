package es.uvigo.esei.dai.hybridserver.http;

public enum HTTPResourceName {

    HTML("HTML"),
    XML("XML"),
    XSD("XSD"),
    XSLT("XSLT");

    private final String type;

    HTTPResourceName(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
