package com.tutorial.springbootreactivemongodb.foo.service;

import com.tutorial.springbootreactivemongodb.foo.domain.Foo;
import com.tutorial.springbootreactivemongodb.foo.repo.FooRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Created by jiangzs@gmail.com on 2018/10/30.
 */
@Service
public class FooService {
    @Autowired
    private FooRepo fooRepo;

    public Mono<Foo> save(Foo foo) {
        return fooRepo.save(foo)
                .onErrorResume(e ->     // 1
                        fooRepo.findByOwner(foo.getOwner())   // 2
                                .flatMap(original -> {      // 4
                                    foo.setId(original.getId());
                                    return fooRepo.save(foo);   // 3
                                }));
    }

    public Mono<Long> deleteByOwner(String owner) {
        return fooRepo.deleteByOwner(owner);
    }

    public Mono<Foo> findByOwner(String owner) {
        return fooRepo.findByOwner(owner);
    }

    public Flux<Foo> findAll() {
        return fooRepo.findAll().delayElements(Duration.ofMillis(50));
    }

    public Flux<Foo> findBy() {
        return fooRepo.findBy();
    }
}
