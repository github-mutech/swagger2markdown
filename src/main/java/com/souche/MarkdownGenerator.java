package com.souche;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.souche.pojo.Apis;
import com.souche.pojo.SwaggerDocs;
import com.souche.util.HttpClientUtil;
import com.souche.util.PropertiesUtil;

public class MarkdownGenerator {
    public static void main(String[] args) {
        String swaggerApiDocsUrl = PropertiesUtil.getInstance().getStringValue("swagger.api-docs.url");
        SwaggerDocs swaggerDocs = JSON.parseObject(HttpClientUtil.doGet(swaggerApiDocsUrl), SwaggerDocs.class);
        String termsOfServiceUrl = swaggerDocs.getInfo().getTermsOfServiceUrl();
        for (Apis apis : swaggerDocs.getApis()) {
            JSONObject jsonObject = JSON.parseObject(HttpClientUtil.doGet(termsOfServiceUrl + apis.getPath()));
            JSONArray docsApis = jsonObject.getJSONArray("apis");
            JSONObject docsModels = jsonObject.getJSONObject("models");
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
                System.out.println("|请求类型|字段名|是否必填|字段类型|描述|");
                System.out.println("|---|---|---|---|---|");
                JSONArray parameters = operation.getJSONArray("parameters");
                for (Object object : parameters) {
                    JSONObject parameter = (JSONObject) object;
                    System.out.println("|" + parameter.getString("paramType") + "|"
                            + parameter.getString("name") + "|"
                            + parameter.getString("required") + "|"
                            + parameter.getString("type") + "|"
                            + parameter.getString("description") + "|");
                }
                JSONArray responseMessages = operation.getJSONArray("responseMessages");
                for (Object object : responseMessages) {
                    JSONObject responseMessage = (JSONObject) object;
                    if (200 == responseMessage.getInteger("code")) {
                        String responseModel = responseMessage.getString("responseModel");
                        while (docsModels.getJSONObject(responseModel) != null) {
                            System.out.println(docsModels.getJSONObject(responseModel));
                        }
                    }
                }
                System.out.println("### 响应体");
                System.out.println("|字段名|字段类型|描述|");
                System.out.println("|---|---|---|");
//                JSONArray parameters = operation.getJSONArray("parameters");
//                for (Object object : parameters) {
//                    JSONObject parameter = (JSONObject) object;
//                    System.out.println("|" + parameter.getString("paramType") + "|"
//                            + parameter.getString("name") + "|"
//                            + parameter.getString("required") + "|"
//                            + parameter.getString("type") + "|"
//                            + parameter.getString("description") + "|");
//                }

                System.out.println(docsApi);
            }
            System.out.println(jsonObject);
        }
    }
}
