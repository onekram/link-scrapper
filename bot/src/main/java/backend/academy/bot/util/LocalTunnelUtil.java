package backend.academy.bot.util;

import lombok.experimental.UtilityClass;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@UtilityClass
public class LocalTunnelUtil {
    public static String startLocalTunnel(Integer port) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("lt", "--port", port.toString());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);

                if (line.contains("your url is:")) {
                    return line.split("your url is: ")[1].trim();
                }
            }
        } catch (IOException _) {
        }
        return null;
    }
}
