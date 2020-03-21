import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class SessionFactoryUtil {

    private static volatile SessionFactory sessionFactory;

    private SessionFactoryUtil() {
    }

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = new Configuration().configure("hibernate.cfg.xml").
                    buildSessionFactory();
        }
        return sessionFactory;
    }
}
