package com.example.okhttp;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface HttpClient<T> {
     String callMethod(String httpMethod, String path);
     String callMethodWithJson(String httpMethod, String path, Object pathParameterJsonObject);
     String callMethodWithFormData(String httpMethod, String path, Map<String, String> pathParameterMap);
     String callMethodWithMultipartFormData(String httpMethod, String path, Map<String, String> formParameterMap, List<File> fileList);
     T callMethod(String httpMethod, String path, Class<T> returnClass, Class<?> ... parameterClass);
     T callMethodWithJson(String httpMethod, String path, Object jsonObject, Class<T> returnClass, Class<?> ... parameterClass);
     T callMethodWithFormData(String httpMethod, String path, Map<String, String> formParameterMap, Class<T> returnClass, Class<?> ... parameterClass);
     T callMethodWithMultipartFormData(String httpMethod, String path, Map<String, String> formParameterMap, List<File> fileList, Class<T> returnClass, Class<?> ... parameterClass);

     String callGetMethod(String path);
     String callGetMethodWithJson(String path, Object pathParameterJsonObject);
     String callGetMethodWithFormData(String path, Map<String, String> pathParameterMap);
     T callGetMethod(String path, Class<T> returnClass, Class<?> ... parameterClass);
     T callGetMethodWithJson(String path, Object pathParameterJsonObject, Class<T> returnClass, Class<?> ...parameterClass);
     T callGetMethodWithFormData(String path, Map<String, String> pathParameterMap, Class<T> returnClass, Class<?> ...parameterClass);

     String callPostMethod(String path);
     String callPostMethodWithJson(String path, Object pathParameterJsonObject);
     String callPostMethodWithFormData(String path, Map<String, String> pathParameterMap);
     String callPostMethodWithMultipartFormData(String path, Map<String, String> formParameterMap, List<File> fileList);
     T callPostMethod(String path, Class<T> returnClass, Class<?> ... parameterClass);
     T callPostMethodWithJson(String path, Object jsonObject, Class<T> returnClass, Class<?> ... parameterClass);
     T callPostMethodWithFormData(String path, Map<String, String> formParameterMap, Class<T> returnClass, Class<?> ... parameterClass);
     T callPostMethodWithMultipartFormData(String path, Map<String, String> formParameterMap, List<File> fileList, Class<T> returnClass, Class<?> ... parameterClass);

     String callPutMethod(String path);
     String callPutMethodWithJson(String path, Object pathParameterJsonObject);
     String callPutMethodWithFormData(String path, Map<String, String> pathParameterMap);
     String callPutMethodWithMultipartFormData(String path, Map<String, String> formParameterMap, List<File> fileList);
     T callPutMethod(String path, Class<T> returnClass, Class<?> ... parameterClass);
     T callPutMethodWithJson(String path, Object jsonObject, Class<T> returnClass, Class<?> ... parameterClass);
     T callPutMethodWithFormData(String path, Map<String, String> formParameterMap, Class<T> returnClass, Class<?> ... parameterClass);
     T callPutMethodWithMultipartFormData(String path, Map<String, String> formParameterMap, List<File> fileList, Class<T> returnClass, Class<?> ...parameterClass);

     String callDeleteMethod(String path);
     String callDeleteMethodWithJson(String path, Object pathParameterJsonObject);
     String callDeleteMethodWithFormData(String path, Map<String, String> pathParameterMap);
     String callDeleteMethodWithMultipartFormData(String path, Map<String, String> formParameterMap, List<File> fileList);
     T callDeleteMethod(String path, Class<T> returnClass, Class<?> ... parameterClass);
     T callDeleteMethodWithJson(String path, Object jsonObject, Class<T> returnClass, Class<?> ... parameterClass);
     T callDeleteMethodWithFormData(String path, Map<String, String> formParameterMap, Class<T> returnClass, Class<?> ... parameterClass);
     T callDeleteMethodWithMultipartFormData(String path, Map<String, String> formParameterMap, List<File> fileList, Class<T> returnClass, Class<?> ...parameterClass);

}
