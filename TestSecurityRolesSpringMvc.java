
public class ControllerSecurityITest extends AbstractControllerITest {

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;


    private final static Set<String> NO_AUTH_NEEDED = Set.of();
    private final Map<String, Set<String>> rolesPerEndpoints = Map.ofEntries(
            // Technical APIs
            entry("{ [/error]}", NO_AUTH_NEEDED),
         
            // Private APIs
            entry("{GET [/test-request/private/requests]}", Set.of(OperatorRoleConstant.ORDER_READ, ShopRoleConstant.SALES, ShopRoleConstant.SUPPORT)),
         
            // Public APIs
            entry("{GET [/api/test-request/requests], produces [application/json]}", Set.of(OperatorRoleConstant.API_ACCESS, ShopRoleConstant.ADMIN, PartnerRoleConstant.ACCOUNTING_DOCUMENT_REQUEST_READ)),
           
            // internal APIs
            entry("{GET [/api/test-request/internal/requests]}", NO_AUTH_NEEDED),
    );

    @Test
    public void testMvcSecurity() {
        Map<String, Set<String>> rolesPerEndpointsCopy = new HashMap<>(rolesPerEndpoints);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
        handlerMethods.forEach((info, method) -> {
            String mvcHandler = info.toString();
            assertThat(rolesPerEndpointsCopy)
                    .describedAs("'%s' is not specified in the security tests. Add it to " +
                            "ControllerSecurityITest.rolesPerEndpoints " +
                            "with it's expected roles.", mvcHandler)
                    .containsKey(mvcHandler);
            Set<String> roles = rolesPerEndpointsCopy.get(mvcHandler);
            Secured methodAnnotation = method.getMethodAnnotation(Secured.class);
            var methodAnnotationRoles = methodAnnotation == null ? "[nul]" : Arrays.toString(methodAnnotation.value());
            if (roles.isEmpty()) {
                assertThat(methodAnnotation)
                        .describedAs("%s should be open, but is @Secured with %s", mvcHandler,
                                methodAnnotationRoles)
                        .isNull();
            } else {
                assertThat(methodAnnotation)
                        .describedAs("%s should be @Secured. Annotation not found.", mvcHandler)
                        .isNotNull();

                assertThat(methodAnnotation.value())
                        .describedAs("Roles does not match for %s", mvcHandler)
                        .containsExactlyInAnyOrderElementsOf(roles);
            }
            rolesPerEndpointsCopy.remove(mvcHandler);
        });

        if (!rolesPerEndpointsCopy.isEmpty()) {
            fail("Those handlers were specified : %s but not found in the applicationContext.",
                    rolesPerEndpointsCopy.keySet());
        }
    }
}
