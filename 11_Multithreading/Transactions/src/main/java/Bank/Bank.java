package Bank;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.concurrent.*;

@Log4j2
public class Bank {

    final SecurityService SECURITY_SERVICE = new SecurityService();
    final TransferService TRANSFER_SERVICE = new TransferService();
    private final BigDecimal MINIMUM_AMOUNT_TO_SEND_FOR_VERIFICATION = new BigDecimal(50000);
    private final Random RANDOM = new Random();
    private final ConcurrentHashMap<Integer, Account> ACCOUNTS = new ConcurrentHashMap<>();

    public ConcurrentHashMap<Integer, Account> getACCOUNTS() {
        return ACCOUNTS;
    }

    private synchronized boolean isFraud(String fromAccountNum, String toAccountNum, long amount)
            throws InterruptedException {
        Thread.sleep(1000);
        return RANDOM.nextBoolean();
    }

    public void transfer(Integer fromAccountNum, Integer toAccountNum, double amountIn) throws InterruptedException {

        if (fromAccountNum.equals(toAccountNum)) {
            throw new IllegalArgumentException("The sender and recipient accounts must be different");
        }

        Account from = ACCOUNTS.get(fromAccountNum);
        Account to = ACCOUNTS.get(toAccountNum);

        BigDecimal amount = BigDecimal.valueOf(amountIn).setScale(2, RoundingMode.HALF_UP);

        Account.SyncAccs syncAccs = new Account.SyncAccs(from, to);

        synchronized (syncAccs.syncAcc1) {
            synchronized (syncAccs.syncAcc2) {
                    doSimpleCheck(from, to, amount);
            }
        }
    }

    private void doSimpleCheck(Account from, Account to, BigDecimal amount) throws InterruptedException {
        if (from.isBlocked() || to.isBlocked()) {
            log.info("The transfer cannot be completed. One of the accounts is blocked" +
                    " or checking by security service");
            return;
        }
        if (from.getMoney().compareTo(amount) <= 0) {
            log.info("Insufficient funds for transfer");
            return;
        }
        Transaction transaction = new Transaction(from, to, amount);
        if (amount.compareTo(MINIMUM_AMOUNT_TO_SEND_FOR_VERIFICATION) < 0) {
            TRANSFER_SERVICE.TRANSACTIONS.put(transaction);
            log.debug(String.format("%s sent to Transfer_Service",
                    transaction.toString()));
            return;
        }
        from.setBlocked(true);
        to.setBlocked(true);
        SECURITY_SERVICE.TRANSACTIONS_TO_CHECK.put(transaction);
        log.info("Sending transaction to verification. Please, wait...");
    }

    public BigDecimal getBalance(Integer accountNum) {
        Account account = ACCOUNTS.get(accountNum);
        return account.getMoney();
    }

    class SecurityService implements Runnable {

        private final BlockingQueue<Transaction> TRANSACTIONS_TO_CHECK = new LinkedBlockingQueue<>();
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        SecurityService() {
            executorService.submit(this);
        }

        @SneakyThrows(InterruptedException.class)
        @Override
        public void run() {
            while (true) {
                Transaction transaction = TRANSACTIONS_TO_CHECK.take();
                Account from = transaction.getFrom();
                Account to = transaction.getTo();
                BigDecimal amount = transaction.getAmount();
                try {
                    if (isFraud(String.valueOf(from.getAcc_number()),
                            String.valueOf(to.getAcc_number()),
                            amount.longValue())) {
                        log.info("The operation did not pass the security check. Accounts are blocked");
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
                }
                TRANSFER_SERVICE.TRANSACTIONS.putFirst(transaction);
                log.debug(String.format("%s was approved by Security_Service and sent to Transfer_Service",
                        transaction.toString()));
            }
        }
    }

    class TransferService implements Runnable {

        private final BlockingDeque<Transaction> TRANSACTIONS = new LinkedBlockingDeque<>();
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        TransferService() {
            executorService.submit(this);
        }

        @SneakyThrows(InterruptedException.class)
        @Override
        public void run() {
            while (true) {
                Transaction transaction = TRANSACTIONS.take();
                Account from = transaction.getFrom();
                Account to = transaction.getTo();
                BigDecimal amount = transaction.getAmount();
                Account.SyncAccs syncAccs = new Account.SyncAccs(from, to);
                synchronized (syncAccs.syncAcc1) {
                    synchronized (syncAccs.syncAcc2) {
                        from.setMoney(from.getMoney().subtract(amount));
                        to.setMoney(to.getMoney().add(amount));
                    }
                }
                log.info(String.format("Money sent from %s to %s: %.2f у.е.",
                        from.getAcc_number(), to.getAcc_number(), amount.doubleValue()));
            }
        }
    }
}


