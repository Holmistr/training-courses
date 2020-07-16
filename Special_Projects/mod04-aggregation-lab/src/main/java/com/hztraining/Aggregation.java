package com.hztraining;

import com.hazelcast.aggregation.Aggregators;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;
import com.hztraining.inv.Inventory;
import com.hztraining.inv.InventoryKey;

public class Aggregation {

    private HazelcastInstance hazelcastClient;


    public static void main(String[] args) {

        String configname = ConfigUtil.findConfigNameInArgs(args);
        ClientConfig config = ConfigUtil.getClientConfigForCluster(configname);

        Aggregation main = new Aggregation();
        main.hazelcastClient = HazelcastClient.newHazelcastClient(config);

        IMap<InventoryKey, Inventory> invmap = main.hazelcastClient.getMap("invmap");
        IMap<InventoryKey, Inventory> invmap_idx = main.hazelcastClient.getMap("invmap_indexed");

        // Aggregate total on hand across all locations
        String sku = "Item000123";
        // Note: Index is case-sensitive
        SqlPredicate predicate = new SqlPredicate("SKU=" + sku);
        long start = System.currentTimeMillis();
        long totalOnHand = invmap.aggregate(Aggregators.integerSum("quantity"), predicate);
        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("Total on hand for %s = %d (elapsed %d ms)\n", sku, totalOnHand, elapsed);

        start = System.currentTimeMillis();
        totalOnHand = invmap_idx.aggregate(Aggregators.integerSum("quantity"), predicate);
        elapsed = System.currentTimeMillis() - start;
        System.out.printf("Total on hand for %s = %d (elapsed %d ms using indexed map)\n", sku, totalOnHand, elapsed);

        main.hazelcastClient.shutdown();
    }
}
