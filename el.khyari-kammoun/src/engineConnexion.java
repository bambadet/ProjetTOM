
import java.io.IOException;
import java.net.InetAddress;
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
public class engineConnexion {

    public static void main(String[] args) throws IOException {

        final NioEngine engineConnexion = new NioEngine(52004);
        engineConnexion.connect(InetAddress.getLocalHost(), 52001, new ConnectCallbackImpl());

        engineConnexion.mainloop();

    }
}
