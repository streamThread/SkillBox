package model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Teachers {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private int age;

  private String name;

  private int salary;

}
