package Bank;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Bank {


  private final BigDecimal MINIMUM_AMOUNT_TO_SEND_FOR_VERIFICATION = new BigDecimal(
      50000);
  private final Random RANDOM = new Random();
  private final ConcurrentHashMap<Integer, Account> ACCOUNTS;
  private final SecurityService securityService = new SecurityService();
  private final TransferService transferService = new TransferService();

  Bank(ConcurrentHashMap<Integer, Account> accounts) {
    ACCOUNTS = accounts;
  }

  public Map<Integer, Account> getAccounts() {
    return new HashMap<>(ACCOUNTS);
  }

  private synchronized boolean isFraud(String fromAccountNum,
      String toAccountNum, long amount)
      throws InterruptedException {
    Thread.sleep(1000);
    return RANDOM.nextBoolean();
  }

  public TransferStatus transfer(Integer fromAccountNum, Integer toAccountNum,
      double amountIn) throws InterruptedException {

    if (fromAccountNum.equals(toAccountNum)) {
      log.info("The sender and recipient accounts must be different");
      return TransferStatus.BLOCKED_ACCOUNTS_ARE_THE_SAME;
    }

    Account from = ACCOUNTS.get(fromAccountNum);
    Account to = ACCOUNTS.get(toAccountNum);

    BigDecimal amount = BigDecimal.valueOf(amountIn)
        .setScale(2, RoundingMode.HALF_UP);

    Account.SyncAccs syncAccs = new Account.SyncAccs(from, to);

    synchronized (syncAccs.syncAcc1) {
      synchronized (syncAccs.syncAcc2) {
        return checkAndSendTransferToProperService(from, to, amount);
      }
    }
  }

  private TransferStatus checkAndSendTransferToProperService(Account from,
      Account to, BigDecimal amount) throws InterruptedException {
    if (from.isBlocked() || to.isBlocked()) {
      log.info(
          "The transfer cannot be completed. One of the accounts is blocked" +
              " or checking by security service");
      return TransferStatus.IS_BLOCKED;
    }
    if (from.getMoney().compareTo(amount) <= 0) {
      log.info("Insufficient funds for transfer");
      return TransferStatus.INSUFFICIENT_FUNDS;
    }
    Transaction transaction = new Transaction(from, to, amount);
    if (amount.compareTo(MINIMUM_AMOUNT_TO_SEND_FOR_VERIFICATION) < 0) {
      transferService.transactions.put(transaction);
      log.debug(String.format("%s sent to Transfer_Service",
          transaction.toString()));
      return TransferStatus.COMMITED;
    }
    from.setBlocked(true);
    to.setBlocked(true);
    securityService.transactionsToCheck.put(transaction);
    log.info("Sending transaction to verification. Please, wait...");
    return TransferStatus.SENT_TO_SECURITY_SERVICE;
  }

  public BigDecimal getBalance(Integer accountNum) {
    Account account = ACCOUNTS.get(accountNum);
    return account.getMoney();
  }

  void shutDownBankServices(int afterThisTimeValue, TimeUnit timeUnit)
      throws InterruptedException {
    securityService.executorService
        .awaitTermination(afterThisTimeValue, timeUnit);
    securityService.executorService.shutdown();
    transferService.executorService.shutdown();
    securityService.executorService
        .awaitTermination(1000, TimeUnit.MILLISECONDS);
    transferService.executorService
        .awaitTermination(1000, TimeUnit.MILLISECONDS);
  }

  enum TransferStatus {
    BLOCKED_ACCOUNTS_ARE_THE_SAME,
    IS_BLOCKED,
    INSUFFICIENT_FUNDS,
    COMMITED,
    SENT_TO_SECURITY_SERVICE
  }

  class SecurityService implements Runnable {

    private final ExecutorService executorService = Executors
        .newSingleThreadExecutor();
    private final BlockingQueue<Transaction> transactionsToCheck = new LinkedBlockingQueue<>();

    SecurityService() {
      executorService.submit(this);
    }

    @SneakyThrows(InterruptedException.class)
    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        Transaction transaction = transactionsToCheck.take();
        Account from = transaction.getFrom();
        Account to = transaction.getTo();
        BigDecimal amount = transaction.getAmount();
        try {
          if (isFraud(String.valueOf(from.getAcc_number()),
              String.valueOf(to.getAcc_number()),
              amount.longValue())) {
            log.info(
                "The operation did not pass the security check. Accounts are blocked");
            continue;
          }
        } catch (InterruptedException e) {
          Account.SyncAccs syncAccs = new Account.SyncAccs(from, to);
          synchronized (syncAccs.syncAcc1) {
            synchronized (syncAccs.syncAcc2) {
              from.setBlocked(false);
              to.setBlocked(false);
            }
          }
          log.error("SecurityService isFraud error", e);
          break;
        }
        transferService.transactions.putFirst(transaction);
        log.debug(String.format(
            "%s was approved by Security_Service and sent to Transfer_Service",
            transaction.toString()));
      }
    }
  }

  class TransferService implements Runnable {

    private final ExecutorService executorService = Executors
        .newFixedThreadPool(5);
    private final BlockingDeque<Transaction> transactions = new LinkedBlockingDeque<>();

    TransferService() {
      executorService.submit(this);
    }

    @SneakyThrows(InterruptedException.class)
    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        Transaction transaction = transactions.take();
        Account from = transaction.getFrom();
        Account to = transaction.getTo();
        BigDecimal amount = transaction.getAmount();
        Account.SyncAccs syncAccs = new Account.SyncAccs(from, to);
        synchronized (syncAccs.syncAcc1) {
          synchronized (syncAccs.syncAcc2) {
            from.withdrawMoney(amount);
            to.addMoney(amount);
          }
        }
        log.info(String.format("Money sent from %s to %s: %.2f у.е.",
            from.getAcc_number(), to.getAcc_number(), amount.doubleValue()));
      }
    }
  }
}


