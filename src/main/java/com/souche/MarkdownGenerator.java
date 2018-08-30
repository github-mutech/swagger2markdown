package com.souche;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.souche.pojo.Apis;
import com.souche.pojo.SwaggerDocs;
import com.souche.util.HttpClientUtil;
import com.souche.util.PropertiesUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MarkdownGenerator {

    public static String[] titleLevel = {"# ", "## ", "### ", "#### ", "##### "};

    public static void main(String[] args) {
        String swaggerApiDocsUrl = PropertiesUtil.getInstance().getStringValue("swagger.api-docs.url");
        SwaggerDocs swaggerDocs = JSON.parseObject(HttpClientUtil.doGet(swaggerApiDocsUrl), SwaggerDocs.class);
        String termsOfServiceUrl = swaggerDocs.getInfo().getTermsOfServiceUrl();
        for (Apis apis : swaggerDocs.getApis()) {
            JSONObject jsonObject = JSON.parseObject(HttpClientUtil.doGet(swaggerApiDocsUrl + apis.getPath()));
            JSONArray docsApis = jsonObject.getJSONArray("apis");
            JSONObject docsModels = jsonObject.getJSONObject("models");
            System.out.println(titleLevel[0] + apis.getDescription());
            for (int i = 0, size = docsApis.size(); i < size; i++) {
                JSONObject docsApi = (JSONObject) docsApis.get(i);
                JSONObject operation = (JSONObject) docsApi.getJSONArray("operations").get(0);
                System.out.println("## " + (i + 1) + "." + operation.getString("summary"));
                System.out.println("### 请求路径");
                System.out.println("```");
                System.out.println(termsOfServiceUrl + docsApi.getString("path"));
                System.out.println("```");
                System.out.println("### 请求方式");
                System.out.println(operation.getString("method"));
                System.out.println("### 请求类型");
                System.out.println(operation.getString("consumes"));
                System.out.println("### 请求体");
                getRequest(docsModels, operation);
                System.out.println("### 响应体");
                getResponse(docsModels, operation);
            }
        }
    }

    private static void getRequest(JSONObject docsModels, JSONObject operation) {

        JSONArray parameters = operation.getJSONArray("parameters");
        if (parameters.size()>0){
            System.out.println("|请求类型|字段名|是否必填|字段类型|描述|");
            System.out.println("|---|---|---|---|---|");
        }
        List<String> requestTypes = new ArrayList<>();
        for (Object object : parameters) {
            JSONObject parameter = (JSONObject) object;
            String type = parameter.getString("type");
            if (type.equals("array")) {
                type = parameter.getJSONObject("items").getString("type");
                System.out.println("|" + parameter.getString("paramType") + "|"
                        + parameter.getString("name") + "|"
                        + parameter.getString("required") + "|"
                        + "[" + type + "]" + "|"
                        + parameter.getString("description") + "|");
            } else {
                System.out.println("|" + parameter.getString("paramType") + "|"
                        + parameter.getString("name") + "|"
                        + parameter.getString("required") + "|"
                        + type + "|"
                        + parameter.getString("description") + "|");
            }

            if (docsModels.getJSONObject(type) != null) {
                requestTypes.add(type);
            }

        }
        showRequest(docsModels, requestTypes, 4);
    }

    private static void getResponse(JSONObject docsModels, JSONObject operation) {

        JSONArray responseMessages = operation.getJSONArray("responseMessages");
        for (Object object : responseMessages) {
            JSONObject responseMessage = (JSONObject) object;
            if (200 == responseMessage.getInteger("code")) {
                String responseModel = responseMessage.getString("responseModel");
                if (docsModels.getJSONObject(responseModel) != null) {
                    System.out.println("#### " + responseModel);
                    JSONObject properties = docsModels.getJSONObject(responseModel).getJSONObject("properties");
                    Set<String> keySet = properties.keySet();
                    System.out.println("|字段名|字段类型|描述|");
                    System.out.println("|---|---|---|");
                    List<String> types = new ArrayList<>();
                    for (String key : keySet) {
                        String type = properties.getJSONObject(key).getString("type");
                        String description = properties.getJSONObject(key).getString("description");
                        if (type.equals("array")) {
                            type = properties.getJSONObject(key).getJSONObject("items").getString("type");
                            System.out.println("|" + key + "|[" + type + "]|" + description + "|");
                        } else {
                            System.out.println("|" + key + "|" + type + "|" + description + "|");
                        }
                        if (docsModels.getJSONObject(type) != null) {
                            types.add(type);
                        }
                    }
                    if (types.size() > 0) {
                        showResponse(docsModels, types, 3);
                    }
                }
            }
        }
    }

    private static void showRequest(JSONObject docsModels, List<String> requestTypes, int i) {
        if (requestTypes.size() > 0) {
            for (String requestType : requestTypes) {
                System.out.println(titleLevel[i] + requestType);
                System.out.println("|字段名|是否必填|字段类型|描述|");
                System.out.println("|---|---|---|---|");
                JSONObject properties = docsModels.getJSONObject(requestType).getJSONObject("properties");
                List<String> tempTypes = new ArrayList<>();
                Set<String> keySet = properties.keySet();
                for (String key : keySet) {
                    String tempType = properties.getJSONObject(key).getString("type");
                    String description = properties.getJSONObject(key).getString("description");
                    String required = properties.getJSONObject(key).getString("required");
                    if (tempType.equals("array")) {
                        tempType = properties.getJSONObject(key).getJSONObject("items").getString("type");
                        System.out.println("|" + key + "|" + required +
                                "|[" + tempType + "]|" + description + "|");
                    } else {
                        System.out.println("|" + key + "|" + required +
                                "|" + tempType + "|" + description + "|");
                    }
                    if (docsModels.getJSONObject(tempType) != null) {
                        tempTypes.add(tempType);
                    }
                }
                showRequest(docsModels, tempTypes, 4);
            }
        }
    }

    private static void showResponse(JSONObject docsModels, List<String> responseTypes, int i) {
        if (responseTypes.size() > 0) {
            for (String responseType : responseTypes) {
                System.out.println(titleLevel[i] + responseType);
                System.out.println("|字段名|字段类型|描述|");
                System.out.println("|---|---|---|");
                JSONObject properties = docsModels.getJSONObject(responseType).getJSONObject("properties");
                List<String> tempTypes = new ArrayList<>();
                Set<String> keySet = properties.keySet();
                for (String key : keySet) {
                    String tempType = properties.getJSONObject(key).getString("type");
                    String description = properties.getJSONObject(key).getString("description");
                    if (tempType.equals("array")) {
                        tempType = properties.getJSONObject(key).getJSONObject("items").getString("type");
                        System.out.println("|" + key + "|[" + tempType + "]|" + description + "|");
                    }else {
                        System.out.println("|" + key + "|" + tempType + "|" + description + "|");
                    }
                    if (docsModels.getJSONObject(tempType) != null) {
                        tempTypes.add(tempType);
                    }
                }
                showResponse(docsModels, tempTypes, 4);

            }

        }
    }
}
