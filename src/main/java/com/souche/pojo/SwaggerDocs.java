package com.souche.pojo;

import lombok.Data;

import java.util.List;

/**
 * SwaggerDocs
 *
 * @author huchao
 */
@Data
public class SwaggerDocs {

    /**
     * api版本
     */
    private String apiVersion;
    /**
     * apis
     */
    private List<Apis> apis;
    /**
     * authorizations
     */
    private Authorizations authorizations;
    /**
     * info
     */
    private Info info;
    /**
     * swaggerVersion
     */
    private String swaggerVersion;

}