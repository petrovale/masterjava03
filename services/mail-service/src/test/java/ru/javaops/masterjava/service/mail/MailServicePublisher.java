package ru.javaops.masterjava.service.mail;

import com.typesafe.config.Config;
import ru.javaops.masterjava.config.Configs;
import ru.javaops.masterjava.persist.DBIProvider;

import javax.xml.ws.Endpoint;
import java.sql.DriverManager;

/**
 * User: gkislin
 * Date: 28.05.2014
 */
public class MailServicePublisher {

    public static void main(String[] args) {
        Config db = Configs.getConfig("persist.conf","db");
        DBIProvider.init(() -> {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("PostgreSQL driver not found", e);
            }
            return DriverManager.getConnection(db.getString("url"), db.getString("user"), db.getString("password"));
        });
        Endpoint.publish("http://localhost:8080/mail/mailService", new MailServiceImpl());
    }
}
