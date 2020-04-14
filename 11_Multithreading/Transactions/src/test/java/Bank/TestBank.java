package Bank;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
public class TestBank {

    private static Bank bank;

    @BeforeAll
    static void beforeInit() {
        bank = new Bank();
        for (int i = 1; i < 100_001; i++) {
            bank.getACCOUNTS().put(i, new Account(i, 70000d));
        }
    }

    @BeforeEach
    void init() {
        bank.getACCOUNTS().values().forEach(a -> {
            a.setMoney(new BigDecimal(70000));
            a.setBlocked(false);
        });
    }

    @Test
    @DisplayName("transfer() test under load by Stream API")
    void transferOnLoadTest() {
        try {
            Stream.generate(() -> bank).parallel().limit(1_000_000)
                    .forEach(a -> {
                        try {
                            a.transfer(new Random().nextInt(100_000) + 1,
                                    new Random().nextInt(100_000) + 1,
                                    new Random().nextDouble() * 100_000);
                        } catch (InterruptedException | RuntimeException e) {
                            log.error(e);
                        }
                    });
            bank.SECURITY_SERVICE.executorService.awaitTermination(1, TimeUnit.MINUTES);
            bank.TRANSFER_SERVICE.executorService.shutdownNow();
            BigDecimal result = bank.getACCOUNTS().values().stream()
                    .map(Account::getMoney)
                    .reduce(new BigDecimal(0), BigDecimal::add);
            assertEquals(0, new BigDecimal(7000_000_000L).compareTo(result));
        } catch (RuntimeException | InterruptedException e) {
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
    @DisplayName("transfer() test under load by Executors")
    void transferOnLoadTest2() {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            List<Callable<Object>> futures = new ArrayList<>();
            for (int i = 0; i < 1000_000; i++) {
                futures.add(Executors.callable(() -> {
                    try {
                        bank.transfer(
                                new Random().nextInt(100_000) + 1,
                                new Random().nextInt(100_000) + 1,
                                new Random().nextDouble() * 100_000);
                    } catch (InterruptedException e) {
                        log.error(e);
                    }
                }));
            }
            executorService.invokeAll(futures);
            bank.SECURITY_SERVICE.executorService.awaitTermination(1, TimeUnit.MINUTES);
            bank.TRANSFER_SERVICE.executorService.shutdownNow();
            BigDecimal result = bank.getACCOUNTS().values().stream()
                    .map(Account::getMoney)
                    .reduce(new BigDecimal(0), BigDecimal::add);
            assertEquals(0, new BigDecimal(7000_000_000L).compareTo(result));
        } catch (RuntimeException | InterruptedException ex) {
            log.error(ex);
        }
    }

    @Test
    @DisplayName("simple transfer() test")
    void transferTest() {
        try {
            bank.transfer(3, 4, 42000.54);
            BigDecimal accFromResultMoney = bank.getACCOUNTS().get(3).getMoney();
            BigDecimal accToResultMoney = bank.getACCOUNTS().get(4).getMoney();
            Thread.sleep(3000);
            assertAll(
                    "simple transfer() test",
                    () -> assertEquals(0, BigDecimal.valueOf(27999.46).compareTo(accFromResultMoney), "Money is wrong at 'from' acc in cache"),
                    () -> assertEquals(0, BigDecimal.valueOf(112000.54).compareTo(accToResultMoney), "Money is wrong at 'to' acc in cache")
            );
        } catch (RuntimeException | InterruptedException ex) {
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
        try {
            bank.getACCOUNTS().get(1).setBlocked(true);
            bank.transfer(1, 2, 22000.54);
            bank.transfer(3, 4, 80000);
            BigDecimal acc1ResultMoney = bank.getACCOUNTS().get(1).getMoney();
            BigDecimal acc2ResultMoney = bank.getACCOUNTS().get(2).getMoney();
            BigDecimal acc3ResultMoney = bank.getACCOUNTS().get(3).getMoney();
            BigDecimal acc43ResultMoney = bank.getACCOUNTS().get(4).getMoney();
            Thread.sleep(3000);
            assertAll(
                    "transfer() blocking test",
                    () -> assertEquals(0, acc1ResultMoney.compareTo(acc2ResultMoney), "Blocked acc must not to do transfer"),
                    () -> assertEquals(0, acc3ResultMoney.compareTo(acc43ResultMoney), "Insufficient funds blocking not works")
            );
        } catch (RuntimeException | InterruptedException e) {
            log.error(e);
        }
    }
}


