import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
public class Account implements Comparable {

    private Integer acc_number;
    volatile private BigDecimal money;
    volatile private boolean isBlocked;

    Account(Integer acc_number, double money) {
        this.acc_number = acc_number;
        this.money = BigDecimal.valueOf(money).setScale(2, RoundingMode.HALF_UP);
    }

    public synchronized BigDecimal getMoney() {
        return money;
    }

    public synchronized void setMoney(BigDecimal money) {
        this.money = money;
    }

    public synchronized boolean isBlocked() {
        return isBlocked;
    }

    public synchronized void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    @Override
    public int compareTo(Object o) {
        Account account = (Account) o;
        return Integer.compare(this.getAcc_number(), account.getAcc_number());
    }

    static class SyncAccs {
        final Account syncAcc1;
        final Account syncAcc2;

        SyncAccs(Account from, Account to) {
            syncAcc1 = from.compareTo(to) < 0 ? from : to;
            syncAcc2 = from.compareTo(to) < 0 ? to : from;
        }
    }
}
