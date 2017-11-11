package ru.javaops.masterjava.upload;

import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UserProcessor {
    private final UserDao dao = DBIProvider.getDao(UserDao.class);

    public List<User> process(final InputStream is, int chunk) throws XMLStreamException {
        final StaxStreamProcessor processor = new StaxStreamProcessor(is);
        List<User> users = new ArrayList<>();

        while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
            final String email = processor.getAttribute("email");
            final UserFlag flag = UserFlag.valueOf(processor.getAttribute("flag"));
            final String fullName = processor.getReader().getElementText();
            final User user = new User(fullName, email, flag);
            users.add(user);
        }

        int[] ids = dao.insertAll(users, chunk);
        return IntStream.range(0, users.size()).filter(i -> ids[i] == 0)
                .mapToObj(users::get)
                .collect(Collectors.toList());
    }

    public List<User> processMultiply(final InputStream is, int chunk) throws XMLStreamException, InterruptedException, ExecutionException {
        final ExecutorService executor = Executors.newFixedThreadPool(8);
        final CompletionService<List<User>> completionService = new ExecutorCompletionService<>(executor);
        final List<Future<List<User>>> futureResults = new ArrayList<>();

        List<User> users = new ArrayList<>();

        final StaxStreamProcessor processor = new StaxStreamProcessor(is);
        int countUser = 0;
        while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
            countUser++;
            final String email = processor.getAttribute("email");
            final UserFlag flag = UserFlag.valueOf(processor.getAttribute("flag"));
            final String fullName = processor.getReader().getElementText();
            final User user = new User(fullName, email, flag);
            users.add(user);
            if (countUser == chunk) {
                List<User> finalUsers = users;
                futureResults.add(completionService.submit(() -> insert(finalUsers, chunk)));
                users = new ArrayList<>();
            }
        }

        if (!users.isEmpty()) {
            List<User> finalUsers = users;
            futureResults.add(completionService.submit(() -> insert(finalUsers, chunk)));
        }

        List<User> usersResult = new ArrayList<>();
        while (!futureResults.isEmpty()) {
            Future<List<User>> future = completionService.take();
            futureResults.remove(future);
            usersResult.addAll(future.get());
        }
        return usersResult;
    }

    private List<User> insert(List<User> users, int chunk) {
        int[] ids = dao.insertAll(users, chunk);
        return IntStream.range(0, users.size()).filter(i -> ids[i] == 0)
                .mapToObj(users::get)
                .collect(Collectors.toList());
    }
}
