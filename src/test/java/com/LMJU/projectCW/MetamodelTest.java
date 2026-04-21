package com.LMJU.projectCW;

import com.LMJU.projectCW.metamodel.*;
import com.LMJU.projectCW.output.XmlExporter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the metamodel classes.
 *
 * Each test exercises a single piece of behaviour and is named to describe
 * what it is checking — following the multi-method pattern recommended in
 * the module's Testing notes (one public test class with many small,
 * descriptive @Test methods rather than one big catch-all).
 *
 * The tests intentionally do NOT go through Ollama. The goal here is to
 * verify the metamodel shape — the formal tuples from Dr. Athanasopoulos's
 * spec — independently of the AI pipeline, so a regression in the metamodel
 * or in the XML exporter is caught even when Ollama is unavailable.
 */
class MetamodelTest {

    // ------------------------------------------------------------------
    // Sys / Pack / C — structural tests
    // ------------------------------------------------------------------

    @Test
    void testStudentSystemHierarchy() {
        // The canonical example from the spec: a UniversitySystem with
        // a people package and a nested people.students sub-package,
        // with Student inheriting from Person.
        Sys sys = new Sys("UniversitySystem");

        Pack peoplePack = new Pack("people");
        Pack studentsPack = new Pack("people.students");
        peoplePack.getPacks().add(studentsPack);
        sys.getPacks().add(peoplePack);

        C personClass = new C("Person", AccessModifier.PUBLIC, ClassType.CLASS);
        Attr nameAttr = new Attr("name", AccessModifier.PROTECTED);
        personClass.getAttrs().add(nameAttr);
        peoplePack.getClasses().add(personClass);

        C studentClass = new C("Student", AccessModifier.PUBLIC, ClassType.CLASS);
        URel inheritanceRel = new URel(
                "Person",
                Collections.singletonList(RelationshipType.INHERITANCE)
        );
        studentClass.getURels().add(inheritanceRel);
        studentsPack.getClasses().add(studentClass);

        assertEquals("UniversitySystem", sys.getName());
        assertEquals("people", sys.getPacks().get(0).getName());
        assertEquals("Student",
                sys.getPacks().get(0).getPacks().get(0).getClasses().get(0).getName());
    }

    @Test
    void testEmptySystemHasNoPackages() {
        // A freshly created Sys should have an empty (not null) package list.
        // This protects against NPEs in downstream serialisers.
        Sys sys = new Sys("Empty");
        assertNotNull(sys.getPacks(), "packs list must be initialised, not null");
        assertTrue(sys.getPacks().isEmpty(), "a brand new Sys has no packages yet");
    }

    @Test
    void testEmptyClassHasEmptyCollections() {
        // A class with no fields/methods/relations should still expose
        // non-null collections — the formal tuple allows empty sets but
        // the Java representation should never hand back null.
        C c = new C("Marker", AccessModifier.PUBLIC, ClassType.CLASS);
        assertNotNull(c.getAttrs());
        assertNotNull(c.getMets());
        assertNotNull(c.getURels());
        assertEquals(0, c.getAttrs().size());
        assertEquals(0, c.getMets().size());
        assertEquals(0, c.getURels().size());
    }

    @Test
    void testNestedPackagesArePreserved() {
        // Packs should nest recursively — a.b.c must survive three levels deep.
        Sys sys = new Sys("Nested");
        Pack a = new Pack("a");
        Pack b = new Pack("a.b");
        Pack c = new Pack("a.b.c");
        a.getPacks().add(b);
        b.getPacks().add(c);
        sys.getPacks().add(a);

        assertEquals("a.b.c",
                sys.getPacks().get(0).getPacks().get(0).getPacks().get(0).getName());
    }

    // ------------------------------------------------------------------
    // Attribute / Method / Par / Local / Ret tests
    // ------------------------------------------------------------------

    @Test
    void testAttributeStoresAccessAndTypeLists() {
        // Attr must keep cName / dName / inName as separate lists per the spec.
        Attr a = new Attr("balance", AccessModifier.PRIVATE);
        a.getCName().add("Money");
        a.getDName().add("List");
        a.getInName().add("Money()");

        assertEquals(AccessModifier.PRIVATE, a.getAccess());
        assertEquals("Money", a.getCName().get(0));
        assertEquals("List", a.getDName().get(0));
        assertEquals("Money()", a.getInName().get(0));
    }

