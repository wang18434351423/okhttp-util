package com.yyf.okhttputil.modular;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.JSONLibDataFormatSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author wangchen
 * @create 2020/05/15/14:17
 */
public class JsonFormatUtil extends JSON implements Serializable {

    private static final SerializeConfig CONFIG;

    //配置初始化化
    static {
        CONFIG = new SerializeConfig();
        // 使用和json-lib兼容的日期输出格式
        CONFIG.put(java.util.Date.class, new JSONLibDataFormatSerializer());
        // 使用和json-lib兼容的日期输出格式
        CONFIG.put(java.sql.Date.class, new JSONLibDataFormatSerializer());
    }

    /**
     * 配置默认字段输出(当字段值为空时，输出默认值)
     */
    private static final SerializerFeature[] FEATURES = {
            SerializerFeature.WriteMapNullValue,
            // list字段如果为null，输出为[]，而不是null
            SerializerFeature.WriteNullListAsEmpty,
            // 数值字段如果为null，输出为0，而不是null
            SerializerFeature.WriteNullNumberAsZero,
            // Boolean字段如果为null，输出为false，而不是null
            SerializerFeature.WriteNullBooleanAsFalse,
            // 字符类型字段如果为null，输出为""，而不是null
            SerializerFeature.WriteNullStringAsEmpty
    };

    public static JSONObject getFastJson(){
        return new JSONObject();
    }

    public static JSONArray getFastJsonArray(){
        return new JSONArray();
    }

    /**
     * 类转JSON字符串 (时间复杂化处理，并且会打印空属性)
     * @param object
     * @return
     */
    public static String toJSONStringTF(Object object){
        return JSON.toJSONString(object, CONFIG, FEATURES);
    }

    /**
     * 类转JSON字符串 (时间复杂化处理，并且不会打印空属性)
     * @param object
     * @return
     */
    public static String toJSONStringT(Object object){
        return JSON.toJSONString(object,CONFIG );
    }

    /**
     * 类转JSON字符串 会打印对象中所有的属性，没值得直接为空
     * @param object
     * @return
     */
    public static String toJSONStringAF(Object object){
        return JSON.toJSONString(object);
    }

    /**
     * 类转JSON字符串 只打印对象中有值的，没有值的不打印
     * @param object
     * @return
     */
    public static String toJSONStringNAF(Object object){
        ParserConfig.getGlobalInstance().setAsmEnable(false);
        return JSON.toJSONString(object,FEATURES);
    }

    /**
     * JSON字符串转为Object对象
     * @param source
     * @return
     */
    public static Object parse(String source){
        return JSON.parse(source);
    }

    /**
     * JSON字符串转为指定类型的对象
     * @param source
     * @param targetClass
     * @param <T>
     * @return
     */
    public static <T> T parseToObject(String source,Class<T> targetClass){
        return JSON.parseObject(source, targetClass);
    }

    /**
     * JSON字符串转数组
     * @param source
     * @param <T>
     * @return
     */
    public static <T> Object[] parseToArray(String source){
        return parseToArray(source,null);
    }

    /**
     * JSON字符串转为指定类型的对象数组
     * @param source
     * @param targetClass
     * @param <T>
     * @return
     */
    public static <T> Object[] parseToArray(String source,Class<T> targetClass){
        return JSON.parseArray(source, targetClass).toArray();
    }

    /**
     * JSON字符串转为List
     * @param source
     * @param targetClass
     * @param <T>
     * @return
     */
    public static <T> List<T> parseToList(String source,Class<T> targetClass){
        return JSON.parseArray(source, targetClass);
    }

    /**
     * 将string转化为序列化的json字符串
     * @return
     */
    public static Object strToJson(String str) {
        return JSON.parse(str);
    }

    /**
     * json字符串转化为map
     * @param str
     * @return
     */
    public static <K, V> Map<K, V> strToMap(String str) {
        return (Map<K, V>) JSONObject.parseObject(str);
    }

    /**
     * 将map转化为JSON字符串
     * @param map
     * @return
     */
    public static <K, V> String mapToStr(Map<K, V> map) {
        return JSONObject.toJSONString(map);
    }

}
