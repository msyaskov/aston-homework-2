package aston.h2.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Group extends AbstractEntity {

    private String name; // unique

    private LocalDate graduationDate;

    private Curator curator;

    private List<Student> students;

    public Group(Integer id, String name, LocalDate graduationDate, Curator curator, List<Student> students) {
        super(id);
        this.name = name;
        this.graduationDate = graduationDate;
        this.curator = curator;
        this.students = students;
    }

    @Override
    public String toString() {
        return "GroupEntity{" +
                "name='" + name + '\'' +
                ", graduationDate=" + graduationDate +
                '}';
    }
}
