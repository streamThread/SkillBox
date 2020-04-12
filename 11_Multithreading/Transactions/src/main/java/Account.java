import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Entity
@NoArgsConstructor
public class Account implements Comparable {

    @NonNull
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer acc_number;
    volatile private BigDecimal money;
    volatile private boolean isBlocked;
    @Transient
    volatile private BigDecimal moneyInCheckCache = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
    @Transient
    volatile private boolean isChecking;

    Account(double money) {
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

    public synchronized BigDecimal getMoneyInCheckCache() {
        return moneyInCheckCache;
    }

    public synchronized void setMoneyInCheckCache(BigDecimal moneyInCheckCache) {
        this.moneyInCheckCache = moneyInCheckCache;
    }

    public synchronized boolean isChecking() {
        return isChecking;
    }

    public synchronized void setChecking(boolean checking) {
        isChecking = checking;
    }

    @Override
    public int compareTo(Object o) {
        Account account = (Account) o;
        return Integer.compare(this.getAcc_number(), account.getAcc_number());
    }
}
