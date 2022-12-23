package com.vincentcodes.webserver.dispatcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vincentcodes.webserver.HttpHandlerRegister;
import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.annotaion.HttpHandler;
import com.vincentcodes.webserver.annotaion.request.RequestMapping;
import com.vincentcodes.webserver.annotaion.wrapper.InvocationCondition;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.response.HttpResponses;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.dispatcher.operation.DispatcherOperation;
import com.vincentcodes.webserver.dispatcher.operation.OperationResult;
import com.vincentcodes.webserver.dispatcher.operation.OperationResultStatus;
import com.vincentcodes.webserver.dispatcher.reflect.ConditionalWrapper;
import com.vincentcodes.webserver.reflect.MethodDecorator;

/**
 * <p>
 * This will be the default class used for dispatching {@link HttpRequest}s. This 
 * class will enumerate all registered objects from {@link HttpHandlerRegister} 
 * and search for {@link RequestMapping @RequestMapping}. It is not recommended
 * to have more than one <b>SAME</b> {@link RequestMapping @RequestMapping}. 2 
 * examples are shown down below.
 * </p>
 * <p>
 * For methods which return objects, if it is in the form as shown below, please
 * note that you may only mutate the headers and response code of the 
 * ResponseBuilder.
 * </p>
 * <pre>
 * &#064;Mutatable
 * &#064;RequestMapping("/song.mp3")
 * public String handleSong(HttpRequest req, ResponseBuilder res) {
 *     res.getHeaders().add("cache-control", "no-store");
 *     return "Testing!";
 * }
 * </pre>
 * 
 * <p>
 * To handle the WHOLE response yourself, you need to use VOID as a return type
 * on your handler. Example code:
 * </p>
 * <pre>
 * // An example of handling the whole Response yourself. (returns VOID)
 * // Remember to deal with Body (if needed) and Headers which is essential
 * // to communicate with HTTP.
 * &#064;GetHandler
 * &#064;RequestMapping("/song.mp3")
 * public void catchAllHandler(HttpRequest req, ResponseBuilder res){
 *      File file = new File("./song.mp3");
 *      
 *      HttpHeaders headers = res.getHeaders();
 *      headers.add("cache-control", "no-store");
 *      headers.add("content-disposition", "attachment; filename=" + file.getName());
 *      headers.add("content-length", Long.toString(file.length()));
 *      headers.add("content-type", "application/octet-stream");
 * 
 *      HttpBody body = res.getBody();
 *      try(FileInputStream is = new FileInputStream(file)){
 *          byte[] buffer = new byte[(int)file.length()];
 *          is.read(buffer);
 *          body.writeToBody(buffer);
 *      }catch(IOException e){
 *          throw new RuntimeException("Cannot read file: " + file.getAbsolutePath(), e);
 *      }
 *  }
 * </pre>
 * 
 * @see com.vincentcodes.webserver.defaults.DefaultHandler
 * @see com.vincentcodes.webserver.defaults.DownloadOnlyHandler
 * @see RequestMapping
 * @see HttpHandler
 */
public class HttpRequestDispatcher extends Dispatcher<HttpRequest, ResponseBuilder, MethodDecorator> {
    
    private HttpRequestDispatcher(List<DispatcherOperation<HttpRequest, ResponseBuilder, MethodDecorator>> operations){
        super(operations);
    }

    public static HttpRequestDispatcher createInstance(List<DispatcherOperation<HttpRequest, ResponseBuilder, MethodDecorator>> operations) {
        throwErrorIfDuplicatesFound();
        return new HttpRequestDispatcher(operations);
    }

