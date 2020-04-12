import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
public class SecurityService implements Runnable {

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
                if (BANK.isFraudPub(String.valueOf(from.getAcc_number()),
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


