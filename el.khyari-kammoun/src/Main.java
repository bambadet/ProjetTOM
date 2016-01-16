import messages.engine.ConnectCallbackImpl;
import messages.engine.NioEngine;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws IOException {

		final NioEngine engine1 = new NioEngine(52000);
		final NioEngine engine2 = new NioEngine(52001);
		final NioEngine engine3 = new NioEngine(52002);

		engine1.connect(InetAddress.getLocalHost(), 52001, new ConnectCallbackImpl());
		engine1.connect(InetAddress.getLocalHost(), 52002, new ConnectCallbackImpl());
		engine2.connect(InetAddress.getLocalHost(), 52002, new ConnectCallbackImpl());

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

		new Thread(new Runnable() {
			@Override
			public void run() {
				engine1.mainloop();
			}
		}).start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				engine2.mainloop();
			}
		}).start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				engine3.mainloop();
			}
		}).start();
	}
}
