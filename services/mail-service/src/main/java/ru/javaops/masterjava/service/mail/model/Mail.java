package ru.javaops.masterjava.service.mail.model;

public class Mail {
    private static final String OK = "OK";
    private String email;
    private String result;

    public Mail() {
    }

    public Mail(String email, String cause) {
        this.email = email;
        this.result = cause;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return '(' + email + ',' + result + ')';
    }
}
