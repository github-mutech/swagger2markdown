package com.souche.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author huchao
 */
@Data
public class JsonRootBean {

    private String apiVersion;
    private List<Apis> apis;
    private Authorizations authorizations;
    private Info info;
    private String swaggerVersion;

}