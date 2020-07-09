import static java.lang.System.out;

import java.util.Random;
import org.redisson.Redisson;
import org.redisson.api.RDeque;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import org.redisson.config.Config;

public class RedisStorage {

  private final static String KEY = "REGISTERED_USERS";
  private RedissonClient redisson;
  private RKeys rKeys;
  private RDeque<User> registeredUsers;

  void init() {
    Config config = new Config();
    config.useSingleServer().setAddress("redis://127.0.0.1:6379");
    try {
      redisson = Redisson.create(config);
    } catch (RedisConnectionException Exc) {
      out.println("Не удалось подключиться к Redis");
      out.println(Exc.getMessage());
    }
    rKeys = redisson.getKeys();
    registeredUsers = redisson.getDeque(KEY);
    rKeys.delete(KEY);
  }

  void doFakeRegistration() {
    for (long i = 1; i < 21; i++) {
      registeredUsers.add(new User(i));
    }
  }

  User getUser() {
    User user = registeredUsers.poll();
    registeredUsers.add(user);
    return user;
  }

  User getRandomUser() {
    Long randomLong = new Random().longs(1L, (registeredUsers.size() + 1L))
        .findFirst().getAsLong();
    User user = registeredUsers.stream()
        .filter(u -> u.getId().equals(randomLong)).findFirst().get();
    registeredUsers.remove(user);
    registeredUsers.addFirst(user);
    return user;
  }

  void shutdown() {
    redisson.shutdown();
  }
}
