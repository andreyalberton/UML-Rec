package com.LMJU.projectCW.output;

import com.LMJU.projectCW.analysis.OllamaAdapter;
import com.LMJU.projectCW.analysis.ProjectAnalyzer;
import com.LMJU.projectCW.analysis.SourceCodeParser;
import com.LMJU.projectCW.metamodel.Sys;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The REST controller — all three endpoints live here:
 *   POST /api/uml/generate        — single file -> SVG diagram
 *   POST /api/uml/generate-project — ZIP project -> unified SVG diagram
 *   POST /api/uml/export-xml      — single file or ZIP -> metamodel XML
 *
 * Each endpoint follows the same pattern: read the upload, run it
 * through the AI pipeline, build the metamodel, and serialize the
 * result in the requested format.
 */
@RestController
@RequestMapping("/api/uml")
public class UMLWebController {

    private final OllamaAdapter aiAdapter = new OllamaAdapter();
    private final SourceCodeParser parser = new SourceCodeParser();
    private final ProjectAnalyzer projectAnalyzer = new ProjectAnalyzer();

    /**
     * Stateless endpoint: Analyses a single file and returns SVG
     */
    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateUMLFromFile(@RequestParam("file") MultipartFile file) {
        try {
            String sourceCode = new String(file.getBytes(), StandardCharsets.UTF_8);
            String originalFileName = file.getOriginalFilename().replace(".java", "");

            Sys currentSys = new Sys(originalFileName);
            String jsonOutput = aiAdapter.analyzeCode(sourceCode);

            if (jsonOutput != null && !jsonOutput.isEmpty()) {
                parser.parseAndAddToSystem(jsonOutput, currentSys);
            }

            byte[] svgData = MermaidGenerator.generateSvgBytes(currentSys);

            if (svgData == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

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

    /**
     * XML export endpoint: Accepts either a single .java file OR a .zip
     * project and returns the serialized Sys metamodel in the exact schema
     * from Dr. Athanasopoulos's "Metamodel in XML" specification (Doc 5).
     *
     * The SVG endpoints above render the metamodel visually; this endpoint
     * exposes the underlying data contract so the metamodel can be consumed
     * by other tools (renderers, validators, diffs) and can be checked
     * directly against the formal tuples.
     */
    @PostMapping("/export-xml")
    public ResponseEntity<byte[]> exportMetamodelAsXml(@RequestParam("file") MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            if (originalName == null) originalName = "upload";

            Sys sys;
            String downloadBase;

            if (originalName.endsWith(".zip")) {
                // Project mode: reuse the same ZIP-walking logic as the SVG endpoint
                downloadBase = originalName.replace(".zip", "");
                Map<String, String> sourceFiles = extractJavaFilesFromZip(file);
                if (sourceFiles.isEmpty()) {
                    return ResponseEntity.badRequest().build();
                }
                sys = projectAnalyzer.analyzeProject(downloadBase, sourceFiles);
            } else {
                // Single-file mode
                downloadBase = originalName.replace(".java", "");
                String sourceCode = new String(file.getBytes(), StandardCharsets.UTF_8);
                sys = new Sys(downloadBase);
                String jsonOutput = aiAdapter.analyzeCode(sourceCode);
                if (jsonOutput != null && !jsonOutput.isEmpty()) {
                    parser.parseAndAddToSystem(jsonOutput, sys);
                }
            }

            String xml = XmlExporter.toXml(sys);
            byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);
            String downloadName = downloadBase + "_metamodel.xml";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadName + "\"")
                    .body(xmlBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Project endpoint: Accepts a ZIP file containing a Java project,
     * extracts all .java files, analyses them with AI, resolves
     * cross-file relationships, and returns a unified SVG diagram.
     */
    @PostMapping("/generate-project")
    public ResponseEntity<byte[]> generateUMLFromProject(@RequestParam("file") MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            String projectName = originalName != null ? originalName.replace(".zip", "") : "Project";

            // Extract all .java files from ZIP
            Map<String, String> sourceFiles = extractJavaFilesFromZip(file);

            if (sourceFiles.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            System.out.println("[ZIP UPLOAD] Found " + sourceFiles.size() + " Java files in " + projectName);

            // Analyse entire project with cross-file relationship detection
            Sys projectSys = projectAnalyzer.analyzeProject(projectName, sourceFiles);

            // Generate unified SVG diagram
            byte[] svgData = MermaidGenerator.generateSvgBytes(projectSys);

            if (svgData == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            String downloadName = projectName + "_project_diagram.svg";

            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("image/svg+xml"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadName + "\"")
                    .body(svgData);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Shared ZIP-walker used by both generate-project (SVG) and export-xml.
     * Keeps the two endpoints in sync so any filtering rules apply uniformly.
     */
    private Map<String, String> extractJavaFilesFromZip(MultipartFile file) throws java.io.IOException {
        Map<String, String> sourceFiles = new LinkedHashMap<>();
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (entry.isDirectory() || name.startsWith("__MACOSX") || name.startsWith(".")) {
                    zis.closeEntry();
                    continue;
                }
                if (name.endsWith(".java")) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8));
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                    String shortName = name.contains("/") ? name.substring(name.lastIndexOf("/") + 1) : name;
                    sourceFiles.put(shortName, content.toString());
                }
                zis.closeEntry();
            }
        }
        return sourceFiles;
    }
}
