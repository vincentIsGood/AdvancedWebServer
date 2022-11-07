package com.vincentcodes.webserver.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

// I can't think of a good name, help.
public class MethodDecorator {
    private Object owner;
    private Method method;
    private ClassDecorator parent;

    public MethodDecorator(Object owner, Method method) {
        this.owner = owner;
        this.method = method;
        parent = new ClassDecorator(owner, owner.getClass());
    }

    public Object getOwner(){
        return owner;
    }

    public ClassDecorator getParent(){
        return parent;
    }

    public Method get(){
        return method;
    }

    public void setAccessible(boolean flag){
        method.setAccessible(flag);
    }

    // quicker (not much though)
    public Object quickInvoke(Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        setAccessible(true);
        return invoke(args);
    }

    public Object invoke(Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        return method.invoke(owner, args);
    }

    public Parameter[] getParameter(){
        return method.getParameters();
    }

    public int getParameterCount(){
        return method.getParameterCount();
    }

    /**
     * "parameters in form of" can be illustrated using this example...
     * <pre>
     * public void somefunction(int a, String b, InputStream c){...}
     * 
     * somefunction.parametersInFormOf(int.class, String.class, InputStream.class) == true
     * </pre>
     * @param objects
     * @return
     */
    public boolean parametersInFormOf(Class<?>... types){
        if(types.length == method.getParameterCount()){
            Class<?>[] clazzes = method.getParameterTypes();
            for(int i = 0; i < clazzes.length; i++){
                if(!clazzes[i].equals(types[i]))
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Find parameter position based on annotation type. For example, for
     * <code>public void foo(int a, @CustomAnnotation String b);</code>
     * the annotation position (or index of the annotation) is 1
     * @param annotationType position type
     * @return parameter position / index of that parameter. -1 if not found
     */
    public int positionOfParameter(Class<? extends Annotation> annotationType){
        for(int i = 0; i < method.getParameterCount(); i++){
            Parameter parameter = method.getParameters()[i];
            if(parameter.isAnnotationPresent(annotationType)){
                return i;
            }
        }
        return -1;
    }

    /**
     * inefficient? It loops everytime.
     */
    public boolean hasParameter(Class<? extends Annotation> annotationType){
        return positionOfParameter(annotationType) != -1;
    }

    public boolean returnsVoid(){
        return method.getReturnType().equals(Void.TYPE);
    }
    
    /**
     * All of the annotations must exist inside this method to return true
     */
    public boolean hasAnnotations(Iterable<Class<? extends Annotation>> annotations){
        boolean allExists = true;
        for(Class<? extends Annotation> annotation : annotations){
            if(!hasAnnotation(annotation)){
                return !allExists;
            }
        }
        return allExists;
    }

    /**
     * !hasAnyOneAnnotation -> None
     */
    public boolean hasAnyOneAnnotation(Iterable<Class<? extends Annotation>> annotations){
        for(Class<? extends Annotation> annotation : annotations){
            if(hasAnnotation(annotation)){
                return true;
            }
        }
        return false;
    }

    public boolean hasAnnotation(Class<? extends Annotation> annotation){
        return method.isAnnotationPresent(annotation);
    }

    /**
     * @param annotation Any annotation 
     * @return the annotation or null if the specified annotation is not found
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotation){
        if(hasAnnotation(annotation))
            return method.getAnnotation(annotation);
        return null;
    }
}
