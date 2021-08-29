package com.fuusy.fuperformance.memory.bitmap;

import com.squareup.haha.perflib.ArrayInstance;
import com.squareup.haha.perflib.ClassInstance;
import com.squareup.haha.perflib.Instance;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

/**
 * @Description:
 * @Author: weilu
 * @Time: 2019/1/4 0004 16:12.
 */
public class Tools {

    public static void getStackInfo(Instance instance) {

        while (instance.getNextInstanceToGcRoot() != null) {
            print("      " + instance.getNextInstanceToGcRoot());
            instance = instance.getNextInstanceToGcRoot();
        }
    }

    public static AnalyzerResult getAnalyzerResult(Instance instance) {
        List<ClassInstance.FieldValue> classInstanceValues = ((ClassInstance) instance).getValues();
        ArrayInstance bitmapBuffer = fieldValue(classInstanceValues, "mBuffer");
        int bitmapHeight = fieldValue(classInstanceValues, "mHeight");
        int bitmapWidth = fieldValue(classInstanceValues, "mWidth");
        AnalyzerResult result = new AnalyzerResult();
        try {
            result.setBufferHash(getMd5(getByteArray(bitmapBuffer)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        result.setClassInstance(bitmapBuffer.toString());
        result.setBufferSize(bitmapBuffer.getValues().length);
        result.setWidth(bitmapWidth);
        result.setHeight(bitmapHeight);
        return result;
    }

    public static int getHashCodeByInstance(Instance instance) {
        List<ClassInstance.FieldValue> classInstanceValues = ((ClassInstance) instance).getValues();
        ArrayInstance curBitmapBuffer = fieldValue(classInstanceValues, "mBuffer");
        return Arrays.hashCode(curBitmapBuffer.getValues());
    }


    public static <T> T fieldValue(List<ClassInstance.FieldValue> values, String fieldName) {
        for (ClassInstance.FieldValue fieldValue : values) {
            if (fieldValue.getField().getName().equals(fieldName)) {
                return (T) fieldValue.getValue();
            }
        }
        throw new IllegalArgumentException("Field " + fieldName + " does not exists");
    }

    public static void print(String content) {
        System.out.println(content);
    }

    // 计算md5值
    public static String getMd5(byte[] bytes) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(bytes, 0, bytes.length);
        return byteArrayToHex(md5.digest()).toLowerCase();
    }

    private static String byteArrayToHex(byte[] byteArray) {
        char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        char[] resultCharArray = new char[byteArray.length * 2];
        int index = 0;
        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }
        return new String(resultCharArray);
    }

    public static byte[] getByteArray(ArrayInstance arrayInstance){
        try {
            Method asRawByteArray =
                    ArrayInstance.class.getDeclaredMethod("asRawByteArray", int.class, int.class);
            asRawByteArray.setAccessible(true);
            Field length = ArrayInstance.class.getDeclaredField("mLength");
            length.setAccessible(true);
            int lengthValue = (int)length.get(arrayInstance);
            byte[] rawByteArray = (byte[]) asRawByteArray.invoke(arrayInstance, 0, lengthValue);
            return rawByteArray;
        } catch (Exception e) {
            return null;
        }
    }
}
