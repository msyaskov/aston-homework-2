package aston.hw2.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Student extends AbstractEntity {

    private String name;

    private LocalDate dateOfBirth;

    private Group group;

    public Student(Integer id, String name, LocalDate dateOfBirth, Group group) {
        super(id);
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.group = group;
    }

}
