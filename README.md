# UML Recovery Tool

Recovers UML class diagrams from Java source code using a locally hosted AI model. Upload a `.java` file or a `.zip` of a Java project through the web interface, and the tool analyses the code with Mistral 7B (via Ollama), extracts classes, fields, methods, and relationships, and returns an SVG class diagram or an XML export.

Everything runs locally. No source code leaves the user machine.

## Prerequisites

- **Java 25** (OpenJDK) — download from https://jdk.java.net
- **Ollama** — download the desktop app from https://ollama.ai and install it
- **Mistral 7B** — open a terminal and run `ollama pull mistral` (this downloads the model, only needed once)

Maven is included in the project via the `mvnw` wrapper, so you don't need to install it separately.

## How to run

You need two things running at the same time: Ollama (the AI server) and the Spring Boot app.

**Step 1 — Start Ollama**

You can either open the Ollama desktop app (it runs in the background automatically), or open a terminal and run:
```
ollama serve
```
Either way works. Leave it running.

**Step 2 — Start the application**

Open a new terminal window. Navigate to the project folder (the one containing `pom.xml`), then run:
On Mac:
```
./mvnw spring-boot:run
```
On Windows use `mvnw.cmd spring-boot:run` instead.

Wait until you see `Started UMLWebApplication` in the terminal output.

**Step 3 — Use the tool**

Open `http://localhost:8080` in the browser. Drag and drop a `.java` file or a `.zip` project archive and hit Generate.

## API Endpoints

| Method | Endpoint | Input | Output |
|--------|----------|-------|--------|
| POST | `/api/uml/generate` | Single `.java` file | SVG diagram |
| POST | `/api/uml/generate-project` | `.zip` archive | Unified SVG diagram |
| POST | `/api/uml/export-xml` | `.java` or `.zip` | Metamodel XML |

## Project Structure

```
src/main/java/com/LMJU/projectCW/
├── UMLWebApplication.java          # Spring Boot entry point
├── analysis/
│   ├── OllamaAdapter.java          # AI communication + prompt engineering
│   ├── SourceCodeParser.java       # JSON parsing + defensive repair
│   ├── ProjectAnalyzer.java        # Multi-file analysis + relationship resolution
│   └── RelationshipDefinition.java # (unused — see class Javadoc)
├── core/
│   ├── MetamodelFactory.java       # Factory for building metamodel objects
│   └── SyntheticDataGenerator.java # Training data generator
├── metamodel/
│   ├── Sys.java, Pack.java, C.java # Hierarchical metamodel classes
│   ├── Attr.java, Met.java         # Attributes and methods
│   ├── Par.java, Local.java, Ret.java # Method parameters, locals, returns
│   ├── URel.java                   # Relationships between classes
│   ├── AccessModifier.java         # PUBLIC, PRIVATE, PROTECTED, PACKAGE
│   ├── ClassType.java              # CLASS, INTERFACE, ABSTRACT_CLASS, ENUM
│   └── RelationshipType.java       # INHERITANCE, REALIZATION, COMPOSITION, etc.
└── output/
    ├── UMLWebController.java       # REST controller (3 endpoints)
    ├── MermaidGenerator.java       # Metamodel -> Mermaid syntax -> SVG
    └── XmlExporter.java            # Metamodel -> XML serialisation
```

## Running Tests

```
./mvnw test
```

## Tech Stack

- Java 25 + Spring Boot 3.5.7
- Ollama + Mistral 7B (Q4_K_M quantisation)
- Mermaid.ink API for SVG rendering
- Tailwind CSS for the web interface
- Maven for build management
