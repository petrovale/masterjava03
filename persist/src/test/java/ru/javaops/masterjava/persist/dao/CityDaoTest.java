package ru.javaops.masterjava.persist.dao;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.CityTestData;
import ru.javaops.masterjava.persist.model.City;

import static org.junit.Assert.*;
import static ru.javaops.masterjava.persist.CityTestData.CITIES;

public class CityDaoTest extends AbstractDaoTest<CityDao> {

    public CityDaoTest() {
        super(CityDao.class);
    }

    @BeforeClass
    public static void init() throws Exception {
        CityTestData.init();
    }

    @Before
    public void setUp() throws Exception {
        CityTestData.setUp();
    }

    @Test
    public void insertGeneratedId() throws Exception {
        int id = dao.insertGeneratedId(new City("Иннополис"));
        assertEquals(dao.getCurrVal(), id);
    }

    @Test
    public void insertWitId() throws Exception {
        int nextId = dao.getNextVal();
        dao.insertWitId(new City(nextId, "Иннополис"));
        assertEquals(nextId, dao.getCurrVal());
    }

    @Test
    public void getAll() throws Exception {
        assertEquals(CITIES , dao.getAll());
    }

}