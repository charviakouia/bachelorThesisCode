package ivansCode.utils;

public enum JavaLiteralType {

    INTEGER("Integer"),
    BOOLEAN("Boolean"),
    STRING("String"),
    DOUBLE("Double"),
    LONG("Long"),
    NULL("null"),
    CHAR("Character");

    private String stringValue;

    JavaLiteralType(String stringValue){
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

}
