package com.tutorial.springbootreactivemongodb;

import com.tutorial.springbootreactivemongodb.foo.PingSSE;
import com.tutorial.springbootreactivemongodb.foo.SSE;
import com.tutorial.springbootreactivemongodb.foo.domain.Foo;
import com.tutorial.springbootreactivemongodb.foo.service.FooService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

/**
 * Created by jiangzs@gmail.com on 2018/10/30.
 */
@RestController
@RequestMapping("/foo")
public class FooCtrl {

    @Autowired
    private FooService fooService;


    @PostMapping("")
    public Mono<Foo> save(Foo foo) {
        return this.fooService.save(foo);
    }

    @DeleteMapping("/{owner}")
    public Mono<Long> deleteByOwner(@PathVariable("owner") String owner) {
        return this.fooService.deleteByOwner(owner);
    }

    @GetMapping("/{owner}")
    public Mono<Foo> findByOwner(@PathVariable("owner") String owner) {
        return this.fooService.findByOwner(owner);
    }

    @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<SSE>> findAll() {
        return Flux.merge(getFooStream(), getHeartBeatStream());
//        return getHeartBeatStream();
    }

    private Flux<ServerSentEvent<SSE>> getFooStream() {
        return this.fooService.findAll()
                .log()
                .map(foo -> {
                    foo.setBirthday(new Date());
                    return foo;
                })
                .map(foo -> ServerSentEvent.<SSE>builder()
                        .event("foo")
                        .id(foo.getId())
                        .data(foo)
                        .build());
    }

    private Flux<ServerSentEvent<SSE>> getHeartBeatStream() {
        return Flux.interval(Duration.ofSeconds(1))
                .log()
                .map(i ->
                        ServerSentEvent.<SSE>builder()
                                .event("ping")
                                .data(PingSSE.builder().id(i).ping("pong").date(new Date()).build())
                                .build());

    }

    @GetMapping(value = "/by", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<ServerSentEvent<Foo>> findBy() {
        return this.fooService.findBy()
                .map(foo -> {
                    foo.setBirthday(new Date());
                    return foo;
                })
                .map(foo -> ServerSentEvent.<Foo>builder()
                        .event("foo")
                        .id(foo.getId())
                        .data(foo)
                        .build());
    }

    @GetMapping(value = "/te", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<SSE>> findAllA() {
        return  getFooStream();
//        return getHeartBeatStream();
    }
}
