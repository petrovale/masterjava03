package ru.javaops.masterjava.service.mail;

import com.sun.xml.ws.developer.StreamingAttachment;
import ru.javaops.masterjava.web.WebStateException;

import javax.activation.DataHandler;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.soap.MTOM;
import java.util.List;
import java.util.Set;

@StreamingAttachment(parseEagerly=true, memoryThreshold=40000L)
@MTOM
@WebService(targetNamespace = "http://mail.javaops.ru/")
//@SOAPBinding(
//        style = SOAPBinding.Style.DOCUMENT,
//        use= SOAPBinding.Use.LITERAL,
//        parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public interface MailService {

    @WebMethod
    String sendToGroup(
            @WebParam(name = "to") Set<Addressee> to,
            @WebParam(name = "cc") Set<Addressee> cc,
            @WebParam(name = "subject") String subject,
            @WebParam(name = "body") String body,
            @WebParam(name = "attachments") List<Attachment> attachments) throws WebStateException;

    @WebMethod
    GroupResult sendBulk(
            @WebParam(name = "to") Set<Addressee> to,
            @WebParam(name = "subject") String subject,
            @WebParam(name = "body") String body,
            @WebParam(name = "attachments") List<Attachment> attachments) throws WebStateException;
}