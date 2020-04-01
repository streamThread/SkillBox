import lombok.*;

@Data
public class Account {

    @NonNull
    private final String ACC_NUMBER;
    private volatile long money;
    private boolean isBlocked;

    Account(@NonNull String ACC_NUMBER, long money) {
        this.ACC_NUMBER = ACC_NUMBER;
        this.money = money;
    }
}
