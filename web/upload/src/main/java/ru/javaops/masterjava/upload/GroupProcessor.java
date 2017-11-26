package ru.javaops.masterjava.upload;

import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.type.GroupType;
import ru.javaops.masterjava.xml.schema.Project;

import java.util.List;

public class GroupProcessor {
    private final GroupDao groupDao = DBIProvider.getDao(GroupDao.class);

    public void process(List<Project.Group> groups, Integer projectId) {
        for (Project.Group group : groups) {
            groupDao.insert(new Group(group.getName(), GroupType.valueOf(group.getType().value()), projectId));
        }
    }
}
