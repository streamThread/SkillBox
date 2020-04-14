package Bank;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class Transaction {
    Account from;
    Account to;
    BigDecimal amount;
}
