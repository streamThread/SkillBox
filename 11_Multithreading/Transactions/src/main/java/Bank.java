import lombok.extern.log4j.Log4j2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Log4j2
public class Bank {
    private final Random random = new Random();
    private Map<String, Account> accounts = Collections.synchronizedMap(new HashMap<>());

    public synchronized boolean isFraud(String fromAccountNum, String toAccountNum, long amount)
            throws InterruptedException {
        Thread.sleep(1000);
        return random.nextBoolean();
    }

    public boolean transfer(String fromAccountNum, String toAccountNum, long amount) throws InterruptedException {
        Account from = accounts.get(fromAccountNum);
        Account to = accounts.get(toAccountNum);
        synchronized (from) {
            synchronized (to) {
                if (from.isBlocked() || to.isBlocked()) {
                    log.info("The transfer cannot be completed. One of the accounts is blocked");
                    return false;
                }
                if (from.getMoney() < amount) {
                    log.info("Insufficient funds for transfer");
                    return false;
                }
                if (amount > 50000 && isFraud(fromAccountNum, toAccountNum, amount)) {
                    from.setBlocked(true);
                    to.setBlocked(true);
                    log.info("The operation did not pass the security check. Accounts are blocked");
                    return false;
                }
                from.setMoney(from.getMoney() - amount);
                to.setMoney(to.getMoney() + amount);
                log.info("Money sent");
                return true;
            }
        }
    }

    public long getBalance(String accountNum) {
        Account account = accounts.get(accountNum);
        return account.getMoney();
    }
}
