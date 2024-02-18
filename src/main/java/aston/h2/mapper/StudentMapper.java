package aston.h2.mapper;

import aston.h2.dto.StudentDto;
import aston.h2.entity.Student;

public class StudentMapper implements Mapper<Student, StudentDto> {

    @Override
    public StudentDto map(Student student) {
        if (student == null) {
            return null;
        }

        return new StudentDto(student.getId(), student.getName(), student.getDateOfBirth());
    }

    @Override
    public Student reverseMap(StudentDto studentDto) {
        if (studentDto == null) {
            return null;
        }

        Student student = new Student();
        student.setId(studentDto.getId());
        student.setName(studentDto.getName());
        student.setDateOfBirth(studentDto.getDateOfBirth());

        return student;
    }
}
