
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Scanner;
import messages.engine.ConnectCallbackImpl;
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
public class scan {

    public static void main(String[] args) throws IOException {

        final NioEngine engine1 = new NioEngine(52000);
        engine1.connect(InetAddress.getLocalHost(), 52001, new ConnectCallbackImpl());
        engine1.connect(InetAddress.getLocalHost(), 52002, new ConnectCallbackImpl());

        new Thread(new Runnable() {
            @Override
            public void run() {
                engine1.mainloop();
            }
        }).start();
        Thread userInput = new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner scanner = new Scanner(new InputStreamReader(System.in));
                while (true) {
                    System.out.println("Message: ");
                    String input = scanner.nextLine();
                    engine1.sendBroadcast(input);
                }
            }
        });
        userInput.start();

    }

}
