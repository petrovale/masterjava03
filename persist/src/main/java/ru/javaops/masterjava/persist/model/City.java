package ru.javaops.masterjava.persist.model;

import com.bertoncelj.jdbi.entitymapper.Column;
import lombok.*;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class City extends BaseEntity {
    private @NonNull String name;
    @Column("middle_name")
    private @NonNull String middleName;

    public City(int id, String name, String middleName) {
        this(name, middleName);
        this.id = id;
    }
}
