package client;

import com.google.gson.JsonObject;

public class JsonRequestStr {
    String type;
    String key;
    JsonObject value;

    public JsonRequestStr(String type, String key, JsonObject value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }
}