    @Test
    void testMethodHasParameterLocalAndReturn() {
        // Met must carry pars + locals + a single ret (per the formal tuple).
        Met m = new Met("calculate", AccessModifier.PUBLIC);
        Par p = new Par("input");
        p.getCName().add("Order");
        m.getPars().add(p);

        Local loc = new Local("total");
        loc.getCName().add("Money");
        m.getLocals().add(loc);

        Ret ret = new Ret();
        ret.getCName().add("Money");
        m.setRet(ret);

        assertEquals(1, m.getPars().size());
        assertEquals("Order", m.getPars().get(0).getCName().get(0));
        assertEquals("total", m.getLocals().get(0).getName());
        assertEquals("Money", m.getRet().getCName().get(0));
    }

    @Test
    void testMethodReturnDefaultsToEmptyNotNull() {
        // New methods must come with a non-null empty Ret so XML/UML
        // renderers don't have to special-case void-returning methods.
        Met m = new Met("doStuff", AccessModifier.PUBLIC);
        assertNotNull(m.getRet(), "Ret must be initialised in constructor");
        assertTrue(m.getRet().getCName().isEmpty());
        assertTrue(m.getRet().getDName().isEmpty());
    }

    // ------------------------------------------------------------------
    // Relationship tests
    // ------------------------------------------------------------------

    @Test
    void testRelationshipCanCarryMultipleTypes() {
        // URel's type field is a List — the spec allows a pair of classes
        // to be linked by more than one kind of relationship simultaneously.
        URel rel = new URel("Account", Arrays.asList(
                RelationshipType.ASSOCIATION,
                RelationshipType.DEPENDENCY
        ));
        assertEquals(2, rel.getType().size());
        assertTrue(rel.getType().contains(RelationshipType.ASSOCIATION));
        assertTrue(rel.getType().contains(RelationshipType.DEPENDENCY));
    }

    @Test
    void testInheritanceRelationshipStoresTargetName() {
        URel rel = new URel("Person", Collections.singletonList(RelationshipType.INHERITANCE));
        assertEquals("Person", rel.getCName());
        assertEquals("Person", rel.getTarget(), "target alias must match cName");
        assertEquals(RelationshipType.INHERITANCE, rel.getType().get(0));
    }

    @Test
    void testAllRelationshipTypesAreDistinct() {
        // Safety net against accidental enum collapse — the six types
        // from the spec must all exist and be unique.
        RelationshipType[] all = RelationshipType.values();
        assertEquals(6, all.length, "spec defines exactly 6 relationship types");
    }

    // ------------------------------------------------------------------
    // XmlExporter tests — verify the serialized output matches the
    // <sys><pack><c>… schema from Doc 5 byte-for-byte on key tags.
    // ------------------------------------------------------------------

    @Test
    void testXmlExporterEmitsRootSysTag() {
        Sys sys = new Sys("TinySystem");
        String xml = XmlExporter.toXml(sys);
        assertTrue(xml.contains("<sys name=\"TinySystem\">"),
                "root <sys> tag must carry the system name");
        assertTrue(xml.trim().endsWith("</sys>"),
                "document must close with </sys>");
    }

    @Test
    void testXmlExporterEmitsPackagesAndClasses() {
        Sys sys = new Sys("Demo");
        Pack p = new Pack("com.demo");
        C c = new C("Widget", AccessModifier.PUBLIC, ClassType.CLASS);
        p.getClasses().add(c);
        sys.getPacks().add(p);

        String xml = XmlExporter.toXml(sys);
        assertTrue(xml.contains("<pack name=\"com.demo\">"));
        assertTrue(xml.contains("<c name=\"Widget\""));
        assertTrue(xml.contains("type=\"CLASS\""));
    }

    @Test
    void testXmlExporterEmitsRelationships() {
        Sys sys = new Sys("RelDemo");
        Pack p = new Pack("rel");
        C c = new C("Child", AccessModifier.PUBLIC, ClassType.CLASS);
        c.getURels().add(new URel("Parent",
                Collections.singletonList(RelationshipType.INHERITANCE)));
        p.getClasses().add(c);
        sys.getPacks().add(p);

        String xml = XmlExporter.toXml(sys);
        assertTrue(xml.contains("<uRel cName=\"Parent\" type=\"INHERITANCE\"/>"),
                "relationship must be serialised with cName and type attributes");
    }

    @Test
    void testXmlExporterEscapesSpecialCharacters() {
        // Guard against a class named something silly like "Foo<T>" or
        // a package with an ampersand breaking the XML document.
        Sys sys = new Sys("A & B");
        Pack p = new Pack("pkg<with>chars");
        sys.getPacks().add(p);

        String xml = XmlExporter.toXml(sys);
        assertTrue(xml.contains("A &amp; B"));
        assertTrue(xml.contains("pkg&lt;with&gt;chars"));
        assertFalse(xml.contains("A & B"),
                "raw ampersand must be escaped in the output");
    }
}
