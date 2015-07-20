package eu.over9000.veya.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import eu.over9000.veya.Veya;
import eu.over9000.veya.world.BlockType;

public class Console {

	public static final Map<String, Consumer<List<String>>> commands = new HashMap<>();
	private static final AtomicBoolean alive = new AtomicBoolean(false);
	private static BufferedReader reader;

	private static final Thread consoleThread = new Thread(() -> {
		while (alive.get()) {
			try {
				while (!reader.ready()) {
					Thread.sleep(100);
				}
				final String input = reader.readLine();
				if (input != null) {
					final List<String> inputSplitted = new ArrayList<>(Arrays.asList(input.split(" ")));
					final String command = inputSplitted.remove(0);
					final Consumer<List<String>> handler = commands.get(command);
					if (handler != null) {
						handler.accept(inputSplitted);
					}
				}
			} catch (Exception e) {
				if (!alive.get()) {
					return;
				} else {
					e.printStackTrace();
				}
			}
		}
	}, "consoleThread");

	public static void start() {
		reader = new BufferedReader(new InputStreamReader(System.in));
		alive.set(true);
		consoleThread.start();
	}

	public static void stop() {
		try {
			alive.set(false);
			reader.close();
			consoleThread.interrupt();
			consoleThread.join();
		} catch (final InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	static {
		commands.put("block", args -> {
			BlockType type = BlockType.TEST;
			
			if (args.size() != 1) {
				return;
			}

			try {
				type = BlockType.valueOf(args.get(0).toUpperCase());
			} catch (IllegalArgumentException e) {
				try {
					int index = Integer.parseInt(args.get(0));
					type = BlockType.values()[index];
				} catch (final ArrayIndexOutOfBoundsException | NumberFormatException e1) {
					System.out.println("could not parse input '" + args.get(0) + "' to block");
				}
			}

			Veya.scene.setBlock(type);
			System.out.println("set place block type to " + type);
		});
		commands.put("collision", args -> {
			if (!args.isEmpty()) {
				return;
			}

			Veya.collisionSwitch = !Veya.collisionSwitch;

			System.out.println("collision changed to " + Veya.collisionSwitch);
		});
		commands.put("gravity", args -> {
			if (!args.isEmpty()) {
				return;
			}

			Veya.gravitySwitch = !Veya.gravitySwitch;

			System.out.println("gravity changed to " + Veya.gravitySwitch);
		});
	}
}
