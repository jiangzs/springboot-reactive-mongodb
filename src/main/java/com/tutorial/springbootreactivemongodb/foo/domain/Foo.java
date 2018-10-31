package com.tutorial.springbootreactivemongodb.foo.domain;


import com.tutorial.springbootreactivemongodb.foo.SSE;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "foo")
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Foo implements SSE {

    @Id
    private String id;

    @Indexed(unique = true)
    private String owner;
    private Double value;

    private String name;
    private String phone;
    private Date birthday;
}
