/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.sampler;

import static net.coru.kloadgen.util.ProducerKeysHelper.KAFKA_TOPIC_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.charithe.kafka.KafkaHelper;
import com.github.charithe.kafka.KafkaJunitExtension;
import com.github.charithe.kafka.KafkaJunitExtensionConfig;
import com.github.charithe.kafka.StartupMode;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KafkaJunitExtension.class)
@KafkaJunitExtensionConfig(startupMode = StartupMode.WAIT_FOR_STARTUP)
class ConsumerTest {

  @Test
  void testConsumer(KafkaHelper kafkaHelper) throws TimeoutException {
    KafkaConsumerSampler consumerSampler;

    consumerSampler = new KafkaConsumerSampler();
    Arguments consumerArguments = consumerSampler.getDefaultParameters();
    consumerArguments.removeArgument(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG);
    consumerArguments.addArgument(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                                  kafkaHelper.producerConfig().get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG).toString());
    consumerArguments.addArgument(GROUP_ID_CONFIG, "anonymous");
    consumerArguments.removeArgument(KAFKA_TOPIC_CONFIG);
    consumerArguments.addArgument(KAFKA_TOPIC_CONFIG, "testTopic");
    consumerArguments.removeArgument("timeout.millis");
    consumerArguments.addArgument("timeout.millis", "20000");
    consumerArguments.addArgument(VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization" +
            ".StringDeserializer");
    consumerArguments.addArgument(KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization" +
            ".StringDeserializer");
    consumerArguments.addArgument(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    JavaSamplerContext javaSamplerContext = new JavaSamplerContext(consumerArguments);

    LoopController loopController = new LoopController();
    loopController.setLoops(2);
    ThreadGroup tg = new ThreadGroup();
    tg.setSamplerController(loopController);
    javaSamplerContext.getJMeterContext().setThreadGroup(tg);

    consumerSampler.setupTest(javaSamplerContext);

    kafkaHelper.produce("testTopic", kafkaHelper.createStringProducer(), Map.of("key", "value"));
    SampleResult result = consumerSampler.runTest(javaSamplerContext);

    assertThat(result)
            .isNotNull()
            .hasFieldOrPropertyWithValue("success", true);
    assertThat(result.getResponseDataAsString()).isEqualToIgnoringCase("{ partition: 0, message: { key: key, " +
            "value: value }}");

    result = consumerSampler.runTest(javaSamplerContext);
    assertThat(result).isNull();

    /*result = consumerSampler.runTest(javaSamplerContext);
    assertThat(result).isNotNull().hasFieldOrPropertyWithValue("success",false);*/
  }
}

