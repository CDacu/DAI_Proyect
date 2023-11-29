package es.uvigo.esei.dai.hybridserver.dao;

public enum DBType {

    HTML("HTML"),
    XML("XML"),
    XSD("XSD"),
    XSLT("XSLT");

    private final String type;

    DBType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
