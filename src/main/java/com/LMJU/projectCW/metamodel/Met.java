package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

public class Met {
    private String name;
    private AccessModifier access;
    private List<Par> pars;     // {par_i}
    private List<Local> locals; // {local_j}
    private Ret ret;            // ret

    public Met(String name, AccessModifier access) {
        this.name = name;
        this.access = access;
        this.pars = new ArrayList<>();
        this.locals = new ArrayList<>();
        this.ret = new Ret(); // Initialize empty return type
    }

    public String getName() { return name; }
    public AccessModifier getAccess() { return access; }
    public List<Par> getPars() { return pars; }
    public List<Local> getLocals() { return locals; }
    public Ret getRet() { return ret; }
    public void setRet(Ret ret) { this.ret = ret; }
}