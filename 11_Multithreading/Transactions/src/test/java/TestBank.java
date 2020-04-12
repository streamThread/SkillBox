import lombok.extern.log4j.Log4j2;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
public class TestBank {

    static SessionFactory sessionFactory;
    static Bank bank;

    @BeforeAll
    static void beforeInit() {
        sessionFactory = SessionFactoryUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        bank = new Bank();
        try (session) {
            Transaction transaction = session.beginTransaction();
            for (int i = 0; i < 1000; i++) {
                session.save(new Account(70_000d));
            }
            transaction.commit();
        }
    }

    @BeforeEach
    void init() {
        Session session = sessionFactory.openSession();
        try (session) {
            bank = new Bank();
            Transaction transaction = session.beginTransaction();
            session.createQuery("update Account set money = 70000.00, isBlocked = false").setLockOptions(LockOptions.UPGRADE).executeUpdate();
            transaction.commit();
        }
    }


    @Test
    @DisplayName("transfer() test under load by Stream API")
    void transferOnLoadTest() {
        try {
            Stream.generate(() -> bank).limit(100_000).parallel()
                    .map(a -> new FutureTask(Executors.callable(a.getRunnableTransfer(
                            new Random().nextInt(1000) + 1,
                            new Random().nextInt(1000) + 1,
                            new Random().nextDouble() * 100000))))
                    .forEach(a -> {
                        a.run();
                        try {
                            a.get();
                        } catch (InterruptedException | ExecutionException e) {
                            log.error(e);
                        }
                    });
            bank.executors.shutdownNow();
            Session session = sessionFactory.openSession();
            Query<BigDecimal> query = session.createQuery("select sum(money) from Account");
            BigDecimal result = query.uniqueResult();
            session.close();
            assertEquals(0, new BigDecimal(70_000_000).compareTo(result));
        } catch (RuntimeException e) {
            log.error(e);
        }
    }

    @Test
    @DisplayName("transfer() test under load with high concurrency by Stream API")
    void transferOnLoadWithHighConcurrencyTest() {
        try {
            Stream.generate(() -> bank).limit(50_000).parallel()
                    .map(a -> new FutureTask(Executors.callable(a.getRunnableTransfer(
                            new Random().nextInt(10) + 1,
                            new Random().nextInt(10) + 1,
                            new Random().nextDouble() * 100000))))
                    .forEach(futureTask -> {
                        futureTask.run();
                        try {
                            futureTask.get();
                        } catch (InterruptedException | ExecutionException e) {
                            log.error(e);
                        }
                    });
            bank.executors.shutdownNow();
            Session session = sessionFactory.openSession();
            Query<BigDecimal> query = session.createQuery("select sum(money) from Account");
            BigDecimal result = query.uniqueResult();
            session.close();
            assertEquals(0, new BigDecimal(70_000_000).compareTo(result));
        } catch (RuntimeException e) {
            log.error(e);
        }
    }

    @Test
    @DisplayName("getBalance() test under load from different accounts")
    void getBalanceOnLoadTest() {
        try {
            long result = Stream.generate(() -> bank).limit(1_000_000).parallel()
                    .mapToLong(a -> a.getBalance((new Random().nextInt(1000) + 1)).longValue())
                    .sum();
            assertEquals(70_000_000_000L, result);
        } catch (RuntimeException e) {
            log.error(e);
        }
    }

    @Test
    @DisplayName("getBalance() test under load from one account")
    void getBalanceOnLoadTest2() {
        try {
            long result = Stream.generate(() -> bank).limit(1_000_000).parallel()
                    .mapToLong(a -> a.getBalance(1).longValue())
                    .sum();
            assertEquals(70_000_000_000L, result);
        } catch (RuntimeException e) {
            log.error(e);
        }
    }

