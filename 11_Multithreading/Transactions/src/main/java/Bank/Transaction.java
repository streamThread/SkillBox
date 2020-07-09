package Bank;

import java.math.BigDecimal;
import lombok.Value;

@Value
public class Transaction {

  Account from;
  Account to;
  BigDecimal amount;
}
