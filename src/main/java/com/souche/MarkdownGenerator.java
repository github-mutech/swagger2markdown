package com.souche;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.souche.pojo.Apis;
import com.souche.pojo.SwaggerDocs;
import com.souche.util.HttpClientUtil;
import com.souche.util.Markdown;
import com.souche.util.PropertiesUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MarkdownGenerator {

    public static void main(String[] args) throws IOException {
        generator();
    }

    private static void generator() throws IOException {
        String baseUrl = PropertiesUtil.getInstance().getStringValue("base.url");
        String apiDocsUrl = baseUrl.concat("/api-docs");
        SwaggerDocs swaggerDocs = JSON.parseObject(HttpClientUtil.doGet(apiDocsUrl), SwaggerDocs.class);
        for (Apis apis : swaggerDocs.getApis()) {
            JSONObject jsonObject = JSON.parseObject(HttpClientUtil.doGet(apiDocsUrl + apis.getPath()));
            JSONArray docsApis = jsonObject.getJSONArray("apis");
            JSONObject docsModels = jsonObject.getJSONObject("models");
            Markdown.Builder markdwonBuilder = new Markdown.Builder();
            markdwonBuilder.filePath(System.getProperty("user.dir").concat(File.separator).concat("docs").concat(File.separator));
            markdwonBuilder.fileName(apis.getDescription().concat(".md"));
            markdwonBuilder.h1(apis.getDescription());
            for (int i = 0, size = docsApis.size(); i < size; i++) {
                JSONObject docsApi = (JSONObject) docsApis.get(i);
                JSONObject operation = (JSONObject) docsApi.getJSONArray("operations").get(0);
                markdwonBuilder.h2((i + 1) + "." + operation.getString("summary"));
                markdwonBuilder.h3("请求路径");
                markdwonBuilder.code(baseUrl + docsApi.getString("path"));
                markdwonBuilder.h3("请求方式");
                markdwonBuilder.line(operation.getString("method"));
                markdwonBuilder.h3("请求类型");
                markdwonBuilder.line(operation.getString("consumes"));
                markdwonBuilder.h3("请求体");
                getRequest(markdwonBuilder, docsModels, operation);
                markdwonBuilder.h3("响应体");
                getResponse(markdwonBuilder, docsModels, operation);
            }
            markdwonBuilder.build().toFile();
        }
    }

    private static void getRequest(Markdown.Builder markdwonBuilder, JSONObject docsModels, JSONObject operation) {
        JSONArray parameters = operation.getJSONArray("parameters");
        if (parameters.size() == 0) {
            return;
        }
        markdwonBuilder.tableTr("请求类型", "字段名", "是否必填", "字段类型", "描述");
        List<String> requestTypes = new ArrayList<>();
        for (Object object : parameters) {
            JSONObject parameter = (JSONObject) object;
            String type = parameter.getString("type");
            String paramType = parameter.getString("paramType");
            String name = parameter.getString("name");
            String required = parameter.getString("required");
            String description = parameter.getString("description");
            if (type.equals("array")) {
                type = parameter.getJSONObject("items").getString("type");
                markdwonBuilder.tableTd(paramType, name, required, "[" + type + "]", description);
            } else {
                markdwonBuilder.tableTd(paramType, name, required, type, description);
            }
            if (docsModels.getJSONObject(type) != null) {
                requestTypes.add(type);
            }
        }
        getRequestDetail(markdwonBuilder, docsModels, requestTypes);
    }

    private static void getResponse(Markdown.Builder markdwonBuilder, JSONObject docsModels, JSONObject operation) {
        JSONArray responseMessages = operation.getJSONArray("responseMessages");
        for (Object object : responseMessages) {
            JSONObject responseMessage = (JSONObject) object;
            if (200 == responseMessage.getInteger("code")) {
                String responseModel = responseMessage.getString("responseModel");
                if (docsModels.getJSONObject(responseModel) != null) {
                    markdwonBuilder.h4(responseModel);
                    JSONObject properties = docsModels.getJSONObject(responseModel).getJSONObject("properties");
                    Set<String> keySet = properties.keySet();
                    markdwonBuilder.tableTr("字段名", "字段类型", "描述");
                    List<String> types = new ArrayList<>();
                    getTypes(markdwonBuilder, docsModels, properties, keySet, types);
                    if (types.size() > 0) {
                        getResponseDetail(markdwonBuilder, docsModels, types);
                    }
                }
            }
        }
    }

    private static void getTypes(Markdown.Builder markdwonBuilder, JSONObject docsModels, JSONObject properties, Set<String> keySet, List<String> types) {
        for (String key : keySet) {
            String type = properties.getJSONObject(key).getString("type");
            String description = properties.getJSONObject(key).getString("description");
            if (type.equals("array")) {
                type = properties.getJSONObject(key).getJSONObject("items").getString("type");
                markdwonBuilder.tableTd(key, "[" + type + "]", description);
            } else {
                markdwonBuilder.tableTd(key, type, description);
            }
            if (docsModels.getJSONObject(type) != null) {
                types.add(type);
            }
        }
    }

    private static void getRequestDetail(Markdown.Builder markdwonBuilder, JSONObject docsModels, List<String> types) {
        if (types.size() > 0) {
            for (String requestType : types) {
                markdwonBuilder.h4(requestType);
                markdwonBuilder.tableTr("字段名", "是否必填", "字段类型", "描述");
                JSONObject properties = docsModels.getJSONObject(requestType).getJSONObject("properties");
                List<String> tempTypes = new ArrayList<>();
                Set<String> keySet = properties.keySet();
                for (String key : keySet) {
                    String tempType = properties.getJSONObject(key).getString("type");
                    String description = properties.getJSONObject(key).getString("description");
                    String required = properties.getJSONObject(key).getString("required");
                    if (tempType.equals("array")) {
                        tempType = properties.getJSONObject(key).getJSONObject("items").getString("type");
                        markdwonBuilder.tableTd(key, required, "[" + tempType + "]", description);
                    } else {
                        markdwonBuilder.tableTd(key, required, tempType, description);
                    }
                    if (docsModels.getJSONObject(tempType) != null) {
                        tempTypes.add(tempType);
                    }
                }
                getRequestDetail(markdwonBuilder, docsModels, tempTypes);
            }
        }
    }

    private static void getResponseDetail(Markdown.Builder markdwonBuilder, JSONObject docsModels, List<String> types) {
        if (types.size() > 0) {
            for (String responseType : types) {
                markdwonBuilder.h4(responseType);
                markdwonBuilder.tableTr("字段名", "字段类型", "描述");
                JSONObject properties = docsModels.getJSONObject(responseType).getJSONObject("properties");
                List<String> tempTypes = new ArrayList<>();
                Set<String> keySet = properties.keySet();
                getTypes(markdwonBuilder, docsModels, properties, keySet, tempTypes);
                getResponseDetail(markdwonBuilder, docsModels, tempTypes);
            }
        }
    }
}
