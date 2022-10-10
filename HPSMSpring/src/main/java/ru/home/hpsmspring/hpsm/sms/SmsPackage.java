package ru.home.hpsmspring.hpsm.sms;

public class SmsPackage {
    private String message;
    private int format;
    private String query;
//pojo

    public SmsPackage(String message, int format,String query) {
        this.message = message;
        this.format = format;
        this.query = query;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
