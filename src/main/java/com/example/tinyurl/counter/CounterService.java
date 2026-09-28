package com.example.tinyurl.counter;

import com.example.tinyurl.zookeeper.ZooKeeperService;
import org.springframework.stereotype.Service;

@Service
public class CounterService {

    private final ZooKeeperService zooKeeperService;

    private long currentId = 0;
    private long maxId = -1;

    public CounterService(ZooKeeperService zooKeeperService) {
        this.zooKeeperService = zooKeeperService;
    }

    public synchronized long getNextId() {

        try {

            if (currentId > maxId) {

                long[] range =
                        zooKeeperService.allocateRange();

                currentId = range[0];
                maxId = range[1];

                System.out.println(
                        "Allocated ID range: "
                                + currentId
                                + " - "
                                + maxId
                );
            }

            return currentId++;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to generate ID",
                    e
            );
        }
    }
}