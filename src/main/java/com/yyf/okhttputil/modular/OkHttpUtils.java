package com.yyf.okhttputil.modular;

import com.alibaba.fastjson.JSON;
import com.yyf.okhttputil.modular.entity.ResultEntry;
import okhttp3.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp请求工具类
 * @author wangchen
 * @create 2020/05/14/12:45
 */
public class OkHttpUtils {

    private static volatile OkHttpClient okHttpClient = null;
    private static volatile Semaphore semaphore = null;
    private Map<String,String> headerMap;
    private Map<String,String> paramMap;
    private String url;
    private Request.Builder request;

    /**
     * 初始化okHttpClient 并且允许Https访问
     */
    private OkHttpUtils(){
        if(okHttpClient == null){
            synchronized (OkHttpUtils.class){
                if(okHttpClient == null){
                    //创建套接字安全管理器
                    TrustManager[] trustManagers = buildTrustManagers();
                    SSLSocketFactory sslSocketFactory = createSSLSocketFactory(trustManagers);
                    //timeUnit用于获取时间单位
                    okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .writeTimeout(20,TimeUnit.SECONDS)
                            .readTimeout(20,TimeUnit.SECONDS)
                            .sslSocketFactory(sslSocketFactory,(X509TrustManager)trustManagers[0])
                            .hostnameVerifier((hostName,session) -> true)
                            .retryOnConnectionFailure(true)
                            .build();
                    addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
                }
            }
        }
    }

    /**
     * 用于异步请求时，控制访问线程数，返回结果
     * @return
     */
    private static Semaphore getSemaphoreInstance(){
        //只能1个线程同时访问
        synchronized (OkHttpUtils.class){
            if(semaphore == null){
                semaphore = new Semaphore(0);
            }
        }
        return semaphore;
    }

    /**
     * 创建OkHttpUtils
     * @return
     */
    public static OkHttpUtils builder(){
        return new OkHttpUtils();
    }


    /**
     * 添加URL
     * @param url
     * @return
     */
    public OkHttpUtils url(String url){
        this.url = url;
        return this;
    }

    /**
     * 添加请求头
     * @param key   参数名
     * @param value 参数值
     * @return
     */
    public OkHttpUtils addHeader(String key, String value) {
        if(headerMap == null)
            headerMap = new LinkedHashMap<>(16);
        headerMap.put(key, value);
        return this;
    }

    /**
     * 添加参数
     * @param key 参数名
     * @param value 参数值
     * @return
     */
    public OkHttpUtils addParam(String key,String value){
        if(paramMap == null){
            paramMap = new LinkedHashMap<>(16);
        }
        paramMap.put(key, value);
        return this;
    }


    /**
     * 生成安全套接字工厂，用于https请求的证书跳过
     * @param trustManagers
     * @return
     */
    private static SSLSocketFactory createSSLSocketFactory(TrustManager[] trustManagers) {
        SSLSocketFactory sslSocketFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustManagers, new SecureRandom());
            sslSocketFactory = sc.getSocketFactory();
        }catch (Exception e){
            e.printStackTrace();
        }
        return sslSocketFactory;
    }

    private TrustManager[] buildTrustManagers() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
    }

    /**
     * 为request添加请求头
     * @param request
     */
    private void setHeader(Request.Builder request) {
        if(headerMap != null){
            try {
                for(Map.Entry<String,String> entry : headerMap.entrySet()){
                    request.addHeader(entry.getKey(), entry.getValue());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化get方法
     * @return
     */
    public OkHttpUtils get(){
        request = new Request.Builder().get();
        StringBuilder urlBuilder = new StringBuilder(url);
        if(paramMap != null){
            urlBuilder.append("?");
            try {
                for(Map.Entry<String,String> entry : paramMap.entrySet()){
                    urlBuilder.append(URLEncoder.encode(entry.getKey(),"UTF-8"))
                            .append("=")
                            .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                            .append("&");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }
        request.url(urlBuilder.toString());
        return this;
    }

    /**
     * 初始化post方法
     * @param isJsonPost true 表示请求内容为JSON flase为普通表单提交
     * @return
     */
    public OkHttpUtils post(boolean isJsonPost){
        RequestBody requestBody;
        if(isJsonPost){
            String json = "";
            if(paramMap != null){
                json = JSON.toJSONString(paramMap);
            }
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            requestBody = RequestBody.create(mediaType,json);
        }else {
            FormBody.Builder formBody = new FormBody.Builder();
            if(paramMap != null){
                paramMap.forEach(formBody::add);
            }
            requestBody = formBody.build();
        }
        request = new Request.Builder().post(requestBody).url(url);
        return this;
    }

    /**
     * 同步请求
     * @return
     */
    public ResultEntry sync(){
        setHeader(request);
        try {
            Response response = okHttpClient.newCall(request.build()).execute();
            assert response.body() != null;
            return new ResultEntry(response.body().string(),200);
        }catch (IOException e){
            e.printStackTrace();
            return new ResultEntry("请求错误！", 500);
        }
    }

    /**
     * 异步请求，有返回值
     */
    public ResultEntry async(){
        ResultEntry result = new ResultEntry();
        setHeader(request);
        okHttpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                result.setValue("请求出错:"+e.getMessage());
                result.setCode(500);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                assert response.body() != null;
                result.setValue(response.body().string());
                result.setCode(200);
                getSemaphoreInstance().release();
            }
        });
        try {
            getSemaphoreInstance().acquire();
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 异步请求 带有接口回调
     * @param callBack
     */
    public void async(OkCallBack callBack){
        setHeader(request);
        okHttpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.onFailure(call, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callBack.onSuccessful(call, response.body().string());
            }
        });
    }
}
