package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.model.City;

import java.util.List;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class CityDao implements AbstractDao {

    public City insert(City city) {
        if (city.isNew()) {
            int id = insertGeneratedId(city);
            city.setId(id);
        } else {
            insertWitId(city);
        }
        return city;
    }

    @SqlUpdate("INSERT INTO cities (name, middle_name) VALUES (:name, :middleName) ON CONFLICT DO NOTHING")
    @GetGeneratedKeys
    abstract int insertGeneratedId(@BindBean City city);

    @SqlUpdate("INSERT INTO cities (id, name, middle_name) VALUES (:id, :name, :middleName) ON CONFLICT DO NOTHING")
    abstract void insertWitId(@BindBean City city);

    //   http://stackoverflow.com/questions/13223820/postgresql-delete-all-content
    @SqlUpdate("TRUNCATE cities CASCADE")
    @Override
    public abstract void clean();

    @SqlQuery("SELECT * FROM cities ORDER BY name")
    public abstract List<City> getAll();

    @SqlQuery("SELECT id FROM cities WHERE middle_name = :middleName")
    public abstract int getIdByMiddleName(@Bind("middleName") String middleName);

    @SqlQuery("SELECT nextval('city_seq')")
    abstract int getNextVal();

    @SqlQuery("SELECT max(id) from cities")
    abstract int getCurrVal();
}
