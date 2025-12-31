package com.LMJU.projectCW.output;

import com.LMJU.projectCW.metamodel.*;

public class UMLCodeParser {

    public String parse(Sys system) {
        StringBuilder xml = new StringBuilder();

        // <sys> tag
        xml.append("<sys>\n");
        xml.append("  <name>").append(system.getName()).append("</name>\n");

        // Iterate through packages
        for (Pack p : system.getPacks()) {
            xml.append(parsePackage(p, "  "));
        }

        xml.append("</sys>");
        return xml.toString();
    }

    private String parsePackage(Pack p, String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("<pack>\n");
        sb.append(indent).append("  <name>").append(p.getName()).append("</name>\n");

        // Sub packages
        for (Pack sub : p.getPacks()) {
            sb.append(parsePackage(sub, indent + "  "));
        }

        // Classes
        for (C cls : p.getClasses()) {
            sb.append(parseClass(cls, indent + "  "));
        }

        sb.append(indent).append("</pack>\n");
        return sb.toString();
    }

    private String parseClass(C c, String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("<c>\n");
        sb.append(indent).append("  <name>").append(c.getName()).append("</name>\n");
        sb.append(indent).append("  <access>").append(c.getAccess()).append("</access>\n");
        sb.append(indent).append("  <type>").append(c.getType()).append("</type>\n");

        // Attributes
        for (Attr a : c.getAttrs()) {
            sb.append(indent).append("  <attr>\n");
            sb.append(indent).append("    <name>").append(a.getName()).append("</name>\n");
            sb.append(indent).append("    <access>").append(a.getAccess()).append("</access>\n");

            // Render instantiation/types if they exist
            for (String cName : a.getCName()) {
                sb.append(indent).append("    <cName>").append(cName).append("</cName>\n");
            }
            sb.append(indent).append("  </attr>\n");
        }

        // Methods
        for (Met m : c.getMets()) {
            sb.append(indent).append("  <met>\n");
            sb.append(indent).append("    <name>").append(m.getName()).append("</name>\n");
            sb.append(indent).append("  </met>\n");
        }

        // Relationships
        for (URel r : c.getURels()) {
            sb.append(indent).append("  <uRel>\n");
            sb.append(indent).append("    <cName>").append(r.getCName()).append("</cName>\n");
            if (!r.getType().isEmpty()) {
                sb.append(indent).append("    <type>").append(r.getType().get(0)).append("</type>\n");
            }
            sb.append(indent).append("  </uRel>\n");
        }

        sb.append(indent).append("</c>\n");
        return sb.toString();
    }
}