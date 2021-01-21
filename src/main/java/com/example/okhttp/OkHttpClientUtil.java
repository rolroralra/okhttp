package com.example.okhttp;

import com.example.okhttp.model.ConnectionPoolOption;
import com.example.okhttp.model.EndPoint;
import com.example.okhttp.model.Proxy;
import com.example.okhttp.model.Timeout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import java.io.File;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * OkHttpClient Util Class
 *
 * @author: shyoung.kim@samsung.com
 *
 */
public class OkHttpClientUtil<T> implements HttpClient<T> {
    public final static int DEFAULT_MAX_CONNECT_COUNT = ConnectionPoolOption.DEFAULT_MAX_CONNECTION_COUNT;
    public final static long DEFAULT_KEEP_ALIVE_DURATION = ConnectionPoolOption.DEFAULT_KEEP_ALIVE_DURATION;
    public final static int DEFAULT_READ_TIMEOUT = Timeout.DEFAULT_READ_TIMEOUT;
    public final static int DEFAULT_WRITE_TIMEOUT = Timeout.DEFAULT_WRITE_TIMEOUT;
    public final static int DEFAULT_CONNECT_TIMEOUT = Timeout.DEFAULT_CONNECT_TIMEOUT;
    public final static int DEFAULT_RETRY_COUNT = ConnectionPoolOption.DEFAULT_RETRY_COUNT;
    public final static TimeUnit DEFAULT_TIMEUNIT = TimeUnit.MILLISECONDS;
    public final static MediaType DEFAULT_MEDIA_TYPE = MediaType.parse("application/json");

    private OkHttpClient okHttpClient;
    private final Gson gson;
    private final Gson gsonPretty;

    private final EndPoint endPoint;
    private Proxy proxy;
    private final Timeout timeout;
    private final ConnectionPoolOption connectionPoolOption;

    private OkHttpClientUtil() {
        this(new Gson());
    }

    private OkHttpClientUtil(Gson gson) {
        this.gson = gson;
        this.gsonPretty = gson.newBuilder().setPrettyPrinting().create();
        this.endPoint = new EndPoint();
        this.proxy = null;
        this.timeout = new Timeout();
        this.connectionPoolOption = new ConnectionPoolOption();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Gson gson) {
        return new Builder(gson);
    }

    @SuppressWarnings("rawtypes")
    public static class Builder {
        private final OkHttpClientUtil okHttpClientUtil;

        public Builder() {
            this(new Gson());
        }

        public Builder(Gson gson) {
            this.okHttpClientUtil = new OkHttpClientUtil(gson);
        }

        public OkHttpClientUtil build() {
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

            okHttpClientBuilder
                    .readTimeout(this.okHttpClientUtil.timeout.getReadTimeout(), this.okHttpClientUtil.timeout.getTimeUnit())
                    .connectTimeout(this.okHttpClientUtil.timeout.getConnectTimeout(), this.okHttpClientUtil.timeout.getTimeUnit())
                    .writeTimeout(this.okHttpClientUtil.timeout.getWriteTimeout(), this.okHttpClientUtil.timeout.getTimeUnit())
                    .connectionPool(
                            new ConnectionPool(
                                    this.okHttpClientUtil.connectionPoolOption.getMaxConnectionCount(),
                                    this.okHttpClientUtil.connectionPoolOption.getKeepAliveDuration(),
                                    this.okHttpClientUtil.timeout.getTimeUnit())
                    );

            if (this.okHttpClientUtil.proxy != null) {
                okHttpClientBuilder.proxy(
                        new java.net.Proxy(
                                this.okHttpClientUtil.proxy.getType(),
                                new InetSocketAddress(this.okHttpClientUtil.proxy.getHost(), this.okHttpClientUtil.proxy.getPort()))
                );
            }

            this.okHttpClientUtil.okHttpClient = okHttpClientBuilder.build();

            return this.okHttpClientUtil;
        }

