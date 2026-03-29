package Bank;

import java.util.ArrayList;
import java.util.List;

public class test2 {
    private String name;
    private AccessModifier access;
    private List<String> cName;  // User-defined classes in datatype
    private List<String> dName;  // Data structures
    private List<String> inName; // Instantiations

    public Attr(String name, AccessModifier access) {
        this.name = name;
        this.access = access;
        this.cName = new ArrayList<>();
        this.dName = new ArrayList<>();
        this.inName = new ArrayList<>();
    }

    public String getName() { return name; }
    public AccessModifier getAccess() { return access; }
    public List<String> getCName() { return cName; }
    public List<String> getDName() { return dName; }
    public List<String> getInName() { return inName; }
}