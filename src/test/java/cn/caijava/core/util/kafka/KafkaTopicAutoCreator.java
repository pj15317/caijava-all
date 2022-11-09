package cn.caijava.core.util.kafka;//package com.faw_qm.erpcg.cop;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

// 处理控制台错误: topic is/are not present and missingTopicsFatal is true
// org.springframework.kafka.core.KafkaAdmin.afterSingletonsInstantiated
// /opt/kafka_2.13-2.7.1/bin/kafka-consumer-groups.sh --bootstrap-server 10.112.37.50:9092 --describe --group bdm_group1
@Slf4j
@Component
@Deprecated // use KafkaAdmin#afterSingletonsInstantiated
public class KafkaTopicAutoCreator implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${pcp408.topic.save:pcp408_topic_save_test}")
    private String topic1;

    @Value("${pcp408.topic:pcp408_topic_test}")
    private String topic2;

    @SneakyThrows
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        Properties props = new Properties();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        AdminClient adminClient = AdminClient.create(props);

        ListTopicsResult listTopics = adminClient.listTopics();

        // 需要创建的topic列表
        List<String> topics = Lists.newArrayList(topic1, topic2);
        // 当前存在的topic名字
        Set<String> names = listTopics.names().get();
        // 需要创建的topic
        List<NewTopic> topicList = new ArrayList<>();

        for (String topic : topics) {
            boolean contains = names.contains(topic);
            if (!contains) {
                Map<String, String> configs = new HashMap<>();
                int partitions = 5;
                short replication = 1;
                NewTopic newTopic = new NewTopic(topic, partitions, replication).configs(configs);
                topicList.add(newTopic);
            }
        }

        if (ObjectUtil.isNotEmpty(topicList)) {
            String tps = topicList.stream()
                    .map(NewTopic::name)
                    .collect(Collectors.joining(","));
            adminClient.createTopics(topicList);
            log.debug("创建topic完成! topic:{}", tps);
        }

    }
}
