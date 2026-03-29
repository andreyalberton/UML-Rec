package com.LMJU.projectCW;

import com.LMJU.projectCW.metamodel.*;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

class MetamodelTest {

    @Test
    void testStudentSystemHierarchy() {
        // 1= Create System (Sys)
        Sys sys = new Sys("UniversitySystem");
//o
        // 2= Create Packages (Pack)
        Pack peoplePack = new Pack("people");
        Pack studentsPack = new Pack("people.students");

        peoplePack.getPacks().add(studentsPack);
        sys.getPacks().add(peoplePack);

        // 3= Create "Person" Class (C)
        C personClass = new C("people.Person", AccessModifier.PUBLIC, ClassType.CLASS);
        Attr nameAttr = new Attr("people.Person.name", AccessModifier.PROTECTED);
        personClass.getAttrs().add(nameAttr);
        peoplePack.getClasses().add(personClass);

        // 4= Create "Student" Class (C)
        C studentClass = new C("people.students.Student", AccessModifier.PUBLIC, ClassType.CLASS);

        // 5= Add Inheritance Relationship (URel)
        URel inheritanceRel = new URel(
                "people.Person",
                Collections.singletonList(RelationshipType.INHERITANCE)
        );
        studentClass.getURels().add(inheritanceRel);

        studentsPack.getClasses().add(studentClass);

        // Assertions
        assertEquals("UniversitySystem", sys.getName());
        assertEquals("people", sys.getPacks().get(0).getName());
        assertEquals("people.students.Student",
                sys.getPacks().get(0).getPacks().get(0).getClasses().get(0).getName()
        );
    }
}