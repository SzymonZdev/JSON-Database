package server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    static JsonObject db = new JsonObject();
    static boolean run;
    static ServerSocket server = null;

    static ExecutorService service = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        run = true;

        try {
            System.out.println("Server started!");
            server = new ServerSocket(23456);
            server.setReuseAddress(true);
            do {
                Socket client = server.accept();
                ClientHandler clientSock = new ClientHandler(client);
                service.execute(clientSock);
                service.awaitTermination(100, TimeUnit.MILLISECONDS);
            } while (run);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (server != null) {
                try {
                    server.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        Gson gson = new Gson();
        public ClientHandler(Socket socket)
        {
            this.clientSocket = socket;
        }

        public void run()
        {
            ObjectInputStream objectInputStream = null;
            ObjectOutputStream objectOutputStream =  null;
            try {
                objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

                Invoker invoker = new Invoker();
                String reply = "";
                String requestString = (String) objectInputStream.readObject();
                Request request = gson.fromJson(requestString, Request.class);

                switch (request.type) {
                    case "get" -> {
                        Receiver receiver = new Receiver(request.key);
                        if (invoker.executeOperation(new GetCommand(receiver))) {
                            if (receiver.strValue == null) {
                                reply = gson.toJson(new Response("OK", null, receiver.getValue()));
                            } else {
                                reply = gson.toJson(new Response("OK", null, receiver.strValue));
                            }
                        } else {
                            reply = gson.toJson(new Response("ERROR", "No such key"), Response.class);
                        }

                    }
                    case "set" -> {
                        if (invoker.executeOperation(new SetCommand(new Receiver(request.key, request.value)))) {
                            reply = gson.toJson(new Response("OK"), Response.class);
                        }
                    }
                    case "delete" -> {
                        if (invoker.executeOperation(new DeleteCommand(new Receiver(request.key)))) {
                            reply = gson.toJson(new Response("OK"), Response.class);
                        } else {
                            reply = gson.toJson(new Response("ERROR", "No such key"), Response.class);
                        }
                    }
                    case "exit" -> {
                        reply = gson.toJson(new Response("OK"), Response.class);
                        objectOutputStream.writeObject(reply);
                        run = false;
                    }
                }
                objectOutputStream.writeObject(reply);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (objectOutputStream != null) {
                        objectOutputStream.close();
                    }
                    if (objectInputStream != null) {
                        objectInputStream.close();
                        clientSocket.close();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        static class Response {
            String response;
            String reason;
            Object value;


            public Response(String response, String reason, JsonObject value) {
                this.response = response;
                this.reason = reason;
                this.value = value;
            }

            public Response(String response, String reason, String strValue) {
                this.response = response;
                this.reason = reason;
                this.value = strValue;
            }

            public Response(String response, String reason) {
                this.response = response;
                this.reason = reason;
            }
            public Response(String response) {
                this.response = response;
            }

        }

        static class Request {
            String type;
            String[] key;
            JsonElement value;

            public Request(String type, String[] key, JsonElement value) {
                this.type = type;
                this.key = key;
                this.value = value;
            }
        }
    }
}

