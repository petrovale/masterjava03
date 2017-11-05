package ru.javaops.masterjava.xml.util;

import com.google.common.base.Splitter;
import one.util.streamex.StreamEx;
import com.google.common.io.Resources;
import org.xml.sax.SAXException;
import ru.javaops.masterjava.xml.schema.*;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.nullToEmpty;

public class MainXml {
    private static final Comparator<User> USER_COMPARATOR = Comparator.comparing(User::getFullName).thenComparing(User::getEmail);

    public static void main(String[] args) throws IOException, JAXBException, SAXException, XMLStreamException {
                if (args.length != 1) {
            throw new IllegalArgumentException("project name parameter not found");
        }
        String nameProject = args[0];
        URL payloadUrl =  Resources.getResource("payload.xml");

        Set<User> usersOfProject = parseByJaxb(nameProject, payloadUrl);
        usersOfProject.forEach(u -> System.out.println(u.getFullName() + "/" + u.getEmail()));
        System.out.println();

        usersOfProject = parseByStax(nameProject, payloadUrl);
        usersOfProject.forEach(u -> System.out.println(u.getFullName() + "/" + u.getEmail()));
    }

    private static Set<User> parseByJaxb(String nameProject, URL payloadUrl) throws JAXBException, IOException {
        JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);
        JAXB_PARSER.setSchema(Schemas.ofClasspath("payload.xsd"));

        Payload payload;
        try (InputStream is = payloadUrl.openStream()) {
            payload = JAXB_PARSER.unmarshal(is);
        }

        Project project = StreamEx.of(payload.getProjects().getProject())
                .filter(p -> p.getName().equals(nameProject))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Invalid project name '" + nameProject + '\''));

        final List<Project.Group> groupsOfProject = new ArrayList<>(project.getGroup());
        return StreamEx.of(payload.getUsers().getUser())
                .filter(u -> !Collections.disjoint(groupsOfProject, u.getGroups()))
                .collect(Collectors.toCollection(() -> new TreeSet<>(USER_COMPARATOR)));
    }

    private static Set<User> parseByStax(String nameProject, URL payloadUrl) throws IOException, XMLStreamException, JAXBException {
        try (StaxStreamProcessor processor =
                     new StaxStreamProcessor(payloadUrl.openStream())) {
            final Set<String> groupNames = new HashSet<>();

            while (processor.startElement("Project", "Projects")) {
                if (nameProject.equals(processor.getAttributeValue("name"))){
                    while (processor.startElement("Group", "Project")) {
                        groupNames.add(processor.getAttributeValue("name"));
                    }
                    break;
                }
            }

            if (groupNames.isEmpty()) {
                throw new IllegalArgumentException("Invalid " + nameProject + " or no groups");
            }

            Set<User> users = new TreeSet<>(USER_COMPARATOR);

            JaxbParser parser = new JaxbParser(User.class);
            while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
                String groupsRefs = processor.getAttributeValue("groups");
                if (!Collections.disjoint(groupNames, Splitter.on(' ').splitToList(nullToEmpty(groupsRefs)))) {
                    User user = parser.unmarshal(processor.getReader(), User.class);
                    users.add(user);
                }
            }
            return users;
        }
    }
}
