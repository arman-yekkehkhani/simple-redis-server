import org.example.redis.RedisServer;
import org.junit.jupiter.api.Test;

public class RedisServerTest {

    @Test
    public void completeTest() throws InterruptedException {
        RedisServer redis = RedisServer.getInstance();

        assert redis.getDbIdx() == 0;

        redis.setDbIdx(2);
        assert redis.getDbIdx() == 2;

        redis.put("k1", "v1");
        assert redis.get("k1").equals("v1");
        assert redis.get("k2") == null;

        assert redis.delete("k1", "k2") == 1;

        redis.put("k1", "v1");
        redis.put("k2", "v2");
        redis.put("k3", "v3");
        assert redis.countExistingKeys("k1", "k3", "k4") == 2;

        assert redis.getKeys("k.*").equals(" \n" +
                "k3\n" +
                "k2\n" +
                "k1");

        redis.put("k4", "5");
        redis.increment("k4");
        assert Integer.parseInt(redis.get("k4")) == 6;

        redis.put("k5", "5");
        redis.incrementBy("k5", 5L);
        assert Integer.parseInt(redis.get("k5")) == 10;

        redis.put("k6", "v6");
        redis.setExpire("k6", 2);
        assert redis.get("k6").equals("v6");
        assert (redis.remainingTime("k6") - 2) < 0.1;
        Thread.sleep(2100);
        assert redis.get("k6") == null;
    }
}
