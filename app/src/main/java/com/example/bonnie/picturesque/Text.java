package com.example.bonnie.picturesque;


//Classe que nem precisei utilizar XD
// mas resolvi mante-la

public class Text {

    private String tag;
    private String value;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String txt) {
        this.value = txt;
    }

    public Text(String tag, String txt) {
        this.tag = tag;
        this.value = txt;
    }
}
