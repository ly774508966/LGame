package loon.opengl;

import loon.BaseIO;
import loon.utils.StringUtils;
import loon.utils.TArray;

public class ShaderUtils {

	private ShaderUtils() {
	}

	public static String process(String codePath) {
		return process(BaseIO.loadText(codePath), new TArray<String>());
	}

	private static String process(String codeText, TArray<String> included) {
		StringBuilder builder = new StringBuilder();
		String[] lines = StringUtils.split(codeText, '\n');
		if (lines != null) {
			for (int line = 0; line < lines.length; line++) {
				if (lines[line].trim().startsWith("//@include once")) {
					String include = lines[line].replace("//@include once", "")
							.trim();

					if (!included.contains(include)) {
						builder.append(process(BaseIO.loadText(include),
								included));

						builder.append("\n#line ").append(line + 1)
								.append("\n");

						included.add(include);
					}
				} else if (lines[line].trim().startsWith("//@include")) {
					String include = lines[line].replace("//@include", "")
							.trim();
					builder.append(process(BaseIO.loadText(include), included));
					builder.append("\n#line ").append(line + 1).append("\n");
					included.add(include);
				} else {
					builder.append(lines[line]).append("\n");
				}
			}
		}
		return builder.toString();
	}
}
