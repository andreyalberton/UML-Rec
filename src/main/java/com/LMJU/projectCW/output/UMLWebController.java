package com.LMJU.projectCW.output;

import com.LMJU.projectCW.analysis.OllamaAdapter;
import com.LMJU.projectCW.analysis.SourceCodeParser;
import com.LMJU.projectCW.metamodel.Sys;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/uml")
public class UMLWebController {

    private final OllamaAdapter aiAdapter = new OllamaAdapter();
    private final SourceCodeParser parser = new SourceCodeParser();

    /**
     * Stateless endpoint: Analyses code and returns SVG
     */
    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateUMLFromFile(@RequestParam("file") MultipartFile file) {
        try {
            String sourceCode = new String(file.getBytes(), StandardCharsets.UTF_8);
            String originalFileName = file.getOriginalFilename().replace(".java", "");

            // Initialise
            Sys currentSys = new Sys(originalFileName);
            String jsonOutput = aiAdapter.analyzeCode(sourceCode);

            if (jsonOutput != null && !jsonOutput.isEmpty()) {
                parser.parseAndAddToSystem(jsonOutput, currentSys);
            }

            // Generate SVG using the Mermaid API
            byte[] svgData = MermaidGenerator.generateSvgBytes(currentSys);

            if (svgData == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            // Prepare the vector file for browser download
            String downloadName = originalFileName + "_diagram.svg";

            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("image/svg+xml"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadName + "\"")
                    .body(svgData);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}