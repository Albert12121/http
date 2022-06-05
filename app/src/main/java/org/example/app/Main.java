package org.example.app;

import org.example.server.Server;

public class Main {

    public static void main(String[] args) {
        final Server server = new Server();
        server.setPortNumber(7878);
        server.start();
    }

}
