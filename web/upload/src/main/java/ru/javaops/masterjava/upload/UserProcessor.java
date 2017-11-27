package ru.javaops.masterjava.upload;

import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import one.util.streamex.StreamEx;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.dao.UserGroupDao;
import ru.javaops.masterjava.persist.model.City;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserGroup;
import ru.javaops.masterjava.persist.model.type.UserFlag;
import ru.javaops.masterjava.upload.PayloadProcessor.FailedEmails;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.nullToEmpty;

@Slf4j
public class UserProcessor {
    private static final int NUMBER_THREADS = 4;

    private static final JaxbParser jaxbParser = new JaxbParser(ObjectFactory.class);
    private static UserDao userDao = DBIProvider.getDao(UserDao.class);
    private static final GroupDao groupDao = DBIProvider.getDao(GroupDao.class);
    private static final UserGroupDao userGroupDao = DBIProvider.getDao(UserGroupDao.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_THREADS);

    /*
     * return failed users chunks
     */
    public List<FailedEmails> process(final StaxStreamProcessor processor, Map<String, City> cities, int chunkSize) throws XMLStreamException, JAXBException {
        log.info("Start processing with chunkSize=" + chunkSize);

        Map<String, Future<List<String>>> chunkFutures = new LinkedHashMap<>();  // ordered map (emailRange -> chunk future)

        int id = userDao.getSeqAndSkip(chunkSize);
        List<User> chunk = new ArrayList<>(chunkSize);
        Map<Integer, List<UserGroup>> chunkUserGroup = new HashMap<>();
        val unmarshaller = jaxbParser.createUnmarshaller();
        List<FailedEmails> failed = new ArrayList<>();
        val groups = groupDao.getAsMap();

        while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
            String cityRef = processor.getAttribute("city");  // unmarshal doesn't get city ref
            String strGroupRefs = processor.getAttribute("groupRefs");
            List<String> groupRefs;
            if (strGroupRefs == null) {
                groupRefs = new ArrayList<>();
            } else {
                groupRefs = Splitter.on(' ').splitToList(nullToEmpty(strGroupRefs));  // unmarshal doesn't get group refs
            }
            ru.javaops.masterjava.xml.schema.User xmlUser = unmarshaller.unmarshal(processor.getReader(), ru.javaops.masterjava.xml.schema.User.class);

            String nameGroup;
            if ((nameGroup = checkNameGroupConsistent(groups, groupRefs)) != null && strGroupRefs != null) {
                failed.add(new FailedEmails(xmlUser.getEmail(), "Group '" + nameGroup + "' is not present in DB"));
            } else if (cities.get(cityRef) == null) {
                failed.add(new FailedEmails(xmlUser.getEmail(), "City '" + cityRef + "' is not present in DB"));
            } else {
                int userId = id++;
                final User user = new User(userId, xmlUser.getValue(), xmlUser.getEmail(), UserFlag.valueOf(xmlUser.getFlag().value()), cityRef);
                if (!groupRefs.isEmpty()) {
                    final List<UserGroup> userGroups = toUserGroups(userId, groups, groupRefs);
                    chunkUserGroup.put(userId, userGroups);
                }
                chunk.add(user);
                if (chunk.size() == chunkSize) {
                    addChunkFutures(chunkFutures, chunk, chunkUserGroup);
                    chunk = new ArrayList<>(chunkSize);
                    chunkUserGroup = new HashMap<>();
                    id = userDao.getSeqAndSkip(chunkSize);
                }
            }
        }

        if (!chunk.isEmpty()) {
            addChunkFutures(chunkFutures, chunk, chunkUserGroup);
        }

        List<String> allAlreadyPresents = new ArrayList<>();
        chunkFutures.forEach((emailRange, future) -> {
            try {
                List<String> alreadyPresentsInChunk = future.get();
                log.info("{} successfully executed with already presents: {}", emailRange, alreadyPresentsInChunk);
                allAlreadyPresents.addAll(alreadyPresentsInChunk);
            } catch (InterruptedException | ExecutionException e) {
                log.error(emailRange + " failed", e);
                failed.add(new FailedEmails(emailRange, e.toString()));
            }
        });
        if (!allAlreadyPresents.isEmpty()) {
            failed.add(new FailedEmails(allAlreadyPresents.toString(), "already presents"));
        }
        return failed;
    }

    private void addChunkFutures(Map<String, Future<List<String>>> chunkFutures, List<User> chunk, Map<Integer, List<UserGroup>> chunkUserGroup) {
        String emailRange = String.format("[%s-%s]", chunk.get(0).getEmail(), chunk.get(chunk.size() - 1).getEmail());
        Future<List<String>> future = executorService.submit(() -> {
            List<String> emails = userDao.insertAndGetConflictEmails(chunk);
            for (User user : chunk) {
                if (!emails.contains(user.getEmail())) {
                    userGroupDao.insertBatch(chunkUserGroup.get(user.getId()));
                }
            }
            return emails;
        });
        chunkFutures.put(emailRange, future);
        log.info("Submit chunk: " + emailRange);
    }

    private String checkNameGroupConsistent(Map<String, Group> groups, List<String> groupRefs) {
        return groupRefs.stream().filter(nameGroup -> groups.get(nameGroup) == null).findFirst().orElse(null);
    }

    private static List<UserGroup> toUserGroups(int userId, Map<String, Group> groups, List<String> groupRefs) {
        List<Integer> groupIds = groupRefs.stream().map(nameGroup -> groups.get(nameGroup).getId()).collect(Collectors.toList());
        return StreamEx.of(groupIds).map(groupId -> new UserGroup(userId, groupId)).toList();
    }
}
