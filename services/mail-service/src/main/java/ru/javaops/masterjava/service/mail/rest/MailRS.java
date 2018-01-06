package ru.javaops.masterjava.service.mail.rest;


import com.google.common.collect.ImmutableList;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.validator.constraints.NotBlank;
import ru.javaops.masterjava.service.mail.Attachment;
import ru.javaops.masterjava.service.mail.GroupResult;
import ru.javaops.masterjava.service.mail.MailServiceExecutor;
import ru.javaops.masterjava.service.mail.MailWSClient;
import ru.javaops.masterjava.service.mail.util.MailUtils;
import ru.javaops.masterjava.web.WebStateException;

import javax.activation.DataHandler;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Path("/")
public class MailRS {
    @GET
    @Path("test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        return "Test";
    }

    @POST
    @Path("send")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces(MediaType.APPLICATION_JSON)
    public GroupResult send(@FormDataParam("attach") FormDataBodyPart attachBodyPart,
                            @NotBlank @FormDataParam("users") String users,
                            @FormDataParam("subject") String subject,
                            @NotBlank @FormDataParam("body") String body) throws WebStateException {
        final List<Attachment> attachments;
        if (attachBodyPart == null) {
            attachments = ImmutableList.of();
        } else {
            try {
                String attachName = attachBodyPart.getContentDisposition().getFileName();
//          UTF-8 encoding workaround: https://java.net/jira/browse/JERSEY-3032
                String utf8name = new String(attachName.getBytes("ISO8859_1"), "UTF-8");
                BodyPartEntity bodyPartEntity = ((BodyPartEntity) attachBodyPart.getEntity());

                attachments = ImmutableList.of(new Attachment(utf8name, new DataHandler((MailUtils.ProxyDataSource) bodyPartEntity::getInputStream)));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
        }
        return MailServiceExecutor.sendBulk(MailUtils.split(users), subject, body, attachments);
    }
}