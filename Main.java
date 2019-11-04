package com.selab;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            var server = new ServerManager(4107);
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