        public Builder protocol(String value) {
            this.okHttpClientUtil.endPoint.setProtocol(value);
            return this;
        }

        public Builder host(String value) {
            this.okHttpClientUtil.endPoint.setHost(value);
            return this;
        }

        public Builder port(Integer value) {
            this.okHttpClientUtil.endPoint.setPort(value);
            return this;
        }

        public Builder basePath(String value) {
            this.okHttpClientUtil.endPoint.setBasePath(value);
            return this;
        }

        public Builder setReadTimeout(Integer value) {
            this.okHttpClientUtil.timeout.setReadTimeout(value);
            return this;
        }

        public Builder setConnectTimeout(Integer value) {
            this.okHttpClientUtil.timeout.setConnectTimeout(value);
            return this;
        }

        public Builder setWriteTimeout(Integer value) {
            this.okHttpClientUtil.timeout.setWriteTimeout(value);
            return this;
        }

        public Builder setTimeUnit(TimeUnit value) {
            this.okHttpClientUtil.timeout.setTimeUnit(value);
            return this;
        }

        public Builder setProxy(Proxy value) {
            this.okHttpClientUtil.proxy = value;
            return this;
        }

        public Builder setMaxConnectionCount(Integer value) {
            this.okHttpClientUtil.connectionPoolOption.setMaxConnectionCount(value);
            return this;
        }

        public Builder setKeepAliveDuration(Integer value) {
            this.okHttpClientUtil.connectionPoolOption.setKeepAliveDuration(value);
            return this;
        }

        public Builder setRetryCount(Integer value) {
            this.okHttpClientUtil.connectionPoolOption.setRetryCount(value);
            return this;
        }
    }

