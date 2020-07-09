package Bank;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import Bank.Bank.TransferStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Log4j2
public class TestBank {

  private static Bank bank;

  @BeforeEach
  void init() {
    ConcurrentHashMap<Integer, Account> accounts = new ConcurrentHashMap<>();
    for (int i = 1; i < 100_001; i++) {
      accounts.put(i, new Account(i, 70000d));
    }
    bank = new Bank(accounts);
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
      bank.shutDownBankServices(1, TimeUnit.MINUTES);
      BigDecimal result = bank.getAccounts().values().stream()
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
          .mapToLong(
              a -> a.getBalance((new Random().nextInt(1000) + 1)).longValue())
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
      bank.shutDownBankServices(1, TimeUnit.MINUTES);
      BigDecimal result = bank.getAccounts().values().stream()
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
      TransferStatus transferStatus = bank.transfer(3, 4, 42000.54);
      BigDecimal accFromResultMoney = bank.getAccounts().get(3).getMoney();
      BigDecimal accToResultMoney = bank.getAccounts().get(4).getMoney();
      Thread.sleep(3000);
      assertAll(
          "simple transfer() test",
          () -> assertEquals(0,
              BigDecimal.valueOf(27999.46).compareTo(accFromResultMoney),
              "Money is wrong at 'from' acc in cache"),
          () -> assertEquals(0,
              BigDecimal.valueOf(112000.54).compareTo(accToResultMoney),
              "Money is wrong at 'to' acc in cache"),
          () -> assertEquals(TransferStatus.COMMITED, transferStatus,
              "wrong transfer status returned")
      );
    } catch (RuntimeException | InterruptedException ex) {
      log.error(ex);
    }
  }

  @Test
  @DisplayName("transfer() blocking test")
  void transferTest3() {
    try {
      bank.getAccounts().get(1).setBlocked(true);
      TransferStatus transferStatus1 = bank.transfer(1, 2, 22000.54);
      TransferStatus transferStatus2 = bank.transfer(3, 4, 80000);
      TransferStatus transferStatus3 = bank.transfer(5, 5, 10000);
      Thread.sleep(5000);
      assertAll(
          "transfer() blocking test",
          () -> assertEquals(TransferStatus.IS_BLOCKED, transferStatus1,
              "Blocked acc must not to do transfer"),
          () -> assertEquals(TransferStatus.INSUFFICIENT_FUNDS, transferStatus2,
              "Insufficient funds blocking not works"),
          () -> assertEquals(TransferStatus.BLOCKED_ACCOUNTS_ARE_THE_SAME,
              transferStatus3,
              "The sender and recipient accounts must be different"));
    } catch (RuntimeException | InterruptedException e) {
      log.error(e);
    }
  }
}


