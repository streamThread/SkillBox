import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
public class Bank {

    final private BigDecimal MINIMUM_AMOUNT_TO_SEND_FOR_VERIFICATION = new BigDecimal(50000);
    private final SecurityService SECURITY_SERVICE = new SecurityService(this);
    Random random = new Random();
    private ConcurrentHashMap<Integer, Account> accounts = new ConcurrentHashMap<>();

    public ConcurrentHashMap<Integer, Account> getAccounts() {
        return accounts;
    }

    private synchronized boolean isFraud(String fromAccountNum, String toAccountNum, long amount)
            throws InterruptedException {
        Thread.sleep(1000);
        return random.nextBoolean();
    }

    public void transfer(Integer fromAccountNum, Integer toAccountNum, double amountIn) {

        if (fromAccountNum.equals(toAccountNum)) {
            throw new IllegalArgumentException("The sender and recipient accounts must be different");
        }

        Account from = accounts.get(fromAccountNum);
        Account to = accounts.get(toAccountNum);

        BigDecimal amount = BigDecimal.valueOf(amountIn).setScale(2, RoundingMode.HALF_UP);

        Account.SyncAccs syncAccs = new Account.SyncAccs(from, to);

        synchronized (syncAccs.syncAcc1) {
            synchronized (syncAccs.syncAcc2) {
                if (doSimpleTransfer(from, to, amount).equals(TransactionStatus.NEED_CHECK)) {
                    from.setBlocked(true);
                    to.setBlocked(true);
                    SECURITY_SERVICE.TRANSACTIONS.add(new Transaction(from, to, amount));
                    synchronized (SECURITY_SERVICE) {
                        SECURITY_SERVICE.notify();
                    }
                    log.info("Sending transaction to verification. Please, wait...");
                }
            }
        }
    }

    private TransactionStatus doSimpleTransfer(Account from, Account to, BigDecimal amount) {
        if (from.isBlocked() || to.isBlocked()) {
            log.info("The transfer cannot be completed. One of the accounts is blocked" +
                    " or checking by security service");
            return TransactionStatus.BLOCKED;
        }
        if (from.getMoney().compareTo(amount) <= 0) {
            log.info("Insufficient funds for transfer");
            return TransactionStatus.BLOCKED;
        }
        if (amount.compareTo(MINIMUM_AMOUNT_TO_SEND_FOR_VERIFICATION) < 0) {
            from.setMoney(from.getMoney().subtract(amount));
            to.setMoney(to.getMoney().add(amount));
            log.info(String.format("Money sent from %s to %s: %.2f у.е.",
                    from.getAcc_number(), to.getAcc_number(), amount.doubleValue()));
            return TransactionStatus.COMMITED;
        }
        return TransactionStatus.NEED_CHECK;
    }

    public BigDecimal getBalance(Integer accountNum) {
        Account account = accounts.get(accountNum);
        return account.getMoney();
    }

    private enum TransactionStatus {
        BLOCKED, COMMITED, NEED_CHECK
    }

    private static class SecurityService implements Runnable {

        final Deque<Transaction> TRANSACTIONS = new ArrayDeque<>();
        private final Bank BANK;

        SecurityService(Bank bank) {
            BANK = bank;
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(this);
        }

        @Override
        public void run() {
            Transaction transaction;
            while (true) {
                synchronized (this) {
                    while ((transaction = TRANSACTIONS.poll()) == null) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            log.error(e);
                        }
                    }
                }
                Account from = transaction.getFrom();
                Account to = transaction.getTo();
                BigDecimal amount = transaction.getAmount();
                Account.SyncAccs syncAccs = new Account.SyncAccs(from, to);
                try {
                    if (BANK.isFraud(String.valueOf(from.getAcc_number()),
                            String.valueOf(to.getAcc_number()),
                            amount.longValue())) {
                        log.info("The operation did not pass the security check. Accounts are blocked");
                        continue;
                    }
                } catch (InterruptedException e) {
                    synchronized (syncAccs.syncAcc1) {
                        synchronized (syncAccs.syncAcc2) {
                            to.setBlocked(false);
                            from.setBlocked(false);
                            log.error(e);
                        }
                    }
                }
                synchronized (syncAccs.syncAcc1) {
                    synchronized (syncAccs.syncAcc2) {
                        from.setBlocked(false);
                        to.setBlocked(false);
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


