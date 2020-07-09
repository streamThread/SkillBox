package model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@IdClass(LinkedPurchaseList.LinkedPurchaseListKey.class)
public class LinkedPurchaseList {

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id")
  private Students studentId;

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id")
  private Courses courseId;

  @EqualsAndHashCode
  @ToString
  @AllArgsConstructor
  public class LinkedPurchaseListKey implements Serializable {

    static final long serialVersionUID = 1L;
    @Getter
    @Setter
    private Students studentId;

    @Getter
    @Setter
    private Courses courseId;
  }
}
