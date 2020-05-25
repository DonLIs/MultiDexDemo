package me.donlis.multidexdemo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectUtil {

    public static Field findField(Object instance, String name) throws NoSuchFieldException{
        if(instance == null){
            throw new NullPointerException();
        }

        Class<?> aClass = instance.getClass();
        while(aClass != null){
            try {
                Field field = aClass.getDeclaredField(name);
                if(!field.isAccessible()){
                    field.setAccessible(true);
                }
                return field;
            } catch (NoSuchFieldException e) {
                aClass = aClass.getSuperclass();
            }
        }
        throw new NoSuchFieldException("No such field: "+ name);
    }

    public static Method findMethod(Object instance, String name, Class<?> ...params) throws NoSuchMethodException{
        if(instance == null){
            throw new NullPointerException();
        }

        Class<?> aClass = instance.getClass();
        while (aClass != null){
            try {
                Method method = aClass.getDeclaredMethod(name, params);
                if(!method.isAccessible()){
                    method.setAccessible(true);
                }
                return method;
            } catch (NoSuchMethodException e) {
                aClass = aClass.getSuperclass();
            }
        }
        throw new NoSuchMethodException("No such method: "+ name);
    }

}
