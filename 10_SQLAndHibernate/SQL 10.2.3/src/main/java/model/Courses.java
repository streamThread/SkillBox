package model;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@EqualsAndHashCode(exclude = "students")
@ToString(exclude = "students")
@Getter
@Setter
@NoArgsConstructor
@Entity
public class Courses {

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "Subscriptions",
            joinColumns = {@JoinColumn(name = "course_id")},
            inverseJoinColumns = {@JoinColumn(name = "student_id")})
    List<Students> students;

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
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Teachers teacher;
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
