package com.tutorial.springbootreactivemongodb;

import com.tutorial.springbootreactivemongodb.foo.domain.Foo;
import com.tutorial.springbootreactivemongodb.foo.repo.FooRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Created by jiangzs@gmail.com on 2018/10/30.
 */
@RestController
@RequestMapping("/index")
@Slf4j
public class IndexCtrl {

    @Autowired
    FooRepo fooRepo;

    @GetMapping("/")
    public Mono<String> index(){
        for (int i = 0; i < 5; i++) {
            Foo foo = new Foo();
            foo.setId( String.valueOf(i));
            foo.setOwner("Owner" + i);
            foo.setValue(12.0 * i);
            fooRepo.save(foo);
        }

        return Mono.just("ok index");
    }

    @GetMapping("/{id}")
    public Mono<Foo> getFooById(@PathVariable("id") String id){
        return fooRepo.findById(id);
    }
}
