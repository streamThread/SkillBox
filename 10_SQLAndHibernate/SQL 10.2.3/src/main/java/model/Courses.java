package model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
public class Courses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String description;

    private int duration;

    private String name;

    private int price;

    @Column(name = "price_per_hour")
    private float pricePerHour;

    @Column(name = "students_count")
    private int studentsCount;

    @Column(name = "teacher_id")
    private int teacherId;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum")
    private MyEnum type;

    private enum MyEnum {
        PROGRAMMING,
        DESIGN,
        MARKETING,
        MANAGEMENT
    }
}
