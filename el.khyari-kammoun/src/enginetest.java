
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
public class enginetest {

    public static void main(String[] args) throws IOException {

        final NioEngine engine1 = new NioEngine(52001);
        engine1.connect(InetAddress.getLocalHost(), 52002, new ConnectCallbackImpl());
        engine1.connect(InetAddress.getLocalHost(), 52003, new ConnectCallbackImpl());

        new Thread(new Runnable() {
            @Override
            public void run() {
                engine1.mainloop();
            }
        }).start();

        Thread user = new Thread(new BurstThread(engine1));
        user.start();
    }

}
