package cps.demo.jaegertracingprovider.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author dienvt
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProviderDeliverRequest {

    private String orderId;
}
