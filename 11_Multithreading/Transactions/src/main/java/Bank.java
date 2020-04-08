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

@Log4j2
public class Bank {

    Random random = new Random();
    private ConcurrentReferenceHashMap<Integer, Account> accounts = new ConcurrentReferenceHashMap<>();

    public ConcurrentReferenceHashMap<Integer, Account> getAccounts() {
        return accounts;
    }

    private synchronized boolean isFraud(String fromAccountNum, String toAccountNum, long amount)
            throws InterruptedException {
        Thread.sleep(1000);
        return random.nextBoolean();
    }

    //при разработке исходим из того, что служба безопасности (СБ) одна и обращаемся к ней только в крайнем случае
    public void transfer(Integer fromAccountNum, Integer toAccountNum, double amountIn) {

        if (fromAccountNum.equals(toAccountNum)) {
            throw new IllegalArgumentException("The sender and recipient accounts must be different");
        }

        Session session = SessionFactoryUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        Account from = getAccountFromBaseOrCache(fromAccountNum, session);
        Account to = getAccountFromBaseOrCache(toAccountNum, session);

        Account syncAcc1 = from.compareTo(to) < 0 ? from : to;
        Account syncAcc2 = from.compareTo(to) < 0 ? to : from;

        BigDecimal amount = BigDecimal.valueOf(amountIn).setScale(2, RoundingMode.HALF_UP);

        try (session) {
            synchronized (syncAcc1) {
                synchronized (syncAcc2) {
                    if (!doSimpleTransfer(from, to, amount, transaction).equals(TransactionStatus.NEED_CHECK)) {
                        return;
                    }
                }
            }
//      если требуется проверка СБ, то снимаем блокировку c аккаунтов, чтобы можно было совершать по ним другие операции
//      синхронизируемся по банку
            synchronized (this) {
//     если аккаунт был заблокирован во время ожидания потока в очереди, разблокируем деньги
//     и закрываем метод. (экономия времени достигается путем завершения работы до отправки
//     операции на проверку в СБ)
                if (from.isBlocked() || to.isBlocked()) {
                    synchronized (from) {
                        from.setMoneyInCheckCache(from.getMoneyInCheckCache().subtract(amount));
                    }
                    log.info("The operation did not pass the security check. Accounts are blocked");
                    return;
                }
                // по условию метод разработан кем-то другим, оставляем как есть
                if (isFraud(String.valueOf(fromAccountNum),
                        String.valueOf(toAccountNum),
                        amount.longValue())) {
                    synchronized (syncAcc1) {
                        synchronized (syncAcc2) {
                            from.setMoneyInCheckCache(from.getMoneyInCheckCache().subtract(amount));
                            from.setBlocked(true);
                            to.setBlocked(true);
                            transaction.commit();
                        }
                    }
                    log.info("The operation did not pass the security check. Accounts are blocked");//
                    return;
                }
            }
            synchronized (syncAcc1) {
                synchronized (syncAcc2) {
                    from.setMoneyInCheckCache(from.getMoneyInCheckCache().subtract(amount));
                    from.setMoney(from.getMoney().subtract(amount));
                    to.setMoney(to.getMoney().add(amount));
                    transaction.commit();
                }
            }
            log.info(String.format("Money sent from %s to %s: %.2f у.е.",
                    fromAccountNum, toAccountNum, amount.doubleValue()));
        } catch (InterruptedException e) {
            synchronized (from) {
                from.setMoneyInCheckCache(from.getMoneyInCheckCache().subtract(amount));
            }
            log.error("isFraud() InterruptedException", e);
        }
    }

    private TransactionStatus doSimpleTransfer(Account from, Account to, BigDecimal amount, Transaction transaction) {
        if (from.isBlocked() || to.isBlocked()) {
            log.info("The transfer cannot be completed. One of the accounts is blocked");
            return TransactionStatus.BLOCKED;
        }
        //проверяем наличие средств на счете с учетом тех, что находятся на проверке в СБ
        if ((from.getMoney().subtract(from.getMoneyInCheckCache())).compareTo(amount) < 0) {
            log.info("Insufficient funds for transfer");
            return TransactionStatus.BLOCKED;
        }

        if (amount.compareTo(new BigDecimal(50000)) < 0) {
            from.setMoney(from.getMoney().subtract(amount));
            to.setMoney(to.getMoney().add(amount));
            transaction.commit();
            log.info(String.format("Money sent from %s to %s: %.2f у.е.",
                    from.getAcc_number(), to.getAcc_number(), amount.doubleValue()));
            return TransactionStatus.COMMITED;
        }
        from.setMoneyInCheckCache(from.getMoneyInCheckCache().add(amount)); // резервируем средства для проверки в службе безопасности
        return TransactionStatus.NEED_CHECK;
    }

    public BigDecimal getBalance(Integer accountNum, Session session) {
        Account account = getAccountFromBaseOrCache(accountNum, session);
        return account.getMoney();
    }

    private Account getAccountFromBaseOrCache(Integer accNum, Session session) {
        Account result;
        if ((result = accounts.get(accNum)) == null) {
            synchronized (this) {
                if ((result = accounts.get(accNum)) == null) {
                    result = session.get(Account.class, accNum);
                    result.setMoney(result.getMoney().setScale(2, RoundingMode.HALF_UP));
                    accounts.put(result.getAcc_number(), result);
                }
            }
        }
        session.lock(result, LockMode.READ);
        return result;
    }

    public Transfer getRunnableTransfer(Integer fromAccountNum, Integer toAccountNum, double amount) {
        return this.new Transfer(fromAccountNum, toAccountNum, amount);
    }

    private enum TransactionStatus {
        BLOCKED, COMMITED, NEED_CHECK
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
}


