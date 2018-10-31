package com.tutorial.springbootreactivemongodb;

import com.tutorial.springbootreactivemongodb.foo.SSE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by jiangzs@gmail.com on 2018/10/31.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MQSse implements SSE {
    private String msg;
}
