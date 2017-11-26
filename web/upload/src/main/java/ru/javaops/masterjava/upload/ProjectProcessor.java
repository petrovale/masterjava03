package ru.javaops.masterjava.upload;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.ProjectDao;
import ru.javaops.masterjava.persist.model.Project;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

@Slf4j
public class ProjectProcessor {
    private static final JaxbParser jaxbParser = new JaxbParser(ObjectFactory.class);
    private final ProjectDao projectDao = DBIProvider.getDao(ProjectDao.class);
    private final GroupProcessor groupProcessor = new GroupProcessor();

    public void process(StaxStreamProcessor processor) throws XMLStreamException, JAXBException {
        val map = projectDao.getAsMap();
        val unmarshaller = jaxbParser.createUnmarshaller();
        while (processor.startElement("Project", "Projects")) {
            ru.javaops.masterjava.xml.schema.Project xmlProject = unmarshaller.unmarshal(processor.getReader(), ru.javaops.masterjava.xml.schema.Project.class);
            Project project = new Project(xmlProject.getName(), xmlProject.getDescription());
            if (!map.containsKey(xmlProject.getName())) {
                projectDao.insert(project);
                map.put(xmlProject.getName(), project);
            } else {
                project = map.get(xmlProject.getName());
            }
            groupProcessor.process(xmlProject.getGroup(), project.getId());
        }
    }
}
