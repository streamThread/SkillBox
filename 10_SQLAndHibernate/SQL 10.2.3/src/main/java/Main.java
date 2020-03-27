import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class Main {

    public static void main(String[] args) {

        SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory();

        Session session = sessionFactory.openSession();

        try (session) {

            Transaction transaction = session.beginTransaction();

            String queryInsert = "insert into LinkedPurchaseList(courseId, studentId) " +
                    "select (select c from Courses c where c.name = p.courseName), " +
                    "(select s from Students s where s.name = p.name) from PurchaseList p";

            int rows = session.createQuery(queryInsert).executeUpdate();

            System.out.println("rows : " + rows);

            transaction.commit();
        }
    }
}
