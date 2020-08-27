package com.ehr.model;

import lombok.Data;

@Data
public class ehrutil {
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescribtion() {
        return describtion;
    }

    public void setDescribtion(String describtion) {
        this.describtion = describtion;
    }

    public String getEvalue() {
        return evalue;
    }

    public void setEvalue(String evalue) {
        this.evalue = evalue;
    }

    private String describtion;
    private String evalue;

    public ehrutil(int id, String describtion, String evalue) {
        this.id = id;
        this.describtion = describtion;
        this.evalue = evalue;
    }
}
