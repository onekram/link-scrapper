package backend.academy.bot.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class LocalTunnelUtil {
    public static String startLocalTunnel(Integer port) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("lt", "--port", port.toString());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);

                    if (line.contains("your url is:")) {
                        return line.split("your url is: ", 2)[1].trim();
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error while local tunnel processing", e);
        }
        return null;
    }
}
