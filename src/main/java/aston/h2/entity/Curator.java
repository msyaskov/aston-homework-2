package aston.h2.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Curator extends AbstractEntity {

    private String name;

    private String email;

    private Integer experience;

    private Group group;

    public Curator(Integer id, String name, String email, Integer experience, Group group) {
        super(id);
        this.name = name;
        this.email = email;
        this.experience = experience;
        this.group = group;
    }

}
