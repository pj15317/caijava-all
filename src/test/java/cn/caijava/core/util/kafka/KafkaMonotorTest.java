package cn.caijava.core.util.kafka;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.junit.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Properties;

public class KafkaMonotorTest {
    public static final String BROKERS_LOCAL = "127.0.0.1:9092";
    public static final String BROKERS_DEV = "10.133.76.103:9092,10.133.76.105:9092,10.133.76.150:9092";
    public static final String BROKERS_SIT = "10.112.37.50:9092,10.112.37.158:9092,10.112.37.140:9092";
    public static final String BROKERS_UAT = "10.124.27.225:9092,10.124.27.79:9092,10.124.27.9:9092";
    public static final String BROKERS_PROD = "10.120.70.42:9092,10.120.71.66:9092,10.120.71.213:9092";

    KafkaTemplate kafkaTemplate;
    @Test
    public void test_01() throws InterruptedException {
        Properties props = new Properties();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, BROKERS_LOCAL);
        AdminClient adminClient = AdminClient.create(props);
        kafkaTemplate.metrics();
    }
}
