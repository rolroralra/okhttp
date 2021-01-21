package com.example;

import com.example.okhttp.NexledgerRPCHttpClient;
import com.example.okhttp.OkHttpClientUtil;
import com.example.okhttp.model.EndPoint;

import java.util.HashMap;

public class Main {
    public static void main(String[] args) {

        NexledgerRPCHttpClient nexledgerRPCHttpClient = new NexledgerRPCHttpClient(
                EndPoint.builder().port(8080).build(),
                null,
                10000,
                10000,
                10000
        );

        System.out.println(nexledgerRPCHttpClient.callGetMethod("/security/generate/token?subject=rolroralra"));


        OkHttpClientUtil okHttpClientUtil = OkHttpClientUtil.builder().port(8080).build();

        String response = okHttpClientUtil.callGetMethodWithFormData("/security/generate/token", new HashMap<String, String>(){{put("subject", "rolroralra");}});
        System.out.println(response);
        //security/generate/token?subject=rolroralra
    }
}
