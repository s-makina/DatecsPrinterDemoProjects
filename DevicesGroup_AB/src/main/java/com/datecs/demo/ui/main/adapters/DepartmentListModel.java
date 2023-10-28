package com.datecs.demo.ui.main.adapters;


import java.io.Serializable;

public class DepartmentListModel implements Serializable {

    private String deptID;
    private String deptTaxGr;
    private String deptRecSalesSum;
    private String deptTotalSalesSum;
    private String deptNameLines;

    public DepartmentListModel(String deptID, String deptTaxGr, String deptRecSalesSum, String deptTotalSalesSum, String deptNameLines) {
        this.deptID = deptID;
        this.deptTaxGr = deptTaxGr;
        this.deptRecSalesSum = deptRecSalesSum;
        this.deptTotalSalesSum = deptTotalSalesSum;
        this.deptNameLines = deptNameLines;
    }

    public String getDeptID() {
        return deptID;
    }

    public void setDeptID(String deptID) {
        this.deptID = deptID;
    }

    public String getDeptTaxGr() {
        return deptTaxGr;
    }

    public void setDeptTaxGr(String deptTaxGr) {
        this.deptTaxGr = deptTaxGr;
    }

    public String getDeptRecSalesSum() {
        return deptRecSalesSum;
    }

    public void setDeptRecSalesSum(String deptRecSalesSum) {
        this.deptRecSalesSum = deptRecSalesSum;
    }

    public String getDeptTotalSalesSum() {
        return deptTotalSalesSum;
    }

    public void setDeptTotalSalesSum(String deptTotalSalesSum) {
        this.deptTotalSalesSum = deptTotalSalesSum;
    }

    public String getDeptNameLines() {
        return deptNameLines;
    }

    public void setDeptNameLines(String deptNameLines) {
        this.deptNameLines = deptNameLines;
    }
}