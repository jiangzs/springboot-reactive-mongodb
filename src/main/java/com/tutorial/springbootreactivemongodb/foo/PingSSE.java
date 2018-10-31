package com.tutorial.springbootreactivemongodb.foo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by jiangzs@gmail.com on 2018/10/31.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PingSSE implements SSE {
    private Long id;
    private String ping;
    private Date date;
}
