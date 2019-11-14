package cps.demo.jaegertracingservice.entity;

import lombok.*;

/**
 * @author dienvt
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliverRequest {

    private String orderid;
}
