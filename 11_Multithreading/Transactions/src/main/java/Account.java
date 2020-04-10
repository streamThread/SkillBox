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
    private BigDecimal money;
    private boolean isBlocked;
    @Transient
    private BigDecimal moneyInCheckCache = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
    @Transient
    private boolean isChecking;

    Account(double money) {
        this.money = BigDecimal.valueOf(money).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public int compareTo(Object o) {
        Account account = (Account) o;
        return Integer.compare(this.getAcc_number(), account.getAcc_number());
    }
}
