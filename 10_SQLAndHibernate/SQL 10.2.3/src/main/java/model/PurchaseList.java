package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@NoArgsConstructor
@Entity
@IdClass(PurchaseList.PurchaseListKey.class)
public class PurchaseList {

  @Id
  @Column(name = "course_name")
  private String courseName;

  private int price;

  @Id
  @Column(name = "student_name")
  private String name;

  @Column(name = "subscription_date")
  private LocalDateTime subscriptionDate;

  @EqualsAndHashCode
  @ToString
  @AllArgsConstructor
  public class PurchaseListKey implements Serializable {

    static final long serialVersionUID = 1L;
    @Getter
    @Setter
    private String courseName;

    @Getter
    @Setter
    private String name;
  }
}
