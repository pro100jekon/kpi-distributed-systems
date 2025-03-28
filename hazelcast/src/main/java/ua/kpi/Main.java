package ua.kpi;

import com.hazelcast.collection.IQueue;
import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.util.Arrays;
import java.util.Optional;

import static ua.kpi.LockTaskType.*;

public class Main {

    public static final String CLUSTER_NAME = "lab2-cluster";

    private static HazelcastInstance createInstance(String instancesToDiscover) {
        var config = new Config()
                .addMapConfig(new MapConfig("map")
                                .setInMemoryFormat(MapConfig.DEFAULT_IN_MEMORY_FORMAT)
                        //.setBackupCount(3)
                        //.setAsyncBackupCount(3)
                )
                .addQueueConfig(new QueueConfig("queue")
                        .setMaxSize(10)
                        .setQueueStoreConfig(new QueueStoreConfig()
                                .setEnabled(true)
                                .setClassName("com.hazelcast.QueueStoreImpl")
                                .setProperty(QueueStoreConfig.STORE_BINARY, "false")
                                .setProperty(QueueStoreConfig.STORE_MEMORY_LIMIT, "100")
                                .setProperty(QueueStoreConfig.STORE_BULK_LOAD, "50")))
                .setClusterName(CLUSTER_NAME);
        if (instancesToDiscover != null) {
            config.setNetworkConfig(new NetworkConfig()
                    .setPort(5701)
                    .setJoin(new JoinConfig()
                            .setTcpIpConfig(new TcpIpConfig()
                                    .setEnabled(true)
                                    .setMembers(Arrays.stream(instancesToDiscover.split(","))
                                            .map(host -> host + ":5701").toList()))));
        }
        return Hazelcast.newHazelcastInstance(config);
    }

    /*public static void main(String[] args) {
        var instances = new ArrayList<>(List.of(createInstance(1998), createInstance(1999), createInstance(2000)));

        Collections.shuffle(instances);
        System.out.printf("Using Hazelcast on port (%d)%n", instances.getFirst().getConfig().getNetworkConfig().getPort());
        IMap<String, String> map = instances.getFirst().getMap(MAP_NAME);
        for (int i = 0; i < 1000; i++) {
            map.put(Integer.toString(i), "Value_%d".formatted(i));
        }
    }*/

    public static void main(String[] args) throws InterruptedException { // for parallel disabling
        var hazelcast = Main.createInstance(System.getenv("INSTANCES"));
        if (System.getenv("LOCK_TYPE") != null) {
            long start = 0, end = 0, last = 0;
            sync(hazelcast, "start");
            switch (System.getenv("LOCK_TYPE")) {
                case NONE -> {
                    start = System.currentTimeMillis();
                    last = noLockTest(hazelcast);
                    end = System.currentTimeMillis();
                }
                case PESSIMISTIC_LOCK -> {
                    start = System.currentTimeMillis();
                    last = pessimisticLockTest(hazelcast);
                    end = System.currentTimeMillis();
                }
                case OPTIMISTIC_LOCK -> {
                    start = System.currentTimeMillis();
                    last = optimisticLockTest(hazelcast);
                    end = System.currentTimeMillis();
                }
            }
            sync(hazelcast, "finish");
            System.out.printf("""
                            Task %s took %d ms.
                            Last logged value: %d
                            Current value: %d
                            """, System.getenv("LOCK_TYPE"),
                    end - start,
                    last,
                    Long.parseLong(hazelcast.getMap("map").get("key").toString()));
        } else if (System.getenv("FILL_MAP") != null) {
            IMap<String, String> map = hazelcast.getMap("map");
            for (int i = 0; i < 1000; i++) {
                map.put(Integer.toString(i), "Value_%d".formatted(i));
            }
        } else if (System.getenv("QUEUE") != null) {
            sync(hazelcast, "start");
            if (System.getenv("QUEUE").equals("READ")) {
                read(hazelcast);
            } else {
                write(hazelcast);
            }
        }
    }

    private static void sync(HazelcastInstance hazelcast, String mode) throws InterruptedException {
        var toCheck = mode.equals("start")
                ? Optional.ofNullable(System.getenv("INSTANCES"))
                    .map(str -> str.split(","))
                    .orElse(new String[0]).length + 1
                : 0;
        var toAdd = mode.equals("start") ? 1 : -1;
        var map = hazelcast.<String, Long>getMap("map");
        map.lock("glock");
        map.computeIfPresent("glock", (_, v) -> v + toAdd);
        map.putIfAbsent("glock", 1L);
        map.unlock("glock");
        while (true) {
            map.lock("glock");
            if (map.get("glock") != toCheck) {
                Thread.sleep(100);
                map.unlock("glock");
            } else {
                map.unlock("glock");
                break;
            }
        }
    }

    static long noLockTest(HazelcastInstance instance) {
        IMap<String, Long> map = instance.getMap("map");
        map.putIfAbsent("key", 0L);
        long last = 0;
        for (int i = 0; i < 10000; i++) {
            var value = map.get("key");
            value++;
            last = Optional.ofNullable(map.put("key", value)).orElse(-1L);
        }
        return last;
    }

    static long pessimisticLockTest(HazelcastInstance instance) {
        IMap<String, Long> map = instance.getMap("map");
        map.putIfAbsent("key", 0L);
        long last = 0;
        for (int i = 0; i < 10000; i++) {
            try {
                map.lock("key");
                var value = map.get("key");
                last = map.put("key", value + 1);
            } finally {
                map.unlock("key");
            }
        }
        return last;
    }

    static long optimisticLockTest(HazelcastInstance instance) {
        IMap<String, Long> map = instance.getMap("map");
        map.putIfAbsent("key", 0L);
        long last = 0;
        for (int i = 0; i < 10000; i++) {
            while (true) {
                var value = map.get("key");
                if (map.replace("key", value, value + 1)) {
                    break;
                }
                last = value;
            }
        }
        return last;
    }

    static void read(HazelcastInstance instance) throws InterruptedException {
        IQueue<Integer> queue = instance.getQueue("queue");
        int i = 0;
        while (true) {
            var took = queue.take();
            System.out.print(" Took: " + took);
            i++;
            if (i != 0 && i % 6 == 0) System.out.println();
            if (took == -1) {
                queue.put(-1);
                break;
            }
        }
    }

    static void write(HazelcastInstance instance) throws InterruptedException {
        System.out.println("About to write...");
        IQueue<Integer> queue = instance.getQueue("queue");
        for (int i = 0; i < 100; i++) {
            queue.put(i);
            System.out.printf("Size after writing %d: %d%n", i, queue.size());
        }
        queue.put(-1);
        System.out.println("Finished writing...");
    }
}