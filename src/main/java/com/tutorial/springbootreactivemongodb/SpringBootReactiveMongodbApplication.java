package com.tutorial.springbootreactivemongodb;

import com.tutorial.springbootreactivemongodb.foo.SSE;
import io.prometheus.client.spring.web.EnablePrometheusTiming;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.TopicProcessor;
import reactor.util.concurrent.Queues;

@Slf4j
@EnablePrometheusTiming
@EnableScheduling
@EnableRabbit
@RestController
@SpringBootApplication
public class SpringBootReactiveMongodbApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactiveMongodbApplication.class, args);
    }


    @GetMapping(value = "/rabbitmq", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<SSE>> findAll() {
        return Flux.merge( getRabbitMQStream(), HeartBeat.getHeartBeatStream(10));
//        return this.rabbitmqSseFluxProcessor;
//        return getHeartBeatStream();
    }

    private Flux<ServerSentEvent<SSE>> getRabbitMQStream() {
        return this.rabbitmqSseFluxProcessor;
    }


    private TopicProcessor<ServerSentEvent<SSE>> rabbitmqSseFluxProcessor = TopicProcessor.share("rabbitmq", Queues.SMALL_BUFFER_SIZE);

    @RabbitListener(queues = "test")
    public void handleAmqpMessages(byte[] message) {
        final String v = new String(message);
        log.info("msg {}", v);
        this.rabbitmqSseFluxProcessor.onNext(ServerSentEvent.<SSE>builder()
                .event("mq")
                .data(MQSse.builder().msg(v).build())
                .build());

    }


}
