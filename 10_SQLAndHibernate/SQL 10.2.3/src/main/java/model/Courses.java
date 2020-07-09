package model;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
