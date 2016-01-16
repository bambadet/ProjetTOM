
import java.io.IOException;
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
public class enginetest {

    public static void main(String[] args) throws IOException {

        final NioEngine engine1 = new NioEngine(52001);

        new Thread(new Runnable() {
            @Override
            public void run() {
                engine1.mainloop();
            }
        }).start();
        

    }

}
