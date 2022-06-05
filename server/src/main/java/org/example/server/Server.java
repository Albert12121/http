package org.example.server;

import com.google.common.primitives.Bytes;
import org.example.server.exception.BadRequestException;
import org.example.server.exception.DeadLineExceedException;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Server {
    public static final byte[] CRLFCRLF = {'\r', '\n', '\r', '\n'};
    private int portNumber = 9999;
    private int readTimeout = 60;
    private int buferSize = 4096;
    private int soTimeout = 30*1000;


    public Server(int portNumber, int readTimeout, int buferSize,int soTimeout) {
        this.portNumber = portNumber;
        this.readTimeout = readTimeout;
        this.buferSize = buferSize;
        this.soTimeout = soTimeout;
    }

    public Server() {
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public void start() {
        try (
                final ServerSocket serverSocket = new ServerSocket(portNumber);
        ) {
            while (true) {
                try (final Socket socket = serverSocket.accept();) {
                    handleClient(socket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket) throws IOException {
        socket.setSoTimeout(soTimeout);
        try (
                socket;
                final OutputStream output = socket.getOutputStream();
                final InputStream input = socket.getInputStream();
        ) {
            //socket.getInputStream().read();
            System.out.println(socket.getInetAddress());
            System.out.println("Enter command");

            final String message = readMessage(input);
            final String response =
                    "HTTP/1.1 200 OK\r\n" +
                            "Connection: close\r\n" +
                            "Content-Length: 8\r\n" +
                            "\r\n" +
                            "JavaTest";
            output.write(response.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private String readMessage(InputStream input) throws IOException {
        final byte[] buffer = new byte[buferSize];
        int offset = 0;
        int lenght = buffer.length;
        final Instant deadLine = Instant.now().plus(readTimeout, ChronoUnit.SECONDS);


        while (true) {
            if (Instant.now().isAfter(deadLine)) {
                throw new DeadLineExceedException();
            }
            final int read = input.read(buffer, offset, lenght);

            offset += read;
            lenght = buffer.length - offset;

            final int headerEndIndex = Bytes.indexOf(buffer, CRLFCRLF);
            if (headerEndIndex != -1) {
                break;
            }
            if (read == -1) {
                throw new BadRequestException("CRLFCRLF not found");
            }
            if (read == 0 || lenght == 0) {
                throw new BadRequestException("Not found");
            }
        }
        return new String(buffer, 0, buffer.length, StandardCharsets.UTF_8).trim();
    }
}
