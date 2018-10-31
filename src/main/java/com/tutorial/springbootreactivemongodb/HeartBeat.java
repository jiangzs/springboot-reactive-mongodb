package com.tutorial.springbootreactivemongodb;

import com.tutorial.springbootreactivemongodb.foo.PingSSE;
import com.tutorial.springbootreactivemongodb.foo.SSE;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Date;

/**
 * Created by jiangzs@gmail.com on 2018/10/31.
 */
public final class HeartBeat {

    public static Flux<ServerSentEvent<SSE>> getHeartBeatStream(final Integer interval) {
        return Flux.merge(getPingPong(), Flux.interval(Duration.ofSeconds(interval))
                .log()
                .map(i ->
                        ServerSentEvent.<SSE>builder()
                                .event("ping")
                                .data(PingSSE.builder().id(i).ping("pong").date(new Date()).build())
                                .build())
        );
    }

    private static Flux<ServerSentEvent<SSE>> getPingPong() {
        return Flux.just(ServerSentEvent.<SSE>builder()
                .event("ping")
                .data(PingSSE.builder().id(Long.valueOf(1)).ping("pong").date(new Date()).build())
                .build());
    }
}