    private String getRestApiUrl(String path) {
        try {
            String url = this.endPoint.getUrl() + path;
            return url;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getRestApiUrl(String path, Object object) {
        try {
            HttpUrl.Builder httpUrlBuilder
                    = Objects.requireNonNull(
                    HttpUrl.parse(this.getRestApiUrl(path))
            ).newBuilder();

            for (Field field : object.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.get(object) != null) {
                    httpUrlBuilder.addQueryParameter(field.getName(), field.get(object).toString());
                }
                field.setAccessible(false);
            }

            URL url = httpUrlBuilder.build().url();
            return url.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getRestApiUrl(String path, Map<String, String> queryParams) {
        try {
            HttpUrl.Builder httpUrlBuilder
                    = Objects.requireNonNull(
                    HttpUrl.parse(this.getRestApiUrl(path))
            ).newBuilder();

            queryParams.forEach(httpUrlBuilder::addQueryParameter);

            URL url = httpUrlBuilder.build().url();
            return url.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private T parseGson(String jsonString, Class<T> returnClass, Class<?> ... parameterClass) {
        T result;
        try {
            if (parameterClass.length > 0) {
                result = gson.fromJson(jsonString, TypeToken.getParameterized(returnClass, parameterClass).getType());
            }
            else {
                result = gson.fromJson(jsonString, returnClass);
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        return result;
    }

    // TODO: Handle Response. (if not successful response)
    // response.isSuccessful();
    // response.isRedirect();
    @Override
    public String callMethod(String httpMethod, String path) {
        String result;
        try {
            // Request.Builder
            Request.Builder requestBuilder = new Request.Builder()
                    .url(this.getRestApiUrl(path));

            // HttpMethod
            switch(httpMethod.toUpperCase()) {
                case "GET":
                    requestBuilder.get();
                    break;
                case "POST":
                case "PUT":
                case "DELETE":
                    // RequestBody Builder
                    requestBuilder.method(
                            httpMethod,
                            RequestBody.create(new byte[0], DEFAULT_MEDIA_TYPE)
                    );
                    break;
                default:
                    throw new RuntimeException("Http Method Not Supported. " + httpMethod);
            }

            // Request
            Request request = requestBuilder.build();

            // Response
            Response response = okHttpClient.newCall(request).execute();
            result = Objects.requireNonNull(response.body()).string();

            // Response Status Check
            if (!response.isSuccessful() && !response.isRedirect()) {
                throw new RuntimeException("Response is not successful. " + result);
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public String callMethodWithJson(String httpMethod, String path, Object jsonObject) {
        String result;
        try {
            // Request.Builder
            Request.Builder requestBuilder = new Request.Builder()
                    .url(this.getRestApiUrl(path));

            // HttpMethod
            switch(httpMethod.toUpperCase()) {
                case "GET":
                    requestBuilder
                            .url(this.getRestApiUrl(path, jsonObject))
                            .get();
                    break;
                case "POST":
                case "PUT":
                case "DELETE":
                    // RequestBody Builder
                    RequestBody requestBody = RequestBody.create(
                            gson.toJson(jsonObject, jsonObject.getClass()),
                            MediaType.parse("application/json")
                    );

                    requestBuilder.method(httpMethod, requestBody);
                    break;
                default:
                    throw new RuntimeException("Http Method Not Supported. " + httpMethod);
            }

            // Request
            Request request = requestBuilder.build();

            // Response
            Response response = okHttpClient.newCall(request).execute();
            result = Objects.requireNonNull(response.body()).string();

            // Response Status Check
            if (!response.isSuccessful() && !response.isRedirect()) {
                throw new RuntimeException("Response is not successful. " + result);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public String callMethodWithFormData(String httpMethod, String path, Map<String, String> parameterMap) {
        String result;
        try {
            // Request.Builder
            Request.Builder requestBuilder = new Request.Builder()
                    .url(this.getRestApiUrl(path));

            // HttpMethod
            switch(httpMethod.toUpperCase()) {
                case "GET":
                    requestBuilder.url(this.getRestApiUrl(path, parameterMap))
                            .get();
                    break;
                case "POST":
                case "PUT":
                case "DELETE":
                    // FormBody.Builder (RequestBodyBuilder)
                    FormBody.Builder formBodyBuilder = new FormBody.Builder();
                    parameterMap.forEach(formBodyBuilder::add);

                    RequestBody requestBody = formBodyBuilder.build();
                    requestBuilder.method(httpMethod, requestBody);
                    break;
                default:
                    throw new RuntimeException("Http Method Not Supported. " + httpMethod);
            }

            // Request
            Request request = requestBuilder.build();

            // Response
            Response response = okHttpClient.newCall(request).execute();
            result = Objects.requireNonNull(response.body()).string();

            // Response Status Check
            if (!response.isSuccessful() && !response.isRedirect()) {
                throw new RuntimeException("Response is not successful. " + result);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public String callMethodWithMultipartFormData(String httpMethod, String path, Map<String, String> parameterMap, List<File> fileList) {
        String result;
        try {
            // Request.Builder
            Request.Builder requestBuilder = new Request.Builder()
                    .url(this.getRestApiUrl(path));

            // HttpMethod
            // RequestBodyBuilder
            switch (httpMethod.toUpperCase()) {
                case "GET" -> {
                    requestBuilder.url(this.getRestApiUrl(path, parameterMap))
                            .get();
                }
                case "POST", "PUT", "DELETE" -> {
                    MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM);
                    for (File file : fileList) {
                        requestBodyBuilder.addFormDataPart(
                                "file",
                                file.getName(),
                                RequestBody.create(file, MediaType.parse("text/plain"))
                        );
                    }
                    for (String key : parameterMap.keySet()) {
                        requestBodyBuilder.addFormDataPart(key, parameterMap.get(key));
                    }
                    RequestBody requestBody = requestBodyBuilder.build();
                    requestBuilder.method(httpMethod, requestBody);
                }
                default -> throw new RuntimeException("Http Method Not Supported. " + httpMethod);
            }

            // Request
            Request request = requestBuilder.build();

            // Response
            Response response = okHttpClient.newCall(request).execute();
            result = Objects.requireNonNull(response.body()).string();

            // Response Status Check
            if (!response.isSuccessful() && !response.isRedirect()) {
                throw new RuntimeException("Response is not successful. " + result);
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        return result;
    }

    public T callMethod(String httpMethod, String path, Class<T> returnClass, Class<?> ... parameterClass) {
        T result;
        try {
            String responseBodyString = this.callMethod(httpMethod, path);

            result = this.parseGson(responseBodyString, returnClass, parameterClass);
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        return result;
    }

    public T callMethodWithJson(String httpMethod, String path, Object jsonObject, Class<T> returnClass, Class<?> ... parameterClass) {
        T result;
        try {
            String responseBodyString = this.callMethodWithJson(httpMethod, path, jsonObject);
            result = this.parseGson(responseBodyString, returnClass, parameterClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public T callMethodWithFormData(String httpMethod, String path, Map<String, String> parameterMap, Class<T> returnClass, Class<?> ...parameterClass) {
        T result;
        try {
            String responseBodyString = this.callMethodWithFormData(httpMethod, path, parameterMap);
            result = this.parseGson(responseBodyString, returnClass, parameterClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public T callMethodWithMultipartFormData(String httpMethod, String path, Map<String, String> parameterMap, List<File> fileList, Class<T> returnClass, Class<?> ...parameterClass) {
        T result;
        try {
            String responseBodyString = this.callMethodWithMultipartFormData(httpMethod, path, parameterMap, fileList);
            result = this.parseGson(responseBodyString, returnClass, parameterClass);
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public String callGetMethod(String path) {
        return this.callMethod("GET", path);
    }

    @Override
    public String callGetMethodWithJson(String path, Object pathParameterJsonObject) {
        return this.callMethodWithJson("GET", path, pathParameterJsonObject);
    }

    @Override
    public String callGetMethodWithFormData(String path, Map<String, String> pathParameterMap) {
        return this.callMethodWithFormData("GET", path, pathParameterMap);
    }

    @Override
    public T callGetMethod(String path, Class<T> returnClass, Class<?>... parameterClass) {
        return this.callMethod("GET", path, returnClass, parameterClass);
    }

    @Override
    public T callGetMethodWithJson(String path, Object pathParameterJsonObject, Class<T> returnClass, Class<?>... parameterClass) {
        return this.callMethodWithJson("GET", path, pathParameterJsonObject, returnClass, parameterClass);
    }

    @Override
    public T callGetMethodWithFormData(String path, Map<String, String> pathParameterMap, Class<T> returnClass, Class<?>... parameterClass) {
        return this.callMethodWithFormData("GET", path, pathParameterMap, returnClass, parameterClass);
    }

    @Override
    public String callPostMethod(String path) {
        return this.callMethod("POST", path);
    }

    @Override
    public String callPostMethodWithJson(String path, Object pathParameterJsonObject) {
        return this.callMethodWithJson("POST", path, pathParameterJsonObject);
    }

    @Override
    public String callPostMethodWithFormData(String path, Map<String, String> pathParameterMap) {
        return this.callMethodWithFormData("POST", path, pathParameterMap);
    }

    @Override
    public String callPostMethodWithMultipartFormData(String path, Map<String, String> formParameterMap, List<File> fileList) {
        return this.callMethodWithMultipartFormData("POST", path, formParameterMap, fileList);
    }

    @Override
    public T callPostMethod(String path, Class<T> returnClass, Class<?>... parameterClass) {
        return this.callGetMethodWithJson("POST", path, returnClass, parameterClass);
    }

    @Override
    public T callPostMethodWithJson(String path, Object jsonObject, Class<T> returnClass, Class<?>... parameterClass) {
        return this.callMethodWithJson("POST", path, jsonObject, returnClass, parameterClass);
    }

    @Override
    public T callPostMethodWithFormData(String path, Map<String, String> formParameterMap, Class<T> returnClass, Class<?>... parameterClass) {
        return this.callMethodWithFormData("POST", path, formParameterMap, returnClass, parameterClass);
    }

    @Override
    public T callPostMethodWithMultipartFormData(String path, Map<String, String> formParameterMap, List<File> fileList, Class<T> returnClass, Class<?>... parameterClass) {
        return callMethodWithMultipartFormData("POST", path, formParameterMap, fileList, returnClass, parameterClass);
    }

    @Override
    public String callPutMethod(String path) {
        return this.callMethod("PUT", path);
    }

    @Override
    public String callPutMethodWithJson(String path, Object pathParameterJsonObject) {
        return this.callMethodWithJson("PUT", path, pathParameterJsonObject);
    }

    @Override
    public String callPutMethodWithFormData(String path, Map<String, String> pathParameterMap) {
        return this.callMethodWithFormData("PUT", path, pathParameterMap);
    }

    @Override
    public String callPutMethodWithMultipartFormData(String path, Map<String, String> formParameterMap, List<File> fileList) {
        return this.callMethodWithMultipartFormData("PUT", path, formParameterMap, fileList);
    }

    @Override
    public T callPutMethod(String path, Class<T> returnClass, Class<?> ... parameterClass) {
        return this.callMethod("PUT", path, returnClass, parameterClass);
    }

    @Override
    public T callPutMethodWithJson(String path, Object jsonObject, Class<T> returnClass, Class<?> ... parameterClass) {
        return this.callMethodWithJson("PUT", path, jsonObject, returnClass, parameterClass);
    }

    @Override
    public T callPutMethodWithFormData(String path, Map<String, String> formParameterMap, Class<T> returnClass, Class<?> ... parameterClass) {
        return this.callMethodWithFormData("PUT", path, formParameterMap, returnClass, parameterClass);
    }

    @Override
    public T callPutMethodWithMultipartFormData(String path, Map<String, String> formParameterMap, List<File> fileList, Class<T> returnClass, Class<?> ...parameterClass) {
        return this.callMethodWithMultipartFormData("PUT", path, formParameterMap, fileList, returnClass, parameterClass);
    }

    @Override
    public String callDeleteMethod(String path) {
        return this.callMethod("DELETE", path);
    }

    @Override
    public String callDeleteMethodWithJson(String path, Object pathParameterJsonObject) {
        return this.callMethodWithJson("DELETE", path, pathParameterJsonObject);
    }

    @Override
    public String callDeleteMethodWithFormData(String path, Map<String, String> pathParameterMap) {
        return this.callMethodWithFormData("DELETE", path, pathParameterMap);
    }

    @Override
    public String callDeleteMethodWithMultipartFormData(String path, Map<String, String> formParameterMap, List<File> fileList) {
        return this.callMethodWithMultipartFormData("DELETE", path, formParameterMap, fileList);
    }

    @Override
    public T callDeleteMethod(String path, Class<T> returnClass, Class<?> ... parameterClass) {
        return this.callMethod("DELETE", path, returnClass, parameterClass);
    }

    @Override
    public T callDeleteMethodWithJson(String path, Object jsonObject, Class<T> returnClass, Class<?> ... parameterClass) {
        return this.callMethodWithJson("DELETE", path, jsonObject, returnClass, parameterClass);
    }

    @Override
    public T callDeleteMethodWithFormData(String path, Map<String, String> formParameterMap, Class<T> returnClass, Class<?> ... parameterClass) {
        return this.callMethodWithFormData("DELETE", path, formParameterMap, returnClass, parameterClass);
    }

    @Override
    public T callDeleteMethodWithMultipartFormData(String path, Map<String, String> formParameterMap, List<File> fileList, Class<T> returnClass, Class<?>... parameterClass) {
        return this.callMethodWithMultipartFormData("DELETE", path, formParameterMap, fileList, returnClass, parameterClass);
    }

}
