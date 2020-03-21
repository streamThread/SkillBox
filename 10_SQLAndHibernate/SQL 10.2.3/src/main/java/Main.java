import model.Courses;
import model.Students;
import model.Subscriptions;
import model.Teachers;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class Main {

    public static void main(String[] args) {

        SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory();

        Session session = sessionFactory.openSession();

        try (session; sessionFactory) {

            Courses course = session.get(Courses.class, 5);

            Students student = session.get(Students.class, 6);

            Teachers teacher = session.get(Teachers.class, 7);

            Subscriptions subscription = session.get(Subscriptions.class, new Subscriptions().new SubscriptionKey(course,course.getStudents().get(0)));

            System.out.println(subscription);

            System.out.println(course);

            System.out.println(student);

            System.out.println(teacher);
        }
    }
}
