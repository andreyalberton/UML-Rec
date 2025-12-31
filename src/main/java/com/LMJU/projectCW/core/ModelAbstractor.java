package com.LMJU.projectCW.core;

import com.LMJU.projectCW.analysis.SourceCodeParser;
import com.LMJU.projectCW.ai.AIAdapter;
import com.LMJU.projectCW.metamodel.Sys;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


public class ModelAbstractor {

    private final AIAdapter aiAdapter;
    private final SourceCodeParser parser;

    public ModelAbstractor() {
        this.aiAdapter = new AIAdapter();
        this.parser = new SourceCodeParser();
    }


    public List<Sys> generateSystemsFromDirectory(String datasetRootPath) {
        List<Sys> generatedSystems = new ArrayList<>();
        File rootDir = new File(datasetRootPath);

        if (!rootDir.exists() || !rootDir.isDirectory()) {
            System.err.println("not valid dataset directory: " + datasetRootPath);
            return generatedSystems;
        }

        // Identify projects (folders inside the root)
        File[] projectFolders = rootDir.listFiles(File::isDirectory);

        if (projectFolders == null || projectFolders.length == 0) {
            System.out.println("No projects found in " + datasetRootPath);
            return generatedSystems;
        }

        System.out.println("Found " + projectFolders.length + " projects to process:");

        // Iterate and Abstract projects
        for (File projectFolder : projectFolders) {
            System.out.println("Processing: " + projectFolder.getName());

            // Create the System Container
            Sys projectSys = MetamodelFactory.createSys(projectFolder.getName());

            // Process Java files in this folder
            int fileCount = processJavaFilesRecursively(projectFolder, projectSys);

            System.out.println("    Finished " + projectFolder.getName() + " (" + fileCount + " files parsed)");
            generatedSystems.add(projectSys);
        }

        return generatedSystems;
    }

    /**
     * Helper to walk through the file tree and send code to AI.
     */
    private int processJavaFilesRecursively(File folder, Sys currentSys) {
        final int[] count = {0};

        try (Stream<Path> paths = Files.walk(folder.toPath())) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            // Read the Raw Source Code
                            String sourceCode = Files.readString(path);

                            // Send to AI for Analysis
                            // Abstracting UML class diagram elements
                            String jsonOutput = aiAdapter.analyzeCode(sourceCode);

                            if (jsonOutput != null && !jsonOutput.isEmpty()) {
                                // C. Parse JSON into Metamodel Objects
                                parser.parseAndAddToSystem(jsonOutput, currentSys);
                                count[0]++;
                                System.out.print("."); // Progress indicator
                            }

                        } catch (IOException e) {
                            System.err.println("Failed to read file: " + path);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();
        return count[0];
    }
}
