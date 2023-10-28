package com.datecs.demo.ui.main.adapters;


import java.io.Serializable;

public class DepartmentListModel implements Serializable {
    private int deptID;
    private String deptName;
    private int deptVAT;
    private double deptPrice;

    public DepartmentListModel(int deptID, String deptName, int deptVAT, double deptPrice) {
        this.deptID = deptID;
        this.deptName = deptName;
        this.deptVAT = deptVAT;
        this.deptPrice = deptPrice;
    }

    public int getDeptID() {
        return deptID;
    }

    public void setDeptID(int deptID) {
        this.deptID = deptID;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public int getDeptVAT() {
        return deptVAT;
    }

    public void setDeptVAT(int deptVAT) {
        this.deptVAT = deptVAT;
    }

    public double getDeptPrice() {
        return deptPrice;
    }

    public void setDeptPrice(double deptPrice) {
        this.deptPrice = deptPrice;
    }
}