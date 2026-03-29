package com.LMJU.projectCW.output;

import com.LMJU.projectCW.metamodel.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class MermaidGenerator {

    /**
     * Generates Mermaid syntax and fetches the SVG from the API.
     * the files are not saved on disk.
     */
    public static byte[] generateSvgBytes(Sys system) {
        StringBuilder mermaid = new StringBuilder("classDiagram\n");

        for (Pack p : system.getPacks()) {
            for (C c : p.getClasses()) {
                // Draw Class
                mermaid.append("    class ").append(c.getName()).append(" {\n");

                // Draw Fields
                for (Attr a : c.getAttrs()) {
                    String symbol = a.getAccess().toString().equals("PRIVATE") ? "-" : "+";
                    mermaid.append("        ").append(symbol).append(a.getCName().get(0))
                            .append(" ").append(a.getName()).append("\n");
                }

                // Draw Methods
                for (Met m : c.getMets()) {
                    String symbol = m.getAccess().toString().equals("PRIVATE") ? "-" : "+";
                    mermaid.append("        ").append(symbol).append(m.getName()).append("()\n");
                }
                mermaid.append("    }\n");
            }
        }

        return fetchSvgFromApi(mermaid.toString());
    }

    private static byte[] fetchSvgFromApi(String mermaidCode) {
        try {
            // syntax for the URL
            String encoded = Base64.getUrlEncoder().encodeToString(mermaidCode.getBytes());

            // Switch to /svg/ for vector output
            String apiUrl = "https://mermaid.ink/svg/" + encoded;

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                try (InputStream in = conn.getInputStream();
                     ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    return out.toByteArray();
                }
            }
        } catch (Exception e) {
            System.err.println("API Error: " + e.getMessage());
        }
        return null;
    }
}