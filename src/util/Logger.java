package util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {	

	private static void printWithLevel(String level, String content) {
		System.out.println(
				String.format("[%s] |%s| - %s", new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date()), level, content)
		);
	}
	

	public static void info(String content) {
		printWithLevel("INFO", content);
	}

	public static void error(String content) {
		printWithLevel("ERROR", content);
	}
}

