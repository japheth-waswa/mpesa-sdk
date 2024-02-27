package base;

public enum ContentType {
    JSON("application/json");
    
    private String value;

    ContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
