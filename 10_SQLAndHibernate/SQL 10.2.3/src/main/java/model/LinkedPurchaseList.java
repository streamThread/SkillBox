package model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity
@IdClass(LinkedPurchaseList.LinkedPurchaseListKey.class)
public class LinkedPurchaseList {

    @Id
    @Column(name = "student_id")
    private int studentId;

    @Id
    @Column(name = "course_id")
    private int courseId;

    @EqualsAndHashCode
    @ToString
    @AllArgsConstructor
    public class LinkedPurchaseListKey implements Serializable {

        static final long serialVersionUID = 1L;
        @Getter
        @Setter
        private int studentId;

        @Getter
        @Setter
        private int courseId;
    }
}
