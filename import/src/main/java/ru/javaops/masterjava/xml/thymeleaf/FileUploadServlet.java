package ru.javaops.masterjava.xml.thymeleaf;

import org.thymeleaf.context.WebContext;
import ru.javaops.masterjava.xml.schema.User;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

@WebServlet(urlPatterns = "/upload", loadOnStartup = 1)
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10, //10 MB
        maxFileSize = 1024 * 1024 * 30, // 30 MB
        maxRequestSize = 1024 * 1024 * 50)//50 MB
public class FileUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Collection<Part> parts = request.getParts();
        List<User> users = new ArrayList<>();
        for (Part part : parts) {

            try (InputStream is = part.getInputStream()) {
                StaxStreamProcessor processor = new StaxStreamProcessor(is);

                JaxbParser parser = new JaxbParser(User.class);
                while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
                    User user = parser.unmarshal(processor.getReader(), User.class);
                    users.add(user);
                }

            } catch (XMLStreamException e) {
                e.printStackTrace();
            } catch (JAXBException e) {
                e.printStackTrace();
            }
            break;
        }

        WebContext ctx = new WebContext(request, response, request.getServletContext(),
                request.getLocale());
        ctx.setVariable("users", users);
        ThymeleafAppUtil.getTemplateEngine().process("fileuploadResponse", ctx, response.getWriter());
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        WebContext ctx = new WebContext(request, response, request.getServletContext(),
                request.getLocale());
        ThymeleafAppUtil.getTemplateEngine().process("upload", ctx, response.getWriter());
    }

}
