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

/**
 * @author huchao
 */
public class MarkdownGenerator {

    private final static String SWAGGER_ARRAY = "array";
    private static String baseUrl = PropertiesUtil.getInstance().getStringValue("base.url");
    private static String devUrl = PropertiesUtil.getInstance().getStringValue("dev.url");
    private static String srpUrl = PropertiesUtil.getInstance().getStringValue("srp.url");
    private static String username = PropertiesUtil.getInstance().getStringValue("username");
    private static String email = PropertiesUtil.getInstance().getStringValue("email");
    private static List<String> apiList = PropertiesUtil.getInstance().getListStringValue("api.list");

    public static void main(String[] args) throws IOException {
        new MarkdownGenerator().generator();
    }

    private void generator() throws IOException {
        boolean generatorAll = apiList.size() == 1 && "all".equals(apiList.get(0));
        String apiDocsUrl = baseUrl.concat("/api-docs");
        SwaggerDocs swaggerDocs = JSON.parseObject(HttpClientUtil.doGet(apiDocsUrl), SwaggerDocs.class);
        for (Apis apis : swaggerDocs.getApis()) {
            JSONObject jsonObject = JSON.parseObject(HttpClientUtil.doGet(apiDocsUrl + apis.getPath()));
            JSONArray docsApis = jsonObject.getJSONArray("apis");
            JSONObject docsModels = jsonObject.getJSONObject("models");
            Markdown.Builder markdwonBuilder = new Markdown.Builder();
            markdwonBuilder.filePath(System.getProperty("user.dir").concat(File.separator).concat("docs").concat(File.separator));
            String description = apis.getDescription();
            if (!generatorAll && !apiList.contains(description)) {
                continue;
            }
            // 替换'/'
            if (description.contains("/")) {
                description = description.replaceAll("/", "或");
            }
            markdwonBuilder.fileName(description.concat(".md"));
            markdwonBuilder.h1(apis.getDescription());
            for (int i = 0, size = docsApis.size(); i < size; i++) {
                JSONObject docsApi = (JSONObject) docsApis.get(i);
                JSONArray operations = docsApi.getJSONArray("operations");
                for (Object object : operations) {
                    JSONObject operation = (JSONObject) object;
                    markdwonBuilder.h2((i + 1) + "." + operation.getString("summary"));
                    markdwonBuilder.quote(username);
                    markdwonBuilder.quote(email);
                    markdwonBuilder.h3("请求路径");
                    markdwonBuilder.code(devUrl + docsApi.getString("path"));
                    markdwonBuilder.code(srpUrl + docsApi.getString("path"));
                    markdwonBuilder.h3("请求方式");
                    markdwonBuilder.line(operation.getString("method"));
                    markdwonBuilder.h3("请求类型");
                    markdwonBuilder.line(operation.getString("consumes"));
                    markdwonBuilder.h3("请求体");
                    getRequest(markdwonBuilder, docsModels, operation);
                    markdwonBuilder.h3("响应体");
                    getResponse(markdwonBuilder, docsModels, operation);
                }
            }
            markdwonBuilder.build().toFile();
        }
    }

    private void getRequest(Markdown.Builder markdwonBuilder, JSONObject docsModels, JSONObject operation) {
        JSONArray parameters = operation.getJSONArray("parameters");
        if (parameters.size() == 0) {
            return;
        }
        List<JSONObject> parameterList = jsonArraySortToList(parameters);
        markdwonBuilder.tableTr("请求类型", "字段名", "是否必填", "字段类型", "描述");
        List<String> requestTypes = new ArrayList<>();
        for (JSONObject parameter : parameterList) {
            String type = parameter.getString("type");
            String paramType = parameter.getString("paramType");
            String name = parameter.getString("name");
            String required = parameter.getString("required");
            String description = parameter.getString("description");
            if (type.equals(SWAGGER_ARRAY)) {
                type = parameter.getJSONObject("items").getString("type");
                markdwonBuilder.tableTd(paramType, name, required, "[" + type + "]", description);
            } else {
                markdwonBuilder.tableTd(paramType, name, required, type, description);
            }
            if (docsModels.getJSONObject(type) != null) {
                requestTypes.add(type);
            }
        }
        List<String> typeList = new ArrayList<>();
        getRequestDetail(markdwonBuilder, docsModels, requestTypes, typeList);
    }

