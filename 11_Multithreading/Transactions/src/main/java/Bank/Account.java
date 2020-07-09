package Bank;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.Data;

@Data
public class Account implements Comparable<Account> {

  private Integer acc_number;
  private BigDecimal money;
  private boolean isBlocked;

  Account(Integer acc_number, double money) {
    this.acc_number = acc_number;
    this.money = BigDecimal.valueOf(money).setScale(2, RoundingMode.HALF_UP);
  }

  public synchronized BigDecimal getMoney() {
    return money;
  }

  public synchronized void addMoney(BigDecimal money) {
    this.money = this.money.add(money);
  }

  public synchronized void withdrawMoney(BigDecimal money) {
    this.money = this.money.subtract(money);
  }

  public synchronized boolean isBlocked() {
    return isBlocked;
  }

  public synchronized void setBlocked(boolean blocked) {
    isBlocked = blocked;
  }

  @Override
  public int compareTo(Account o) {
    return Integer.compare(this.getAcc_number(), o.getAcc_number());
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
