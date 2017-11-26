package ru.javaops.masterjava.service.mail;

import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import ru.javaops.masterjava.config.Configs;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.service.mail.dao.MailDao;
import ru.javaops.masterjava.service.mail.model.Mail;

import java.util.List;

@Slf4j
public class MailSender {
    public static final Config CONFIG_MAIL = Configs.getConfig("mail.conf","mail");
    private static final MailDao MAIL_DAO = DBIProvider.getDao(MailDao.class);

    static void sendMail(List<Addressee> to, List<Addressee> cc, String subject, String body) {
        log.info("Send mail to \'" + to + "\' cc \'" + cc + "\' subject \'" + subject + (log.isDebugEnabled() ? "\nbody=" + body : ""));

        Email emailAgent = new SimpleEmail();

        emailAgent.setHostName(CONFIG_MAIL.getString("host"));
        emailAgent.setSmtpPort(CONFIG_MAIL.getInt("port"));
        emailAgent.setAuthenticator(new DefaultAuthenticator(CONFIG_MAIL.getString("username"), CONFIG_MAIL.getString("password")));
        emailAgent.setSSLOnConnect(CONFIG_MAIL.getBoolean("useSSL"));
        String result;
        try {
            emailAgent.setFrom(CONFIG_MAIL.getString("username"));
            emailAgent.setSubject(subject);
            emailAgent.setCharset("utf8");
            emailAgent.setMsg(body);
            for (Addressee addressee : to) {
                emailAgent.addTo(addressee.getEmail());
            }
            emailAgent.send();
            result = "Ok";
        } catch (EmailException e) {
            e.printStackTrace();
            log.error("Failed to send mail to " + to);
            result = e.getMessage() + ". " + e.toString();
        }
        MAIL_DAO.insert(new Mail(to.toString(), result));
    }
}