    // pre-processing
    private static void throwErrorIfDuplicatesFound(){
        Map<String, List<MethodDecorator>> requestMappingToMethods = new HashMap<>();
        List<MethodDecorator> methods = HttpHandlerRegister.getRegistry().findAllMethodsWithAnnotation(RequestMapping.class);
        
        for(MethodDecorator firstMethod : methods){
            Set<Class<? extends Annotation>> requestMethods1 = getRequestMethodHttpAnno(firstMethod);
            String requestMapping1 = getClassRequestMapping(firstMethod) + firstMethod.getAnnotation(RequestMapping.class).value();
            requestMapping1 = requestMapping1.replace("//", "/");

            if(requestMappingToMethods.containsKey(requestMapping1)){
                for(MethodDecorator secondMethod : requestMappingToMethods.get(requestMapping1)){
                    if(firstMethod.equals(secondMethod))
                        continue;
                    Set<Class<? extends Annotation>> requestMethods2 = getRequestMethodHttpAnno(secondMethod);
                    String requestMapping2 = getClassRequestMapping(secondMethod) + secondMethod.getAnnotation(RequestMapping.class).value();
                    requestMapping2 = requestMapping2.replace("//", "/");
                    // If request mapping is the same (eg. both has "/song") and
                    // they have common request method (eg. both has @HttpGet)
                    if(requestMapping1.equals(requestMapping2) && hasCommonElement(requestMethods1, requestMethods2)
                    && !differentInvocationCondition(firstMethod, secondMethod)){
                        throw new IllegalStateException("Duplicate or conflicting request mapping is not allowed. Problem occured between these 2 methods: " + firstMethod.get().toGenericString() + " ; " + secondMethod.get().toGenericString());
                    }
                }
                requestMappingToMethods.get(requestMapping1).add(firstMethod);
            }else{
                // This list is used to keep track of whether they handles different
                // Http methods (eg. the each handler handle @HttpGet, @HttpPost seperately)
                List<MethodDecorator> list = new ArrayList<>();
                list.add(firstMethod);
                requestMappingToMethods.put(requestMapping1, list);
            }
        }
        requestMappingToMethods.clear();
        methods.clear();
    }
    /**
     * @return empty if no RequestMapping annotation is found on clazz
     */
    private static String getClassRequestMapping(MethodDecorator method){
        RequestMapping classReqMap = method.getParent().getAnnotation(RequestMapping.class);
        return classReqMap == null? "" : classReqMap.value();
    }
    /**
     * Get annotations that will be checked for &#64;RequestMapping duplicates
     */
    private static Set<Class<? extends Annotation>> getRequestMethodHttpAnno(MethodDecorator method){
        Set<Class<? extends Annotation>> annotations = new HashSet<>(WebServer.SUPPORTED_REQUEST_METHOD.values());
        List<Class<? extends Annotation>> toBeRemoved = new ArrayList<>();
        // no HttpXXX counts as ALL
        if(method.hasAnyOneAnnotation(WebServer.SUPPORTED_REQUEST_METHOD.values())){
            for(Class<? extends Annotation> anno : annotations){
                if(!method.hasAnnotation(anno)){
                    toBeRemoved.add(anno);
                }
            }
        }
        for(Class<? extends Annotation> anno : toBeRemoved){
            annotations.remove(anno);
        }
        return annotations;
    }
    /**
     * We look for exactly the same invocation condition
     */
    private static boolean differentInvocationCondition(MethodDecorator method1, MethodDecorator method2){
        if(!method1.hasAnnotation(InvocationCondition.class) && !method2.hasAnnotation(InvocationCondition.class))
            return false;
        if(!method1.hasAnnotation(InvocationCondition.class) || !method2.hasAnnotation(InvocationCondition.class))
            return true;
        
        Set<Class<?>> classes = new HashSet<>();
        Class<? extends ConditionalWrapper>[] method1Conditions = method1.getAnnotation(InvocationCondition.class).value();
        Class<? extends ConditionalWrapper>[] method2Conditions = method2.getAnnotation(InvocationCondition.class).value();
        if(method1Conditions.length != method2Conditions.length)
            return true;
        for(Class<?> clazz : method1Conditions){
            classes.add(clazz);
        }
        int noOfMatches = 0;
        for(Class<?> clazz : method2Conditions){
            if(classes.contains(clazz))
                noOfMatches++;
        }
        return noOfMatches != method1Conditions.length;
    }
    private static <T> boolean hasCommonElement(Set<T> set1, Set<T> set2){
        return set1.stream().anyMatch(ele -> set2.contains(ele));
    }

    /**
     * Important: if you have any handlers that contain super wildcard (eg. "/**"), 
     * please be careful since it is <b>VERY</b> likely that it will consume all 
     * requests and return 404. Also, consider not using default handlers.
     */
    @Override
    public ResponseBuilder dispatchObjectToHandlers(HttpRequest request) throws InvocationTargetException{
        for(DispatcherOperation<HttpRequest, ResponseBuilder, MethodDecorator> operation : super.operations){
            OperationResult<ResponseBuilder> result = operation.start(request);
            if(result.status() == OperationResultStatus.FAILURE){
                continue;
            }else{
                // error should not be handled again (OperationResultStatus.ERROR)
                return result.get();
            }
        }
        return HttpResponses.generate404Response();
    }
}
