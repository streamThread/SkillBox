import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.internal.util.collections.ConcurrentReferenceHashMap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Log4j2
public class Bank {

    final Object lock = new Object();
    final private BigDecimal MINIMUM_AMOUNT_TO_SEND_FOR_VERIFICATION = new BigDecimal(50000);
    Random random = new Random();
    ExecutorService executors = Executors.newSingleThreadExecutor();
    private ConcurrentReferenceHashMap<Integer, Account> accounts = new ConcurrentReferenceHashMap<>();

    public ConcurrentReferenceHashMap<Integer, Account> getAccounts() {
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

        Account from = getAccountFromBaseOrCache(fromAccountNum);
        Account to = getAccountFromBaseOrCache(toAccountNum);

        BigDecimal amount = BigDecimal.valueOf(amountIn).setScale(2, RoundingMode.HALF_UP);

        SyncAccs syncAccs = new SyncAccs(from, to);

        synchronized (syncAccs.syncAcc1) {
            synchronized (syncAccs.syncAcc2) {
                if (!doSimpleTransfer(from, to, amount).equals(TransactionStatus.NEED_CHECK)) {
                    return;
                }
            }
        }
        executors.submit(new IsFraud(amount, from, to, syncAccs));
    }

    private TransactionStatus doSimpleTransfer(Account from, Account to, BigDecimal amount) {
        if (from.isBlocked() || to.isBlocked()) {
            log.info("The transfer cannot be completed. One of the accounts is blocked");
            return TransactionStatus.BLOCKED;
        }
        if ((from.getMoney().subtract(from.getMoneyInCheckCache())).compareTo(amount) < 0) {
            log.info("Insufficient funds for transfer");
            return TransactionStatus.BLOCKED;
        }
        if (amount.compareTo(MINIMUM_AMOUNT_TO_SEND_FOR_VERIFICATION) < 0) {
            Session session = SessionFactoryUtil.getSessionFactory().openSession();
            session.lock(from, LockMode.PESSIMISTIC_WRITE);
            session.lock(to, LockMode.PESSIMISTIC_WRITE);
            Transaction transaction = session.beginTransaction();
            from.setMoney(from.getMoney().subtract(amount));
            to.setMoney(to.getMoney().add(amount));
            transaction.commit();
            log.info(String.format("Money sent from %s to %s: %.2f у.е.",
                    from.getAcc_number(), to.getAcc_number(), amount.doubleValue()));
            return TransactionStatus.COMMITED;
        }
        if (from.isChecking() || to.isChecking()) {
            log.info("We need to check this transaction but one of the account is already on verification. Please try later");
            return TransactionStatus.CHECKING_ABORTED;
        }
        from.setMoneyInCheckCache(from.getMoneyInCheckCache().add(amount)); // резервируем средства для проверки в службе безопасности
        from.setChecking(true);
        to.setChecking(true);
        log.info("Sending transaction to verification. Please, wait...");
        return TransactionStatus.NEED_CHECK;
    }

    public BigDecimal getBalance(Integer accountNum) {
        Account account = getAccountFromBaseOrCache(accountNum);
        return account.getMoney();
    }

    private Account getAccountFromBaseOrCache(Integer accNum) {
        Account result;
        if ((result = accounts.get(accNum)) == null) {
            synchronized (lock) {
                if ((result = accounts.get(accNum)) == null) {
                    Session session = SessionFactoryUtil.getSessionFactory().openSession();
                    result = session.get(Account.class, accNum);
                    result.setMoney(result.getMoney().setScale(2, RoundingMode.HALF_UP));
                    accounts.put(result.getAcc_number(), result);
                    session.close();
                }
            }
        }
        return result;
    }

    public Transfer getRunnableTransfer(Integer fromAccountNum, Integer toAccountNum, double amount) {
        return this.new Transfer(fromAccountNum, toAccountNum, amount);
    }

    private enum TransactionStatus {
        BLOCKED, COMMITED, NEED_CHECK, CHECKING_ABORTED
    }


    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private class Transfer implements Runnable {
        Integer fromAccountNum;
        Integer toAccountNum;
        double amount;

        @Override
        public void run() {
            transfer(fromAccountNum, toAccountNum, amount);
        }
    }

    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private class IsFraud implements Runnable {
        BigDecimal amount;
        Account from;
        Account to;
        SyncAccs syncAccs;

        @Override
        public void run() {
            try {
                if (isFraud(String.valueOf(from.getAcc_number()),
                        String.valueOf(to.getAcc_number()),
                        amount.longValue())) {
                    synchronized (syncAccs.syncAcc1) {
                        synchronized (syncAccs.syncAcc2) {
                            Session session = SessionFactoryUtil.getSessionFactory().openSession();
                            try (session) {
                                Transaction transaction = session.beginTransaction();
                                session.lock(from, LockMode.PESSIMISTIC_WRITE);
                                session.lock(to, LockMode.PESSIMISTIC_WRITE);
                                from.setMoneyInCheckCache(from.getMoneyInCheckCache().subtract(amount));
                                from.setChecking(false);
                                to.setChecking(false);
                                from.setBlocked(true);
                                to.setBlocked(true);
                                transaction.commit();
                            }
                        }
                    }
                    log.info("The operation did not pass the security check. Accounts are blocked");
                    return;
                }
                synchronized (syncAccs.syncAcc1) {
                    synchronized (syncAccs.syncAcc2) {
                        Session session = SessionFactoryUtil.getSessionFactory().openSession();
                        try (session) {
                            Transaction transaction1 = session.beginTransaction();
                            session.lock(from, LockMode.PESSIMISTIC_WRITE);
                            session.lock(to, LockMode.PESSIMISTIC_WRITE);
                            from.setMoneyInCheckCache(from.getMoneyInCheckCache().subtract(amount));
                            from.setChecking(false);
                            to.setChecking(false);
                            from.setMoney(from.getMoney().subtract(amount));
                            to.setMoney(to.getMoney().add(amount));
                            transaction1.commit();
                        }
                    }
                }
                log.info(String.format("Money sent from %s to %s: %.2f у.е.",
                        from.getAcc_number(), to.getAcc_number(), amount.doubleValue()));
            } catch (InterruptedException e) {
                synchronized (from) {
                    from.setMoneyInCheckCache(from.getMoneyInCheckCache().subtract(amount));
                }
                log.error(e);
            }
        }
    }

    private class SyncAccs {
        final Account syncAcc1;
        final Account syncAcc2;

        SyncAccs(Account from, Account to) {
            syncAcc1 = from.compareTo(to) < 0 ? from : to;
            syncAcc2 = from.compareTo(to) < 0 ? to : from;
        }
    }
}