    @Test
    @DisplayName("transfer() test under load by Executors")
    void transferOnLoadTest2() throws InterruptedException {
        Session session = SessionFactoryUtil.getSessionFactory().openSession();
        try (session) {
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            List<Callable<Object>> futures = new ArrayList<>();
            for (int i = 0; i < 50_000; i++) {
                futures.add(Executors.callable(bank.getRunnableTransfer(
                        new Random().nextInt(1000) + 1,
                        new Random().nextInt(1000) + 1,
                        new Random().nextDouble() * 100_000)));
            }
            executorService.invokeAll(futures);
            bank.executors.shutdownNow();
            Query<BigDecimal> query = session.createQuery("select sum(money) from Account");
            BigDecimal result = query.uniqueResult();
            assertEquals(0, new BigDecimal(70_000_000).compareTo(result));
        } catch (RuntimeException ex) {
            log.error(ex);
        }
    }

    @Test
    @DisplayName("transfer() test under load with high concurrency by Executors")
    void transferOnLoadWithHighConcurrencyTest2() throws InterruptedException {
        Session session = SessionFactoryUtil.getSessionFactory().openSession();
        try (session) {
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            List<Callable<Object>> transfers = new ArrayList<>();
            for (int i = 0; i < 1_000; i++) {
                transfers.add(Executors.callable(bank.getRunnableTransfer(
                        new Random().nextInt(10) + 1,
                        new Random().nextInt(10) + 1,
                        new Random().nextDouble() * 100_000), null));
            }
            executorService.invokeAll(transfers);
            bank.executors.shutdownNow();
            Query<BigDecimal> query = session.createQuery("select sum(money) from Account");
            BigDecimal result = query.uniqueResult();
            assertEquals(0, new BigDecimal(70_000_000).compareTo(result));
        } catch (RuntimeException ex) {
            log.error(ex);
        }
    }

    @Test
    @DisplayName("simple transfer() test")
    void transferTest() {
        Session session = sessionFactory.openSession();
        try (session) {
            bank.transfer(3, 4, 42000.54);
            BigDecimal accFromResultMoney = bank.getAccounts().get(3).getMoney();
            BigDecimal accToResultMoney = bank.getAccounts().get(4).getMoney();
            BigDecimal accFromResultMoneyFromBase = session.get(Account.class, 3).getMoney();
            BigDecimal accToResultMoneyFromBase = session.get(Account.class, 4).getMoney();
            assertAll(
                    "simple transfer() test",
                    () -> assertEquals(0, BigDecimal.valueOf(27999.46).compareTo(accFromResultMoney), "Money is wrong at 'from' acc in cache"),
                    () -> assertEquals(0, BigDecimal.valueOf(112000.54).compareTo(accToResultMoney), "Money is wrong at 'to' acc in cache"),
                    () -> assertEquals(0, BigDecimal.valueOf(27999.46).compareTo(accFromResultMoneyFromBase), "Money is wrong at 'from' acc in DB"),
                    () -> assertEquals(0, BigDecimal.valueOf(112000.54).compareTo(accToResultMoneyFromBase), "Money is wrong at 'to' acc in DB")
            );
        } catch (RuntimeException ex) {
            log.error(ex);
        }
    }

    @Test
    @DisplayName("transfer() Exeption test")
    void transferTest2() {
        try {
            Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
                bank.transfer(3, 3, 22000.54);
            });
            assertEquals("The sender and recipient accounts must be different", exception.getMessage());
        } catch (RuntimeException ex) {
            log.error(ex);
        }
    }

    @Test
    @DisplayName("transfer() blocking test")
    void transferTest3() {
        Session session = sessionFactory.openSession();
        try (session) {
            Transaction transaction = session.beginTransaction();
            session.get(Account.class, 1).setBlocked(true);
            transaction.commit();
            bank.transfer(1, 2, 22000.54);
            bank.transfer(3, 4, 80000);
            BigDecimal acc1ResultMoney = bank.getAccounts().get(1).getMoney();
            BigDecimal acc2ResultMoney = bank.getAccounts().get(2).getMoney();
            BigDecimal acc3ResultMoney = bank.getAccounts().get(3).getMoney();
            BigDecimal acc43ResultMoney = bank.getAccounts().get(4).getMoney();
            assertAll(
                    "transfer() blocking test",
                    () -> assertEquals(0, acc1ResultMoney.compareTo(acc2ResultMoney), "Blocked acc must not to do transfer"),
                    () -> assertEquals(0, acc3ResultMoney.compareTo(acc43ResultMoney), "Insufficient funds blocking not works")
            );
        }
    }
}


