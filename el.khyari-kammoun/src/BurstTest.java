
import java.io.InputStreamReader;
import java.util.Scanner;
import messages.engine.NioEngine;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Fanny
 */
public class BurstTest implements Runnable {

    NioEngine engine;

    public BurstTest(NioEngine engine) {
        this.engine = engine;
    }

    @Override
    public void run() {
        while (true) {
            
            engine.incrementeTemps();
            engine.sendBroadcast(randomMessage(temps));
            
            // partie TOM : 
            // definir protocol : message -> Checksum, envoi de LENGTH(message)MESSAGECHECKSUM(message)
            // reception de LENGTH(ackmessage)ACKMESSAGECHECKSUM(ackmessage) et stockage d'un ack pour le message
            // objet message, attribut liste peer, boolean deliver
            // liste d'objet message dans le NIOEngine
            
            
            // dans engine : fct deliver qd un message a recu tous les acks
            // envoi des messages délivrés vers un peer central qui compare les ordres pour chacuns
            
            // connexion à un peer vers tous les peers, pendant les broadcasts ? Methode du prof ?
            
            
        }
    }

}
