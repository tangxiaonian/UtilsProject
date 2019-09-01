package com.epoint.projectissues.utils;

import java.io.IOException;
import java.io.PrintStream;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MapperUtils {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static ObjectMapper getInstance() {
        return OBJECT_MAPPER;
    }

    /**
            * 对象转换为json字符串
     * @param entity
     * @return
     */
    public static String ObjectToJson(Object entity) {

        try {
            return OBJECT_MAPPER.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";

    }

    /**
     * 对象序列化后 输出 到指定输出流
     * @param printStream
     * @param entity
     * @throws Exception
     */
    public static void ObjectToJson2(PrintStream printStream, Object entity) throws Exception {
        JsonGenerator generator = OBJECT_MAPPER.getFactory()
                .createGenerator(printStream,JsonEncoding.UTF8);
        generator.writeObject(entity);
    }


    /**
     * json 转  java 实体类
     * @param json
     * @param entity
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T JsonToObject(String json,Class<T> entity) throws IOException {
        return OBJECT_MAPPER.readValue(json, entity);
    }

}
