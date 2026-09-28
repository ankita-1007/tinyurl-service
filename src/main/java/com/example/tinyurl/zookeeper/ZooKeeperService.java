package com.example.tinyurl.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Service;

@Service
public class ZooKeeperService {

    private static final String COUNTER_PATH = "/tinyurl/counter";
    private static final int RANGE_SIZE = 3;


    private final CuratorFramework client;

    public ZooKeeperService(CuratorFramework client) {
        this.client = client;
    }

    public long getCounter() throws Exception {

        Stat stat = new Stat();

        byte[] data = client
                .checkExists()
                .forPath(COUNTER_PATH) != null
                ? client.getData()
                .storingStatIn(stat)
                .forPath(COUNTER_PATH)
                : null;

        if (data == null) {
            createCounter();
            return 0;
        }

        return Long.parseLong(new String(data));
    }

    private void createCounter() throws Exception {

        if (client.checkExists().forPath(COUNTER_PATH) == null) {

            try {
                client.create()
                        .creatingParentsIfNeeded()
                        .forPath(COUNTER_PATH, "0".getBytes());
            } catch (KeeperException.NodeExistsException e) {
                // Another instance created it
            }
        }
    }

    public long incrementAndGet() throws Exception {

        createCounter();

        while (true) {

            Stat stat = new Stat();

            byte[] data = client
                    .getData()
                    .storingStatIn(stat)
                    .forPath(COUNTER_PATH);

            long currentValue = Long.parseLong(new String(data));

            long newValue = currentValue + 1;

            try {

                client.setData()
                        .withVersion(stat.getVersion())
                        .forPath(
                                COUNTER_PATH,
                                String.valueOf(newValue).getBytes()
                        );

                return newValue;

            } catch (org.apache.zookeeper.KeeperException.BadVersionException e) {
                // Someone changed the counter.
                // Read again and retry.
            }
        }
    }

    public long[] allocateRange() throws Exception {

        createCounter();

        while (true) {

            Stat stat = new Stat();

            byte[] data = client.getData()
                    .storingStatIn(stat)
                    .forPath(COUNTER_PATH);

            long currentValue =
                    Long.parseLong(new String(data));

            long newValue =
                    currentValue + RANGE_SIZE;

            try {

                client.setData()
                        .withVersion(stat.getVersion())
                        .forPath(
                                COUNTER_PATH,
                                String.valueOf(newValue).getBytes()
                        );

                long start = currentValue;
                long end = newValue-1;

                return new long[]{start, end};

            } catch (KeeperException.BadVersionException e) {
                // Someone else updated the counter.
                // Try again.
            }
        }
    }
}