    private static List<JSONObject> jsonArraySortToList(JSONArray parameters) {
        List<JSONObject> parameterList = parameters.toJavaList(JSONObject.class);
        parameterList.sort((o1, o2) -> {
            String name1 = o1.getString("name");
            String name2 = o2.getString("name");
            if (name1 == null || name2 == null) {
                return -1;
            }
            return name1.compareTo(name2);
        });
        return parameterList;
    }

    private void getResponse(Markdown.Builder markdwonBuilder, JSONObject docsModels, JSONObject operation) {
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
                    List<String> typeList = new ArrayList<>();
                    getTypes(markdwonBuilder, docsModels, properties, keySet, types);
                    if (types.size() > 0) {
                        getResponseDetail(markdwonBuilder, docsModels, types, typeList);
                    }
                }
            }
        }
    }

    private void getTypes(Markdown.Builder markdwonBuilder, JSONObject docsModels, JSONObject properties, Set<String> keySet, List<String> types) {
        for (String key : keySet) {
            String type = properties.getJSONObject(key).getString("type");
            String description = properties.getJSONObject(key).getString("description");
            if (type.equals(SWAGGER_ARRAY)) {
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

    private void getRequestDetail(Markdown.Builder markdwonBuilder, JSONObject docsModels, List<String> types, List<String> typeList) {
        if (types.size() > 0) {
            for (String requestType : types) {
                if (typeList.contains(requestType)) {
                    continue;
                } else {
                    typeList.add(requestType);
                }
                markdwonBuilder.h4(requestType);
                markdwonBuilder.tableTr("字段名", "是否必填", "字段类型", "描述");
                JSONObject properties = docsModels.getJSONObject(requestType).getJSONObject("properties");
                List<String> tempTypes = new ArrayList<>();
                Set<String> keySet = properties.keySet();
                List<String> propertieList = new ArrayList<>(keySet);
                propertieList.sort((o1, o2) -> {
                    if (o1 == null || o2 == null) {
                        return -1;
                    }
                    return o1.compareTo(o2);
                });
                for (String key : propertieList) {
                    String tempType = properties.getJSONObject(key).getString("type");
                    String description = properties.getJSONObject(key).getString("description");
                    String required = properties.getJSONObject(key).getString("required");
                    if (tempType.equals(SWAGGER_ARRAY)) {
                        tempType = properties.getJSONObject(key).getJSONObject("items").getString("type");
                        markdwonBuilder.tableTd(key, required, "[" + tempType + "]", description);
                    } else {
                        markdwonBuilder.tableTd(key, required, tempType, description);
                    }
                    if (docsModels.getJSONObject(tempType) != null) {
                        tempTypes.add(tempType);
                    }
                }
                getRequestDetail(markdwonBuilder, docsModels, tempTypes, typeList);
            }
        }
    }

    private void getResponseDetail(Markdown.Builder markdwonBuilder, JSONObject docsModels, List<String> types, List<String> typeList) {
        if (types.size() > 0) {
            for (String responseType : types) {
                if (typeList.contains(responseType)) {
                    continue;
                } else {
                    typeList.add(responseType);
                }
                markdwonBuilder.h4(responseType);
                markdwonBuilder.tableTr("字段名", "字段类型", "描述");
                JSONObject properties = docsModels.getJSONObject(responseType).getJSONObject("properties");
                List<String> tempTypes = new ArrayList<>();
                Set<String> keySet = properties.keySet();
                getTypes(markdwonBuilder, docsModels, properties, keySet, tempTypes);
                getResponseDetail(markdwonBuilder, docsModels, tempTypes, typeList);
            }
        }
    }
}
