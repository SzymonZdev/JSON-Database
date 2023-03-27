package client;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serializable;

public class JsonRequest implements Serializable {
    String type;
    String[] key;
    JsonElement value;

    public JsonRequest(String type, String[] key, JsonObject value) {
        if (type.equals("exit")) {
            this.type = type;
            this.key = null;
            this.value = null;
        } else {
            this.type = type;
            this.key = key;
            this.value = value;
        }
    }
}
