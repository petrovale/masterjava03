package ru.javaops.masterjava.xml.util;

import one.util.streamex.StreamEx;
import com.google.common.io.Resources;
import org.xml.sax.SAXException;
import ru.javaops.masterjava.xml.schema.*;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MainXml {
    private static final Comparator<User> USER_COMPARATOR = Comparator.comparing(User::getFullName).thenComparing(User::getEmail);

    private static final JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);

    static {
        JAXB_PARSER.setSchema(Schemas.ofClasspath("payload.xsd"));
    }

    public static void main(String[] args) throws IOException, JAXBException, SAXException {
                if (args.length != 1) {
            throw new IllegalArgumentException("project name parameter not found");
        }
        String nameProject = args[0];

        Payload payload = JAXB_PARSER.unmarshal(
                Resources.getResource("payload.xml").openStream());
        List<User> users = payload.getUsers().getUser();

        Project project = StreamEx.of(payload.getProjects().getProject())
                .filter(p -> p.getName().equals(nameProject))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Invalid project name '" + nameProject + '\''));

        final List<Project.Group> groupsOfProject = new ArrayList<>(project.getGroup());

        Set<User> usersOfProject = StreamEx.of(payload.getUsers().getUser())
                .filter(u -> !Collections.disjoint(groupsOfProject, u.getGroups()))
                .collect(Collectors.toCollection(() -> new TreeSet<>(USER_COMPARATOR)));
        usersOfProject.forEach(u -> System.out.println(u.getFullName() + "/" + u.getEmail()));

    }
}
