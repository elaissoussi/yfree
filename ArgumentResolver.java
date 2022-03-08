public class MyController{

  public void myGet(  @MyFooBarParam(foo="foo", bar="bar) MyFooBar myFooBar){
  
  }
}

// annotation
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.PARAMETER})
public @interface MyFooBarParam {
   String bar();
   String bar();
}

// class param
public class MyFooBar{
  String foo, bar;
  // Getters / Setters
}

//

public class MyFooBarParamArgumentResolver implements HandlerMethodArgumentResolver {


 public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(MyFooBar.class);
    }

public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

HttpServletRequest request = (HttpServletRequest)webRequest.getNativeRequest(HttpServletRequest.class);
 MyFooBarParam annotation = (MyFooBarParam)parameter.getParameterAnnotation(MyFooBarParam.class);
 
 if(annotation !=null ){
      ....
  }
 }
       
}
