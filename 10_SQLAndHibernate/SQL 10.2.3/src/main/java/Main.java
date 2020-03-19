import model.Courses;
import model.Students;
import model.Teachers;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class Main {

    public static void main(String[] args) {

        SessionFactory sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();

        Session session = sessionFactory.openSession();

        try (session; sessionFactory) {

            Courses course = session.get(Courses.class, 5);

            Students student = session.get(Students.class, 6);

            Teachers teacher = session.get(Teachers.class, 7);

            System.out.println(course);

            System.out.println(student);

            System.out.println(teacher);
        }
    }
}
