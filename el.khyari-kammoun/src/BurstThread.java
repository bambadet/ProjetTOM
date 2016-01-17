
import java.io.InputStreamReader;
import java.util.Random;
import java.util.Scanner;
import messages.engine.NioEngine;
import messages.message.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Fanny
 */
public class BurstThread implements Runnable {

    NioEngine engine;

    public BurstThread(NioEngine engine) {
        this.engine = engine;
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void run() {
        // longueur du message aléatoire
        Random random_taille = new Random(5);
        // générateur d'entier pour choisir le caractère aléatoire
        Random random = new Random(4);

        while (true) {

            ChatMessage chat = new ChatMessage(1, "bonjour".getBytes());
            engine.sendBroadcast(chat);

            // ralentir l'émission du burst thread
            long start = System.nanoTime();
            while ((System.nanoTime() - start) < 600000000) {
            }

        }
    }

}
