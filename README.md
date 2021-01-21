## Example

- [OkHttpClientUtil.java](./src/main/java/com/example/okhttp/OkHttpClientUtil.java)
> Builder Pattern

- [NexledgerRPCHttpClient.java](./src/main/java/com/example/okhttp/NexledgerRPCHttpClient.java)
> Just instantiate by init method

<details>
    <summary>Detail</summary>
    <p>

```java
import com.example.okhttp.NexledgerRPCHttpClient;
import com.example.okhttp.OkHttpClientUtil;
import com.example.okhttp.model.EndPoint;

import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        String restApiPath = "/security/generate/token";

        URL url = HttpUrl.parse("http://localhost:8080" + restApiPath)
                .newBuilder()
                .addQueryParameter("subject", "rolroralra")
                .build()
                .url();

        String query = url.getQuery();

        NexledgerRPCHttpClient nexledgerRPCHttpClient = new NexledgerRPCHttpClient(
                EndPoint.builder().port(8080).build(),
                null,
                10000,
                10000,
                10000
        );

        System.out.println(nexledgerRPCHttpClient.callGetMethod(restApiPath + "?" + query));


        OkHttpClientUtil okHttpClientUtil = OkHttpClientUtil.builder()
                .port(8080).build();

        String response = okHttpClientUtil.callGetMethodWithFormData(
                "/security/generate/token",
                new HashMap<String, String>(){{
                    put("subject", "rolroralra");
                }});
        System.out.println(response);
    }
}
```

</p>
</details>
