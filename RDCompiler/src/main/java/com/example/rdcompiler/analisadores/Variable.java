package com.example.rdcompiler.analisadores;

public class Variable {

    private String name;
    private String value;
    private boolean isUsed;
    private String type;
    private int row;

    public Variable()
    {
        this.name = "";
        this.value = "";
        this.isUsed = false;
        this.type = "";
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }
}
