package com.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static  String CARTELLA = "documenti";

    public static void main(String[] args) {
        System.out.println("Avvio server sulla porta 3000 ");

        try (ServerSocket serverSocket = new ServerSocket(3000)) {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    Thread thread = new Thread(new GestoreClient(clientSocket, CARTELLA));
                    thread.start();
                } catch (IOException e) {
                    System.err.println("Errore durante l'accettazione della connessione: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Impossibile avviare il server sulla porta 3000: " + e.getMessage());
        }
    }
}
