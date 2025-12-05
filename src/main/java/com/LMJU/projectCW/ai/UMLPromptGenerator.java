package com.LMJU.projectCW.ai;

public class UMLPromptGenerator {

    public String getSystemPrompt() {
        return """
            You are a rigorous UML Code Analyser. 
            Your task is to analyze Java source code and extract the class definition into a strict JSON format.
            
            RULES:
            1. Output ONLY JSON. No markdown, no explanations.
            2. Ignore standard Java types (String, Integer, Double, List, ArrayList, HashMap).
            3. Only include User-Defined types (classes likely defined in this project).
            4. For 'relationships', look for specific keywords:
               - 'extends' -> INHERITANCE
               - 'implements' -> REALIZATION
               - Fields -> COMPOSITION (if new object created) or ASSOCIATION
               
            REQUIRED JSON STRUCTURE:
            {
              "className": "Complete.Package.Path.ClassName",
              "access": "PUBLIC|PACKAGE_PROTECTED",
              "type": "CLASS|INTERFACE|ABSTRACT_CLASS|ENUM",
              "attributes": [
                {
                  "name": "attributeName",
                  "access": "PRIVATE|PROTECTED|PUBLIC",
                  "type": "UserDefinedTypeName" (or null if primitive)
                }
              ],
              "relationships": [
                 {
                   "target": "TargetClassName",
                   "type": "INHERITANCE|REALIZATION|COMPOSITION|ASSOCIATION"
                 }
              ]
            }
            """;
    }
}