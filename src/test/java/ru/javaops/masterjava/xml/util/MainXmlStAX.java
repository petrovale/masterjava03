package ru.javaops.masterjava.xml.util;

import com.google.common.io.Resources;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainXmlStAX {
    private String nameProject;
    private static final Map<String, String> groups = new HashMap<>();

    public MainXmlStAX(String nameProject) {
        this.nameProject = nameProject;
    }

    public static void main(String[] args) throws IOException, XMLStreamException {
        MainXmlStAX mainXmlStAX = new MainXmlStAX("masterjava");

        try (StaxStreamProcessor processor =
                     new StaxStreamProcessor(Resources.getResource("payload.xml").openStream())) {
            XMLStreamReader reader = processor.getReader();
            List<User> users = new ArrayList<>();
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLEvent.START_ELEMENT) {
                    if ("User".equals(reader.getLocalName())) {
                        users.add(readUser(reader));
                    }
                    if ("Groups".equals(reader.getLocalName())) {
                        readGroup(reader);
                    }
                }
            }
            for (User user : users) {
                if (user.getGroups().stream().anyMatch(g -> g.nameProject.equals(mainXmlStAX.nameProject))) {
                    System.out.println(user.fullName + "/" + user.email);
                }
            }
        }
    }
    private static void readGroup(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            switch (event) {
                case XMLStreamReader.START_ELEMENT:
                    String element = reader.getLocalName();
                    if (element.equals("Group")) {
                        String attribute = reader.getAttributeValue(0);
                        groups.put(reader.getElementText(), attribute);
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    return;
            }
        }
    }

    private static User readUser(XMLStreamReader reader) throws XMLStreamException {
        User user = new User();
        List<Group> groupsOfUser = new ArrayList<>();
        user.setEmail(reader.getAttributeValue(null, "email"));
        while (reader.hasNext()) {
            int event = reader.next();
            switch (event) {
                case XMLStreamReader.START_ELEMENT:
                    String element = reader.getLocalName();
                    if (element.equals("fullName"))
                        user.setFullName(reader.getElementText());
                    else if (element.equals("group")) {
                        String nameGroup = reader.getElementText();
                        groupsOfUser.add(new Group(nameGroup, groups.get(nameGroup)));
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    user.setGroups(groupsOfUser);
                    return user;
            }
        }
        return user;
    }

    public static class User {
        private String fullName;
        private List<Group> groups;
        private String email;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public List<Group> getGroups() {
            return groups;
        }

        public void setGroups(List<Group> group) {
            this.groups = group;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class Group {
        private String name;
        private String nameProject;

        public Group(String name) {
            this.name = name;
        }

        public Group(String name, String nameProject) {
            this.name = name;
            this.nameProject = nameProject;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNameProject() {
            return nameProject;
        }

        public void setNameProject(String nameProject) {
            this.nameProject = nameProject;
        }
    }
}
