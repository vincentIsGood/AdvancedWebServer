package com.vincentcodes.webserver.helper;

import java.util.List;

import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.reflect.FieldDecorator;

public class FieldsInjector {
    private final List<FieldDecorator> fields;
    private final ObjectPool objectPool;
    
    public FieldsInjector(List<FieldDecorator> fields, ObjectPool objectPool) {
        this.fields = fields;
        this.objectPool = objectPool;
    }
    public FieldsInjector(List<FieldDecorator> fields){
        this(fields, new ObjectPool());
    }

    /**
     * Create objects according to what types the fields we have here.
     */
    public void createMissingObjectForFields() {
        for(FieldDecorator field : fields){
            if(!objectPool.hasInstanceOf(field.type()) 
            && objectPool.getButCreateIfAbsent(field.type()) == null)
                WebServer.logger.warn("Cannot create object of type: " + field.type() + ". Skipping");
        }
    }

    /**
     * Inject objects into the fields using object pool
     * 
     * For example:
     * <pre>
     * String someString;
     * </pre>
     * <code>injectObjectIntoType(String.class, "Some content")</code>
     */
    public void inject(){
        try{
            for(FieldDecorator field : fields){
                if(!objectPool.hasInstanceOf(field.type()))
                    continue;
                field.setAccessible(true);
                field.setValue(objectPool.getInstanceOf(field.type()));
            }
        }catch(IllegalArgumentException | IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

    public ObjectPool getObjectPool(){
        return objectPool;
    }
}
