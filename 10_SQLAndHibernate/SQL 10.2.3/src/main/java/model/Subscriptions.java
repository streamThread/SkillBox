package model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@IdClass(Subscriptions.SubscriptionKey.class)
public class Subscriptions {

    @Id
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Courses courseId;

    @Id
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Students studentId;

    @Column(name = "subscription_date")
    private LocalDateTime subscriptionDate;

    @EqualsAndHashCode
    @ToString
    @AllArgsConstructor
    public class SubscriptionKey implements Serializable {

        static final long serialVersionUID = 1L;
        @Getter
        @Setter
        private Courses courseId;
        @Getter
        @Setter
        private Students studentId;
    }
}
