package model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

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
