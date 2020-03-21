package model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;


@EqualsAndHashCode(exclude = "courses")
@ToString(exclude = "courses")
@Getter
@Setter
@NoArgsConstructor
@Entity
public class Students {
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "Subscriptions",
            joinColumns = {@JoinColumn(name = "student_id")},
            inverseJoinColumns = {@JoinColumn(name = "course_id")})
    List<Courses> courses;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int age;

    private String name;
    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

}