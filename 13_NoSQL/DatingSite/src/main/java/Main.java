import java.util.Random;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        RedisStorage redis = new RedisStorage();
        redis.init();
        redis.doFakeRegistration();
        while (true) {
            int rand = new Random().nextInt(10);
            for (int i = 0; i < 10; i++) {
                if (i == rand) {
                    System.out.printf("> Пользователь %d оплатил платную услугу \r\n", redis.getRandomUser().getId());
                }
                System.out.printf("— На главной странице показываем пользователя %d \r\n", redis.getUser().getId());
            }
            Thread.sleep(1000);
        }
    }
}

