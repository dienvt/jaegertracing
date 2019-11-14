package cps.demo.jaegertracingservice.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author dienvt
 */
@Data
@Builder
public class ResourceEntity {

    private String env;
    private List<String> service;
}
