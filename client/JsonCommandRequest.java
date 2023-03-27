package client;

public class JsonCommandRequest {
    String type;
    String key;
    String value;

    public JsonCommandRequest(String type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }
}
