package com.example;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class GestoreClient implements Runnable {
    private final Socket clientSocket;
    private final String cartella;

    private static final String[] ESTENSIONI_AMMESSE = { "txt", "md", "csv" };

    public GestoreClient(Socket clientSocket, String cartella) {
        this.clientSocket = clientSocket;
        this.cartella = cartella;
    }

    @Override
    public void run() {
        try (Socket socket = this.clientSocket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String richiesta = in.readLine();

            if (richiesta == null) {
                return;
            }

            System.out.println("Richiesta ricevuta: [" + richiesta + "]");
            elaboraRichiesta(richiesta, out);

        } catch (IOException e) {
            System.err.println("Errore di comunicazione con il client: " + e.getMessage());
        }
    }

    private void elaboraRichiesta(String richiesta, PrintWriter out) {
        String pulita = richiesta.trim();

        if (pulita.isEmpty()) {
            inviaErrore(out, "ERR SYNTAX");
            return;
        }

        String[] parti = pulita.split("\\s+");

        if (!"LEGGI".equals(parti[0])) {
            inviaErrore(out, "ERR UNKNOWNCMD");
            return;
        }

        if (parti.length != 2 || !nomeValido(parti[1])) {
            inviaErrore(out, "ERR SYNTAX");
            return;
        }

        String nomeBase = parti[1];
        File fileTrovato = null;
        String estensioneTrovata = null;

        for (String ext : ESTENSIONI_AMMESSE) {
            File candidato = new File(cartella, nomeBase + "." + ext);
            if (candidato.isFile()) {
                fileTrovato = candidato;
                estensioneTrovata = ext;
                break; // Si ferma al primo file esistente
            }
        }

        if (fileTrovato == null) {
            inviaErrore(out, "ERR NOTFOUND");
            return;
        }

        boolean statoInviato = false;
        try (BufferedReader fileReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileTrovato), StandardCharsets.UTF_8))) {

            out.println("OK FILE");
            out.println("TYPE " + estensioneTrovata);
            out.println();
            statoInviato = true;

            String riga;
            while ((riga = fileReader.readLine()) != null) {
                out.println(riga);
            }

        } catch (IOException e) {
            System.err.println("Errore durante l'accesso al file " + fileTrovato.getName() + ": " + e.getMessage());
            if (!statoInviato) {
                inviaErrore(out, "ERR READ");
            }
        }
    }

    private boolean nomeValido(String nome) {
        if (nome.isEmpty() || nome.contains(".") || nome.contains("/") || nome.contains("\\")) {
            return false;
        }
        return true;
    }

    private void inviaErrore(PrintWriter out, String codiceErrore) {
        out.println(codiceErrore);
        out.println();
    }
}