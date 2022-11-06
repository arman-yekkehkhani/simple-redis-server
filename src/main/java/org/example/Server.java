package org.example;

import org.example.dto.RequestDTO;
import org.example.dto.ResponseDTO;
import org.example.exception.IllegalCommandException;
import org.example.redis.RedisServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static ServerSocket server;
    private static int port = 6789;

    public static void main(String args[]) throws IOException, ClassNotFoundException {

        System.out.println("Simple Redis server is ready!");
        server = new ServerSocket(port);
        while (true) {
            try (Socket socket = server.accept();
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {

                RequestDTO requestDTO = (RequestDTO) ois.readObject();
                System.out.println("Message Received: " + requestDTO.getCommand());

                ResponseDTO responseDTO = process(requestDTO);
                oos.writeObject(responseDTO);

                if (requestDTO.getCommand().equalsIgnoreCase("shutdown"))
                    break;
            }
        }
        System.out.println("Shutting down Redis server");
        server.close();
    }

    private static ResponseDTO process(RequestDTO requestDTO) {
        Object body = null;
        String[] params = requestDTO.getParams();
        try {
            switch (requestDTO.getCommand().toUpperCase()) {
                case "SELECT":
                    body = handleSelect(params);
                    break;
                case "SET":
                    body = handleSet(params);
                    break;
                case "EXPIRE":
                    RedisServer.getInstance().setExpire(params[0], Integer.parseInt(params[1]));
                    break;
                case "GET":
                    body = RedisServer.getInstance().get(params[0]);
                    break;
                case "DEL":
                    body = RedisServer.getInstance().delete(params);
                    break;
                case "EXISTS":
                    body = RedisServer.getInstance().countExistingKeys(params);
                    break;
                case "TTL":
                    body = RedisServer.getInstance().remainingTime(params[0]);
                    break;
                case "INCR":
                    RedisServer.getInstance().increment(params[0]);
                    break;
                case "INCRBY":
                    RedisServer.getInstance().incrementBy(params[0], Long.parseLong(params[1]));
                    break;
                case "GETEX":
                    body = handleGetEx(params);
                    break;
                case "GETDEL":
                    body = RedisServer.getInstance().get(params[0]);
                    if (body != null)
                        RedisServer.getInstance().delete(params[0]);
                    break;
                case "KEYS":
                    body = RedisServer.getInstance().getKeys(params[0]);
                    break;
                default:
                    throw new IllegalCommandException(String.format("No such command found %s", requestDTO.getCommand()));
            }
            return new ResponseDTO("success", body);
        } catch (Exception e) {
            return new ResponseDTO("failure", e.getMessage());
        }
    }

    private static Object handleGetEx(String[] params) {
        Object body;
        body = RedisServer.getInstance().get(params[0]);
        if (params.length == 3 && body != null)
            RedisServer.getInstance().setExpire(params[0], Integer.parseInt(params[2]));
        return body;
    }

    private static Object handleSet(String[] params) {
        Object body;
        if (params.length != 2) {
            throw new IllegalArgumentException(String.format("ERROR: SET command accepts exactly two input params(%d provided).", params.length));
        }
        Object previousValue = RedisServer.getInstance().put(params[0], params[1]);
        if (previousValue == null)
            body = String.format("Inserted new mapping: (%s-%s)", params[0], params[1]);
        else
            body = String.format("Updated an existing mapping: (%s-%s)", params[0], previousValue);
        return body;
    }

    private static Object handleSelect(String[] params) {
        Object body;
        int idx = RedisServer.getInstance().setDbIdx(Integer.parseInt(params[0]));
        body = "Current DB: " + idx;
        return body;
    }
}
