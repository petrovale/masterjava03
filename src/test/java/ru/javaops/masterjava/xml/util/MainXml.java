package ru.javaops.masterjava.xml.util;

import com.google.common.io.Resources;
import org.xml.sax.SAXException;
import ru.javaops.masterjava.xml.schema2.*;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainXml {
    private static final JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);

    static {
        JAXB_PARSER.setSchema(Schemas.ofClasspath("payload.xsd"));
    }

    private String nameProject;

    public MainXml(String nameProject) {
        this.nameProject = nameProject;
    }

    public static void main(String[] args) throws IOException, JAXBException, SAXException {
        MainXml mainXml = new MainXml("topjava");

        Payload payload = JAXB_PARSER.unmarshal(
                Resources.getResource("payload.xml").openStream());
        List<User> users = payload.getUsers().getUser();
        List<String> usersOfProject = new ArrayList<>();
        for (User user : users) {
            List<JAXBElement<Object>> groups = user.getGroup();
            for (JAXBElement<Object> group : groups) {
                if (((ProjectType) ((GroupType) group.getValue()).getProject()).getName().
                        equals(mainXml.nameProject)) {
                    usersOfProject.add(user.getFullName());
                    break;
                }
            }
        }
        Collections.sort(usersOfProject, String::compareTo);
        System.out.println(usersOfProject);
    }
}
