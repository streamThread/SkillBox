package model;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@EqualsAndHashCode(exclude = "courses")
@ToString(exclude = "courses")
@Getter
@Setter
@NoArgsConstructor
@Entity
public class Students {

  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
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