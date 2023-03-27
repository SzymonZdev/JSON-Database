package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 23456;

    @Parameter(names = {"-t"})
    String type;
    @Parameter(names = {"-k"})
    String key;
    @Parameter(names = {"-v"})
    String value;
    @Parameter(names = {"-in"})
    String file;


    public static void main(String[] args) {
        Main main = new Main();
        Gson gson = new Gson();

        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(args);

        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())
            ){
            System.out.println("Client Started!");
            JsonRequest request = null;
            JsonRequestStr requestStr = null;
            JsonCommandRequest commandRequest = null;
            String jsonRequest = null;

            if (main.file != null) {
                Reader reader = new InputStreamReader(new FileInputStream(System.getProperty("user.dir") + "\\src\\client\\data\\" + main.file));
                //Reader reader = new InputStreamReader(new FileInputStream(System.getProperty("user.dir") + "\\JSON Database\\task\\src\\client\\data\\" + main.file));
                try {
                    request = new Gson().fromJson(reader, JsonRequest.class);
                    System.out.println("Sent: " + new Gson().toJson(request, JsonRequest.class));
                    objectOutputStream.writeObject(gson.toJson(request));
                } catch (JsonSyntaxException e) {
                    //Reader reReader = new InputStreamReader(new FileInputStream(System.getProperty("user.dir") + "\\JSON Database\\task\\src\\client\\data\\" + main.file));
                    Reader reReader = new InputStreamReader(new FileInputStream(System.getProperty("user.dir") + "\\src\\client\\data\\" + main.file));
                    requestStr = new Gson().fromJson(reReader, JsonRequestStr.class);
                    System.out.println("Sent: " + new Gson().toJson(requestStr, JsonRequestStr.class));
                    objectOutputStream.writeObject(gson.toJson(new JsonRequest(requestStr.type, new String[]{requestStr.key}, requestStr.value)));
                }
            } else {
                commandRequest = new JsonCommandRequest(main.type, main.key, main.value);
                jsonRequest = new Gson().toJson(commandRequest, JsonCommandRequest.class);
                System.out.println("Sent: " + jsonRequest);

                Map<String, String> strStrMap = new HashMap<>();
                JsonObject object = new JsonObject();
                if (!main.type.equals("exit")) {
                    strStrMap.put(main.key, main.value);
                    object.addProperty(main.key, main.value);
                } else {
                    strStrMap.put(" ", " ");
                    object.addProperty(" ", " ");
                }

                request = new JsonRequest(main.type, new String[]{main.key}, object);


                objectOutputStream.writeObject(gson.toJson(request));
            }
            System.out.println("Received: " + objectInputStream.readObject());
        } catch(IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

