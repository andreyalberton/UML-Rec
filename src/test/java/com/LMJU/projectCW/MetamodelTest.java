package com.LMJU.projectCW;

import com.LMJU.projectCW.metamodel.*;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

class MetamodelTest {

    @Test
    void testStudentSystemHierarchy() {
        // Create the System
        UMLSystem system = new UMLSystem("UniversitySystem");

        // Create Packages
        UMLPackage peoplePack = new UMLPackage("people");
        UMLPackage studentsPack = new UMLPackage("people.students");

        // Structure: people -> students
        peoplePack.getSubPackages().add(studentsPack);
        system.getPackages().add(peoplePack);

        // Create 'Person' Class
        UMLClass personClass = new UMLClass("people.Person", AccessModifier.PUBLIC, ClassType.CLASS);
        UMLAttribute nameAttr = new UMLAttribute("people.Person.name", AccessModifier.PROTECTED);
        personClass.getAttributes().add(nameAttr);
        peoplePack.getClasses().add(personClass);

        // Create 'Student' Class
        UMLClass studentClass = new UMLClass("people.students.Student", AccessModifier.PUBLIC, ClassType.CLASS);

        // Add Inheritance: Student extends Person
        UMLRelationship inheritanceRel = new UMLRelationship(
                "people.Person",
                Collections.singletonList(RelationshipType.INHERITANCE)
        );
        studentClass.getRelationships().add(inheritanceRel);
        studentsPack.getClasses().add(studentClass);


        // Assertions
        assertEquals("UniversitySystem", system.getName());
        assertEquals(1, system.getPackages().size());
        assertEquals("people", system.getPackages().get(0).getName());
        assertEquals("people.students", system.getPackages().get(0).getSubPackages().get(0).getName());

        UMLClass retrievedStudent = system.getPackages().get(0).getSubPackages().get(0).getClasses().get(0);
        assertEquals("people.students.Student", retrievedStudent.getName());
        assertEquals(RelationshipType.INHERITANCE, retrievedStudent.getRelationships().get(0).getTypes().get(0));
    }
}