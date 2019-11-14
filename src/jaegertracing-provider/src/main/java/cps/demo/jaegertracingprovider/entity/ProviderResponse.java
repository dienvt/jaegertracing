package cps.demo.jaegertracingprovider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author dienvt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderResponse {

    private int code;
}
