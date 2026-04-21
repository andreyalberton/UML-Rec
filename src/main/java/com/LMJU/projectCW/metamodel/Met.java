package com.LMJU.projectCW.metamodel;

import java.util.ArrayList;
import java.util.List;

/**
 * A method inside a class.
 * Carries its parameters, local variables, and return type.
 * Mirrors: Met = (name, access, {par_i}, {local_j}, ret)
 *
 * The return type starts as an empty Ret so downstream code
 * (like the XML exporter) never has to null-check it.
 */
public class Met {
    private String name;
    private AccessModifier access;
    private List<Par> pars;
    private List<Local> locals;
    private Ret ret;

    public Met(String name, AccessModifier access) {
        this.name = name;
        this.access = access;
        this.pars = new ArrayList<>();
        this.locals = new ArrayList<>();
        this.ret = new Ret(); // start with an empty return type, not null
    }

    public String getName() { return name; }
    public AccessModifier getAccess() { return access; }
    public List<Par> getPars() { return pars; }
    public List<Local> getLocals() { return locals; }
    public Ret getRet() { return ret; }
    public void setRet(Ret ret) { this.ret = ret; }
}