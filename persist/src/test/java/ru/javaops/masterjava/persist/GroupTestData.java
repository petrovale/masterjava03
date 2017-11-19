package ru.javaops.masterjava.persist;

import com.google.common.collect.ImmutableList;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.GroupType;

import java.util.List;

public class GroupTestData {
    public static Group TOPJAVA06;
    public static Group TOPJAVA07;
    public static Group TOPJAVA08;
    public static List<Group> TOPJAVA_GROUPS;

    public static void init() {
        TOPJAVA06 = new Group("topjava06", GroupType.FINISHED);
        TOPJAVA07 = new Group("topjava07", GroupType.FINISHED);
        TOPJAVA08 = new Group("topjava08", GroupType.CURRENT);
        TOPJAVA_GROUPS = ImmutableList.of(TOPJAVA06, TOPJAVA07, TOPJAVA08);
    }

    public static void setUp() {
        GroupDao dao = DBIProvider.getDao(GroupDao.class);
        dao.clean();
        DBIProvider.getDBI().useTransaction((conn, status) -> {
            TOPJAVA_GROUPS.forEach(dao::insert);
        });
    }
}
