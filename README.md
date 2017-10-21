### _Разработка полнофункционального многомодульного Maven проекта_
#### состоящего из 3-х веб приложений:

![image](https://cloud.githubusercontent.com/assets/13649199/23876457/ab01ff0a-084e-11e7-964f-49c90579fac9.png)

- **приложение импорта** из XML (JAXB, StAX, XPath, XSLT)
- **многопоточного почтового веб-сервиса** (JavaMail, java.util.concurrent, JAX-WS, MTOM, хендлеры авторизации, логирования и статистики) 
- **веб приложения отправки почты с вложениями**
  - по SOAP (JAX-WS, MTOM)
  - по JAX-RS (Jersey)
  - по JMS ([ActiveMQ](http://activemq.apache.org/))
  - через [AKKA](http://akka.io/)
  - используя асинхронные сервлеты 3.0
- сохранение данных в PostgreSQL используя [jDBI](http://jdbi.org/)
- миграция базы [LiquiBase](http://www.liquibase.org/)
- использование в проекте [Guava](https://github.com/google/guava/wiki), [Thymleaf](http://www.thymeleaf.org/), [Lombok](https://projectlombok.org/), [StreamEx](https://github.com/amaembo/streamex), 
[Typesafe Config](https://github.com/typesafehub/config), [Java Microbenchmark JMH](http://openjdk.java.net/projects/code-tools/jmh)

### Ресурсы (основы)
- Intuit, <a href="http://www.intuit.ru/studies/courses/16/16/lecture/27127">Потоки выполнения. Синхронизация</a>
- Алексей Владыкин, <a href="https://www.youtube.com/watch?v=zxZ0BXlTys0&list=PLlb7e2G7aSpRSBWi5jbGjIe-v_CjRO_Ug">Основы многопоточность в Java</a>
- Виталий Чибриков, <a href="https://www.youtube.com/watch?v=dLDhB6SRXzw&list=PLrCZzMib1e9qkzxEuU_huxtSAxrW1t9NZ">Java. Многопоточность</a>
- Computer Science Center, курс <a href="https://compscicenter.ru/courses/hp-course/2016-spring">Параллельное программирование</a>
- Юрий Ткач, курс <a href="https://www.youtube.com/playlist?list=PL6jg6AGdCNaXo06LjCBmRao-qJdf38oKp">Advanced Java - Concurrency</a>
- Головач, курс <a href="https://www.youtube.com/playlist?list=PLoij6udfBncgVRq487Me6yQa1kqtxobZS">Java Multithreading</a>

---