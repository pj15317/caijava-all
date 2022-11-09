package cn.caijava.core.util.kafka;

import lombok.SneakyThrows;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.ListConsumerGroupsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.common.KafkaFuture;

import java.util.Collection;
import java.util.Properties;

public class ListAllKafkaTopic {

    @SneakyThrows
    public void list_topic() {
        Properties props = new Properties();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "10.112.37.50:9092,10.112.37.158:9092,10.112.37.140:9092");
        AdminClient client = AdminClient.create(props);
        ListTopicsResult topics = client.listTopics();

        topics.names().get().forEach(System.out::println);
    }

    @SneakyThrows
    public void list_group() {
        Properties props = new Properties();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "10.112.37.50:9092,10.112.37.158:9092,10.112.37.140:9092");
        AdminClient client = AdminClient.create(props);
        ListConsumerGroupsResult listConsumerGroupsResult = client.listConsumerGroups();

        KafkaFuture<Collection<ConsumerGroupListing>> all = listConsumerGroupsResult.all();
        Collection<ConsumerGroupListing> groups = all.get();
        groups.forEach(group -> {
            System.err.println(group.groupId());
        });
    }

}
