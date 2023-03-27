package server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Receiver {
    private String[] keys;
    private JsonElement value;
    public String strValue;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public Receiver(String[] keys, JsonElement value) {
        this.keys = keys;
        this.value = value;
    }
    public Receiver(String[] keys) {
        this.keys = keys;
    }

    public JsonObject getValue() {
        return this.value.getAsJsonObject();
    }

    public boolean set() {
        try {
            writeLock.lock();
            if (keys.length == 1) {
                Main.db.add(keys[0], value);
                return true;
            } else {
                JsonObject iterationJson = Main.db.deepCopy();
                Map<String, JsonElement> map = new HashMap<>();
                map.put("newDb", iterationJson);
                for (int i = 0; i < keys.length-1; i++) {
                    map.put(keys[i], iterationJson.get(keys[i]).deepCopy());
                    iterationJson = iterationJson.get(keys[i]).deepCopy().getAsJsonObject();
                }
                map.put(keys[keys.length-1], value);

                for (int i = keys.length-1; i > 0; i--) {
                    int newIndex = i-1;
                    JsonElement updatedValue = map.get(keys[i]);
                    JsonElement valueToUpdate = map.get(keys[newIndex]);
                    valueToUpdate.getAsJsonObject().add(keys[i], updatedValue);
                    map.remove(keys[i]);
                    map.remove(keys[newIndex]);
                    map.put(keys[newIndex], valueToUpdate);
                }
                JsonElement newDb = map.get("newDb");
                newDb.getAsJsonObject().add(keys[0], map.get(keys[0]));
                Main.db = newDb.getAsJsonObject();
            }
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    public boolean get() {
        JsonObject currentJson = Main.db;
        try {
            readLock.lock();
            for (String key : keys) {
                if (currentJson.get(key) == null) {
                    return false;
                }
                if (currentJson.get(key).isJsonObject()) {
                    currentJson = currentJson.get(key).getAsJsonObject();
                    value = currentJson;
                } else {
                    strValue = currentJson.get(key).getAsString();
                }
            }
            return true;
        } finally {
            readLock.unlock();
        }
    }

    public boolean delete() {
        if (get()) {
            JsonObject currentJson = Main.db;
            String[] newKeys;
            if (keys.length == 1) {
                newKeys = keys;
            } else {
                newKeys = new String[keys.length-1];
            }
            try {
                writeLock.lock();
                for (int i = 0; i < keys.length; i++) {
                    // if to check that last key is reached
                    if (i == keys.length-1) {
                        currentJson.remove(keys[i]);
                        value = currentJson;
                    } else {
                        currentJson = currentJson.get(keys[i]).getAsJsonObject();
                        newKeys[i] = keys[i];
                    }
                }
                keys = newKeys;
                value = currentJson;
                return set();
            } finally {
                writeLock.unlock();
            }
        } else {
            return false;
        }
    }
}
