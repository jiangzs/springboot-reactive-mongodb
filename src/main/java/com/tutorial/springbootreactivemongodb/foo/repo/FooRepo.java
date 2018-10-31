package com.tutorial.springbootreactivemongodb.foo.repo;

import com.tutorial.springbootreactivemongodb.foo.domain.Foo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by jiangzs@gmail.com on 2018/10/29.
 */
public interface FooRepo extends ReactiveMongoRepository<Foo, String> {

    @Tailable
    Flux<Foo> findBy();

    Mono<Foo> findByOwner(String owner);

    Mono<Long> deleteByOwner(String owner);
}
