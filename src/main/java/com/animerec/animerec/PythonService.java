package com.animerec.animerec;

import org.springframework.stereotype.Service;
import java.io.*;

/**
 * Python service to run the python script.
 * 
 */
@Service
public class PythonService {

    public String runPythonScript(String url, String flag) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("python3", "src/main/resources/scripts/userScrape.py", url, flag);
        Process p = pb.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        StringBuilder output = new StringBuilder();
        while ((line = in.readLine()) != null) {
            output.append(line);
        }
        return output.toString();
    }
}