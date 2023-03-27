package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;

public class JsonDatabase {
    JsonObject db;

    public JsonDatabase() {
        this.db = init();
    }

    private JsonObject init() {
        JsonObject dbFromFile = new JsonObject();

        try (Reader reader = new InputStreamReader(new FileInputStream(System.getProperty("user.dir") + "\\src\\server\\data\\data.json")))
        {
            dbFromFile = new Gson().toJsonTree(reader).getAsJsonObject();
        } catch (IOException e) {
            return dbFromFile;
        }
        return dbFromFile;
    }
}
