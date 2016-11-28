package com.thed.zephyr.api.play;


import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import scala.runtime.ArrayCharSequence;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RouteParser  {

    Logger logger = Logger.getLogger(RouteParser.class.getName());

    private static String packageName="com.thed.zephyr.connect.controllers";

    private String vmFile = "apiary.vm";

    private static String BASE_PATH = "../zfjconnect/target/scala-2.10/classes/";

    private String outputFileName="target/apiary.txt";

    public RouteParser(String packageName, String BASE_PATH){
        this.packageName = packageName;
        this.BASE_PATH=BASE_PATH;
    }
    /**
     * Main method to test Doc
     * @param args
     */
    public static void main(String [] args) {

        RouteParser fw = new RouteParser(packageName,BASE_PATH);
        fw.generateApiaryDoc();

    }

    /**
     * Generate Apiary Document file.
     */
    public void generateApiaryDoc() {
        String path = RouteParser.BASE_PATH;
        List<Resource> list = new ArrayList<Resource>();
         String line = null;
         try {
            File file = new File(path);
            URL[] cp = {file.toURI().toURL()};
            URLClassLoader urlClassLoader = new URLClassLoader(cp);
            FileReader fileReader = new FileReader(BASE_PATH.concat("routes"));
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            HashMap<String,Resource> hashMap = new HashMap<>();
            Set<String> classSet = new TreeSet<>();
            HashMap<String, List<String>> resourceList1 = new HashMap<>();
            while((line = bufferedReader.readLine()) != null) {
                      if (StringUtils.startsWithAny(line, new String[]{"GET", "POST", "PUT", "DELETE"})) {
                        String httpMethod = getHttpMethod(line);
                        String resource = getResourceUrl(line);
                        String actualMethod = getActualMethod(line);
                        String clazz = getClassName(actualMethod);

                        //First Filter Map:Start
                        List<String> additionalResource = new ArrayList<String>();
                        additionalResource.add(httpMethod);
                        additionalResource.add(actualMethod);
                        if(clazz.length()>0) {
                            Map<String, String> pathParams = getPathAndQueryParams(resource, actualMethod).get(0);
                            Map<String, String> queryParams = getPathAndQueryParams(resource, actualMethod).get(1);
                            if (null != pathParams && !pathParams.isEmpty()) {
                                for (String paramName : pathParams.keySet()) {
                                    resource = modifyPathParamInResource(resource, paramName);
                                }
                            }
                            if (null != queryParams && !queryParams.isEmpty()) {
                                resource = modifyQueryParamInResource(resource, queryParams.keySet());
                            }
                            if(resource.length() > 10) {
                                resourceList1.put(httpMethod + "_" + resource, additionalResource);
                            }
                        }
                            //First Filter Map:End

                            if(!clazz.equals("") && !clazz.equals("lang"))
                            classSet.add(clazz);

                    }
                }

            // Always close files.
            bufferedReader.close();

            //resource loop
            for(String k: classSet){

                //Load Class
                Class<?> loadedClass =  urlClassLoader.loadClass(packageName+"."+k);

                //Create Resource/Group Name
                Resource r = new Resource();
                List<Operation> ops = new ArrayList<Operation>();
                r.setName(extractControllerPrefix(k));

                    for(Map.Entry<String, List<String>> entry : resourceList1.entrySet()) {
                        String key = entry.getKey().split("_")[1];
                        List<String> ar = entry.getValue();
                        String httpMethod = ar.get(0);
                        String actualMethod = ar.get(1);
                        String className = getClassName(actualMethod);
                        String methodName = getMethodName(actualMethod);

                        Map<String, String> pathParams  = getPathAndQueryParams(key,actualMethod).get(0);
                        Map<String,String>  queryParams = getPathAndQueryParams(key,actualMethod).get(1);
                        Method method = getOperationMethod(loadedClass,methodName);
                        List<String> jreq=new ArrayList<>(),jres=new ArrayList<>();
                        String rq = "{\n" +
                                "  "      +
                                "  "      +
                                "       }";
                        String rs = "{\n" +
                                "  "      +
                                "  "      +
                                "       }";
//                        jreq.add(rq);jres.add(rs);

                        if(k.equals(className)){
                            //Create Operations
                            Operation op = new Operation();

                            pathParams.forEach((kpp,vpp)-> {
                                PathParameter pp = new PathParameter();
                                pp.setName(kpp);
                                pp.setDescription(kpp+" of "+extractControllerPrefix(className));
                                pp.setType(vpp);
                                pp.setValue("default");
                                pp.setIsRequired("required");
                                op.addPathParam(pp);
                            });

                            List<QueryParameter> lqp = new ArrayList<QueryParameter>();
                            queryParams.forEach((kqp,vqp)-> {
                                QueryParameter qp = new QueryParameter();
                                qp.setName(kqp);
                                qp.setDescription(kqp+" of "+extractControllerPrefix(className));
                                if (vqp.contains("?=") || vqp.contains("Option[")) {
                                    qp.setIsRequired("optional");
                                    if (vqp.contains("?=")) {
                                        vqp = vqp.substring(0, vqp.indexOf('?'));
                                    } if (vqp.contains("Option[")) {
                                        vqp = vqp.substring(vqp.indexOf('[') + 1, vqp.indexOf(']'));
                                    }
                                }
                                else {
                                    qp.setIsRequired("required");
                                }
                                qp.setType(vqp);
                                lqp.add(qp);
                            });

                            op.setName(WordUtils.capitalize(methodName.replaceAll("([A-Z])"," $1"))+ " ");
                            op.setSummary(methodName.replaceAll("([A-Z])"," $1")+" ");
                            op.setDescription(methodName.replaceAll("([A-Z])"," $1")+" ");
                            op.setRequestType(httpMethod.trim());
                            op.setConsumes("application/json");
                            op.setProduces("application/json");
                            op.setPath("/"+key.replaceAll("[\\n\\t ]", ""));
                            op.setQueryParams(lqp);
                            op.setResponseCode("200");
                            op.setJsonRequest(jreq);
                            op.setJsonResponse(jres);

                            ApiOperation apo = method.getAnnotation(ApiOperation.class);
                            ApiImplicitParams aps = method.getAnnotation(ApiImplicitParams.class);
                            if (apo != null) {
                                op.setName(apo.value());
                                op.setSummary(apo.value());    // don't have description, duplicating name value
                                if (StringUtils.isNotBlank(apo.notes())) {
                                    op.setDescription(apo.notes() + " ");
                                } else {
                                    op.setDescription(apo.value() + " ");
                                }

                                //set produces
                                if (!apo.produces().isEmpty()) {
                                    op.setProduces(apo.produces());
                                }

                                //set consumes
                                if (!apo.consumes().isEmpty()) {
                                    op.setConsumes(apo.consumes());
                                }

                                if (aps != null) {
                                    ApiImplicitParam[] ap = aps.value();

                                    //set request dummy json
                                    jreq.add(getJsonData(ap, "request"));
                                    op.setJsonRequest(jreq);

                                    //set response dummy json
                                    jres.add(getJsonData(ap, "response"));
                                    op.setJsonResponse(jres);
                                }

                             /*else {
                                op.setName(methodName+" ");
                                op.setDescription(methodName+" ");
                            }*/

                                //finally add operation into ops list
                                ops.add(op);
                            }
                        }
                    }
                if (ops != null && !ops.isEmpty()) {
                    r.setGroupNotes(k + "");
                    r.setPath(" ");
                    r.setConsumes("application/json");
                    r.setConsumes("application/json");
                    r.setOperations(ops);

                    list.add(r);
                }
            }

            //finally generate docs from resource list
            generateDocFile(list);

        }catch (Exception e){
            System.out.println("Error: "+e);
        }

    }

    private String modifyPathParamInResource(String path, String paramName) {
        if (path.contains(paramName)) {
            path = path.replace(":" + paramName, "{" + paramName + "}");
        }
        return path;
    }
    private String modifyQueryParamInResource(String path, Set<String> params) {
        if (null != params && !params.isEmpty()) {
            Iterator<String> setItr = params.iterator();
            path = path + "{?" + setItr.next();
            params.remove(0);
            while (setItr.hasNext()) {
                path = path + "," + setItr.next();
            }
            path = path + "}";
        }
        return path;
    }

    /**
     *
     * @param aps
     * @param jsonType
     * @return
     */
    private String getJsonData(ApiImplicitParam[] aps, String jsonType) {
        String result = "";
        for(ApiImplicitParam ap: aps){
            if(ap.name().equals(jsonType)){
                result = ap.value();
            }
        }
        return result;
    }

    /**
     * Get Method Object from Class
     * @param loadedClass
     * @param methodName
     * @return
     */
    private Method getOperationMethod(Class<?> loadedClass, String methodName) {
        Method[] methods = loadedClass.getMethods();
        Method method=null;
        for(Method m: methods){
            if(m.getName().equals(methodName)){
                 method = m;
            }
        }
        return method;
    }


    /**
     * Get Resource Meta Data
     * @param clazz
     * @return
     * @throws IOException
     */
    private Resource getResourceMetadata(Class clazz) throws IOException {
        Resource r = new Resource();
        for (Method m: clazz.getMethods())  {
            getOperationMetadata(r, m);
        }
        return r ;
    }

    /*
	@GET
	@Path("/{id}")
	@ApiOperation(value = "Get testcase by ID", //notes = "Add extra notes here",
					responseClass = "com.thed.rpc.bean.RemoteRepositoryTreeTestcase")
	@ApiErrors(value = { @ApiError(code = 400, reason = "Invalid ID supplied"),
							@ApiError(code = 404, reason = "Testcase not found") })
	*/
    private void getOperationMetadata(Resource r, Method m) throws IOException {
        Path path = (Path) m.getAnnotation(Path.class);
        if (path != null) {
            Operation op = new Operation();

            if (m.getAnnotation(GET.class) != null) {
                op.setRequestType("GET");
            } else if (m.getAnnotation(POST.class) != null) {
                op.setRequestType("POST");
            } else if (m.getAnnotation(PUT.class) != null) {
                op.setRequestType("PUT");
            } else if (m.getAnnotation(DELETE.class) != null) {
                op.setRequestType("DELETE");
            }
            op.setPath(supressDuplicateSlash(r.getPath() + "/" + path.value()));

            ApiOperation api = (ApiOperation) m.getAnnotation(ApiOperation.class);
            if (api != null) {
                op.setName(api.value());
				/*if (StringUtils.isNotBlank(api.notes())) {
					op.setDescription(api.notes());
				} else {*/
                op.setSummary(api.value());	// don't have description, duplicating name value
                //				}
                if (StringUtils.isNotBlank(api.notes())) {
                    op.setDescription(api.notes());
                } else {
                    op.setDescription("TODO: please add Notes");
                }

            } else {
                op.setName("TODO: please add description");
                op.setDescription("TODO: please add description");
            }

            // use Resource's annotation if required
            if (m.getAnnotation(Produces.class) != null) {
                Produces produces = (Produces) m.getAnnotation(Produces.class);
                op.setProduces(StringUtils.join(produces.value(), " "));
            } else {
                op.setProduces(r.getProduces());
            }

            if (m.getAnnotation(Consumes.class) != null) {
                Consumes consumes = (Consumes) m.getAnnotation(Consumes.class);
                op.setConsumes(StringUtils.join(consumes.value(), " "));
            } else {
                op.setConsumes(r.getConsumes());
            }

            if (r.getOperations() == null) {
                r.setOperations(new ArrayList<Operation>());
            }
            r.getOperations().add(op);
            op.setJsonRequest(getRequestResponse(r,m,op,new String("request")));
            op.setJsonResponse(getRequestResponse(r,m,op,new String("response")));
            op.setResponseCode("200");
            getUrlParameter(r, op, m);
        }
    }

    private void getUrlParameter(Resource r, Operation op, Method m) {
        Annotation[][] pa = m.getParameterAnnotations() ;
//		System.out.println(pa);

		/* E.g. AttachmentResource  */
		/*
		public List<RemoteAttachment> getAttachments(
				@ApiParam(value = "Id of entity which need to be fetched", required = true)
				@QueryParam("entityid") String entityId,
				@ApiParam(value = "Entity name, possible values : testcase, requirement, testStepResult, releaseTestSchedule")
				@QueryParam("entityname") String entityName,
				@ApiParam(value = "Token stored in cookie, fetched automatically if available", required = false)
				@CookieParam("token") Cookie tokenFromCookie) throws ZephyrServiceException;
		*/
        Class[] params = m.getParameterTypes() ;

        StringBuilder queryParamsPath = new StringBuilder();
//		TypeVariable<Method>[] tvm = m.getTypeParameters();
        for (int i = 0; i < pa.length; i++) {
            Annotation[] eachParam = pa[i] ;
            // ignore ApiParam or PathParam or CookieParam ignore
            QueryParam qpAnnotation = hasQueryParam(eachParam) ;

            if (qpAnnotation != null) {

                if (op.getQueryParams() == null) {
                    List<QueryParameter> queryParams = new ArrayList<QueryParameter>();
                    op.setQueryParams(queryParams);

                }
				System.out.println(qpAnnotation.value());
                QueryParameter qParam = new QueryParameter();
                qParam.setName(qpAnnotation.value());
                qParam.setType(params[i].getSimpleName());
                qParam.setDescription(getApiDescription(eachParam));
                qParam.setIsRequired(getApiRequiredValue(eachParam));
                queryParamsPath.append(qpAnnotation.value() + ",");
                op.getQueryParams().add(qParam);
            }

            PathParam pathParamAnno = hasPathParam(eachParam) ;

            if (pathParamAnno != null) {
                PathParameter pathParam = new PathParameter();
                pathParam.setName(pathParamAnno.value());
                pathParam.setType(params[i].getSimpleName());
                pathParam.setDescription(getApiDescription(eachParam));
                pathParam.setIsRequired("required");
                pathParam.setValue(pathParamAnno.value());
                op.addPathParam(pathParam);
            }


            Context contextAnnotation = hasContextAnnotation(eachParam) ;

            if (contextAnnotation != null) {
                List<String> names = new ArrayList<String>();
                List<String> types = new ArrayList<String>();
                List<String> descriptions = new ArrayList<String>();
                String allowableValues = getApiAllowableValues(eachParam);
                int lengthOfQP = parseAllowableValues(names, types, descriptions, allowableValues);
                if (op.getQueryParams() == null) {
                    List<QueryParameter> queryParams = new ArrayList<QueryParameter>();
                    op.setQueryParams(queryParams);

                }
                for (int j=0; j<lengthOfQP; j++) {
                    QueryParameter qParam = new QueryParameter();
                    qParam.setName(names.get(j));
                    qParam.setType(types.get(j));
                    qParam.setDescription(descriptions.get(j));
                    qParam.setIsRequired(getApiRequiredValue(eachParam));
                    queryParamsPath.append(names.get(j) + ",");
                    op.getQueryParams().add(qParam);
                }

            }

        }
        if (queryParamsPath.length()>0) {
            if (op.getPath().endsWith("/")) {
                int index = op.getPath().lastIndexOf("/");
                op.setPath(op.getPath().substring(0,index));
            }
            String path = "{?" + queryParamsPath.deleteCharAt(queryParamsPath.lastIndexOf(","))+"}";
            op.setPath(op.getPath() + path);
        }
//		System.out.println(op.getPath());


    }

    public int parseAllowableValues(List names, List types, List descriptions, String allowableValues) {
        String[] params = StringUtils.split(allowableValues, ",");
        if(params==null) return 0;
//		allowableValues = "id:number:Id of cycle, name:String: Name of cycle, build:String:Build of cycle, environment:String:Environment of cycle, startDate:Date:Start date of cycle, endDate:Date:End date of cycle, releaseId:Number:Release id of cycle"
        for(String param : params) {
            names.add(StringUtils.substringBefore(param, ":"));
            types.add(StringUtils.substringBetween(param, ":", ":"));
            descriptions.add(StringUtils.substringAfterLast(param, ":"));
        }
        return params.length;
    }
    private String getApiAllowableValues(Annotation[] paramAnnotaions) {
        for (Annotation ax: paramAnnotaions) {
            if (ax instanceof ApiParam) {
                return ((ApiParam) ax).allowableValues();
            }
        }
        return null ;
    }


    private String getApiRequiredValue(Annotation[] paramAnnotaions) {
        for (Annotation ax: paramAnnotaions) {
            if (ax instanceof ApiParam) {
                if (((ApiParam) ax).required()) {
                    return "required";
                }
            }
        }
        return "optional";
    }

    private QueryParam hasQueryParam(Annotation[] paramAnnotaions) {
        for (Annotation ax: paramAnnotaions) {
            if (ax instanceof QueryParam) {
                return (QueryParam) ax ;
            }
        }
        return null ;
    }
    private PathParam hasPathParam(Annotation[] paramAnnotaions) {
        for (Annotation ax: paramAnnotaions) {
            if (ax instanceof PathParam) {
                return (PathParam) ax ;
            }
        }
        return null ;
    }

    private Context hasContextAnnotation(Annotation[] paramAnnotaions) {
        for (Annotation ax: paramAnnotaions) {
            if (ax instanceof Context) {
                return (Context) ax ;
            }
        }
        return null ;
    }

    private String getApiDescription(Annotation[] paramAnnotaions) {
        for (Annotation ax: paramAnnotaions) {
            if (ax instanceof ApiParam) {
                return ((ApiParam) ax).value() ;
            }
        }
        return "TODO: please provide a description" ;
    }


    private String supressDuplicateSlash(String s) {
        String rep = s.replaceAll("//", "/");
        return rep ;
    }

    // Get the requst/response from a text file on a particular location src/main/resources/apidocs/<resource>/<method>/<request>/response.json e.g. src/main/resources/apidocs/attachment/getAttachment/GET/response.json
    private List<String> getRequestResponse(Resource r, Method m, Operation op, String exampleString) throws IOException {
        String file = null;
        FileInputStream fstream;
        if (exampleString.equals("request")) {
            if (m.getName().equals("getManifest")) {
                file =  BASE_PATH + "manifest" + "/" +  m.getName() + "/" + op.getRequestType() +"/" + "request.json";
            } else {
                file = BASE_PATH + r.getPath() + "/" +  m.getName() + "/" + op.getRequestType() +"/" + "request.json";
            }
        }else {
            if (m.getName().equals("getManifest")) {
                file = BASE_PATH + "manifest" + "/" +  m.getName() + "/" + op.getRequestType() +"/" + "response.json";
            } else {
                file = BASE_PATH + r.getPath() + "/" +  m.getName() + "/" + op.getRequestType() +"/" + "response.json";
            }
        }
        List<String> list = new ArrayList<String>();
        File fileReal = new File(file);
        if(!fileReal.exists()) {
            return list;
        }
        fstream = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        String line;
        while ((line = br.readLine()) != null) {
            list.add(line);
        }
        br.close();
        fstream.close();
        return list;
    }
    private String extractResourcePrefix(String s) {
        String[] sa = StringUtils.split(s, ".");
        String resourceName = sa[sa.length-1];
        String name = resourceName.substring(0, resourceName.indexOf("Resource"));
        return name ;
    }


    private String extractControllerPrefix(String str){
        return str.replace("Controller","");
    }

    public List<Resource> generateResourceList(){

        Collection<Class<?>> sortedTypes = getResourceClasses();
//		/*try {
//			sortedTypes = new Util().getClasses(packageName);
//		} catch (ClassNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}//getResourceClasses();
//*/



        List<Resource> list = new ArrayList<Resource>();
        for(Class type : sortedTypes){
            System.out.println("type: Masud");

            if(type.getName().startsWith(packageName) && type.isInterface()){
                try {
                    list.add(getResourceMetadata(type));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return list;
    }

    public File generateDocFile(List<Resource> list) {
        //All resources
//        System.out.println("All resources");
//        for (Resource resource : list) {
//			System.out.println(resource.getName());
//		}
        File file = generateDocs(list);
        return file;
    }
    /**
     *
     * @param resources
     * @return
     */
    private File generateDocs(List<Resource> resources) {
        Velocity.init();
        VelocityContext context = new VelocityContext();
        context.put("name", new String("Velocity"));
        Template template = null;

        context.put("resources", resources);
        context.put("DOUBLE_HASH", "##");
        context.put("TRIPLE_HASH", "###");
        PrintWriter pw = null;
        try {
            VelocityEngine ve = new VelocityEngine();
            ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            template = ve.getTemplate(vmFile);
            StringWriter sw = new StringWriter();
            template.merge(context, sw);
            File file = new File(outputFileName);
            pw = new PrintWriter(file);
            pw.write(sw.toString());
            pw.flush();
            logger.fine("Log file is generated " + outputFileName);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            pw.close();
        }
        return null;
    }


    private List<Class<?>> getResourceClasses() {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forJavaClassPath())
        );

        Set<Class<?>> types = reflections.getTypesAnnotatedWith(Path.class);

        Ordering<Class> order = new Ordering<Class>() {
            @Override
            public int compare(Class left, Class right) {
                String leftName = StringUtils.substringAfterLast(left.getName(), ".");
                String rightName = StringUtils.substringAfterLast(right.getName(), ".");
                return leftName.compareTo(rightName);
            }
        };


        List<Class<?>> sortedTypes = order.sortedCopy(Iterables.filter(types, new Predicate<Class<?>>() {
            public boolean apply(Class<?> input) {
                return input.getName().startsWith(packageName);
            }
        }));
        return sortedTypes;
    }

    /**
     * Get Method Name from String
     * @param str
     * @return
     */
    public String getMethodName(String str){
        String split = "\\(";
        if(null != str && str.split(split).length>0){
            String[] strs = str.split(split);
            String[] rs =strs[0].split(Pattern.quote("."));
            str= rs[rs.length-1];
        }
        return str;
    }

    /**
     * Get Package and Method Name from String
     * @param line
     * @return
     */
    public String getActualMethod(String line) {
        if(null != line){
            if(null != StringUtils.split(line, "@")){
                if(StringUtils.split(line, "@").length > 1){
                    line=StringUtils.split(line, "@")[1];
                }
            }
        }
        return line.replace("java.lang.",""); //if we have direct util class name appended in params
    }

    /**
     * Get Path/Resource URL from String
     * @param line
     * @return
     */
    public String getResourceUrl(String line) {
        if(null != line){
            if(null != line.split(" /")){
                if(line.split(" /").length>1){
                    if(null != line.split(" /")[1].split(" ")){
                        if(line.split(" /")[1].split(" ").length > 1){
                            line=line.split(" /")[1].split(" ")[0];
                        }
                    }
                }
            }
        }
        return line;
    }

    /**
     * Get HTTP Request Type from String
     * @param line
     * @return
     */
    public String getHttpMethod(String line) {
        return (null != line.split(" ")[0]?line.split(" ")[0]:"");
    }


    public String getClassName(String str){
        String[] pr = str.split(Pattern.quote("."));
        str=pr[pr.length-2];
        if(!"Assets".equals(str)) {
            return str;
        }else{
            return "";
        }
    }

    /**
     * Get PathParameter and QueryParameter from Strings
     * @param str
     * @param str2
     * @return
     */
    public List<Map> getPathAndQueryParams(String str, String str2){
        List<Map> rm = new ArrayList<Map>();
        List<String> r= new ArrayList<String>();
        Map<String, String> hmp = new HashMap<>();
        Map<String, String> hmq = new HashMap<>();

        //check resource level parameters
        if(null != str){
            if(str.split("/").length>0){
                String[] sp = str.split("/");
                for(String s:sp){
                    if(s.startsWith(":"))r.add(s);
                }
            }

            //check method level parameters
            if(str2.split("\\([^)]*\\)").length>0){

                Pattern p = Pattern.compile("\\((.*?)\\)");
                Matcher m = p.matcher(str2);
                while(m.find()){
                    if(r.size()>0) {
                        for (String s : r) {
                            String[] sps = m.group(1).split("\\s*,\\s*");
                            for (String spa : sps) {
                                if (r.contains(":" + spa.split(":")[0].replace(":", ""))) {
                                    //add in path parameters
                                    hmp.put(spa.split(":")[0], spa.split(":")[1]);
//                                System.out.println("hmp: "+hmp.size());
                                } else {
                                    //add in query parameters
                                    hmq.put(spa.split(":")[0], spa.split(":")[1]);
//                                System.out.println("hmq: "+hmp.size());
                                }
                            }
                        }
                    }else{
                        String[] sps = m.group(1).split("\\s*,\\s*");
                        for (String spa : sps) {
                            //add in query parameters
                            String[] splits = spa.split(":");
                            if(splits.length>1)
                            hmq.put(splits[0], splits[1]);
//                          System.out.println("hmq: "+hmp.size());
                         }
                    }
                }

            }

        }
        rm.add(hmp);
        rm.add(hmq);
        return rm;
    }


    /**
     * Read Directory
     * @param path
     */
    public void walk( String path ) {

        File root = new File( path );
        File[] list = root.listFiles();

        if (list == null) return;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
                walk( f.getAbsolutePath() );
                System.out.println( "Dir:" + f.getAbsoluteFile() );
            }
            else {
                System.out.println( "File:" + f.getAbsoluteFile() );
            }
        }
    }


}
