
import java.io.IOException;
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
public class enginetest3 {

    public static void main(String[] args) throws IOException {

        final NioEngine engine3 = new NioEngine(52003);

        engine3.mainloop();


    }


}
