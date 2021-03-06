package rssample;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.spring.SpringResourceFactory;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import rssample.Interceptors.LogClientOutRestInterceptor;
import rssample.Interceptors.LogInRestInterceptor;
import rssample.clientInterfaces.testResource;

import javax.ws.rs.Path;
import java.util.LinkedList;
import java.util.List;

@SpringBootApplication
@ImportResource({"classpath:META-INF/cxf/cxf.xml", "classpath:META-INF/cxf/cxf-servlet.xml"})
public class RssampleApplication {

    private static final Log logger = LogFactory.getLog(RssampleApplication.class);

    @Autowired
    private ApplicationContext ctx;

    @Value("${cxf.path:/services/*}")
    private String cxfPath;

    @Value("${cxf.log.requests:false}")
    private boolean logRequests;

    @Autowired
    private LogInRestInterceptor logInRestInterceptor;

    @Autowired
    private LogClientOutRestInterceptor logClientOutRestInterceptor;

    public static void main(String[] args) {
        SpringApplication.run(RssampleApplication.class, args);
    }

    @Bean
    public ServletRegistrationBean cxfServletRegistrationBean() {
        return new ServletRegistrationBean(new CXFServlet(), "/services/*");
    }

    @Bean
    public Server jaxRsServer() {
        List<ResourceProvider> resourceProviders = new LinkedList<ResourceProvider>();
        for (String beanName : ctx.getBeanDefinitionNames()) {
            if (ctx.findAnnotationOnBean(beanName, Path.class) != null) {
                if (beanName.equals("myClient") || beanName.equals("otherClient"))
                    continue;
                SpringResourceFactory factory = new SpringResourceFactory(beanName);
                factory.setApplicationContext(ctx);
                resourceProviders.add(factory);
            }
        }
        if (resourceProviders.size() > 0) {
            JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
            factory.setBus(ctx.getBean(SpringBus.class));
//            factory.setProviders(Arrays.asList(new JacksonJsonProvider()));
            factory.setResourceProviders(resourceProviders);
            factory.getInInterceptors().add(logInRestInterceptor);
            factory.setAddress("/");
            return factory.create();

        } else {
            return null;
        }
    }

    @Bean(name="myClient")
    public testResource myClient(){
        testResource tr =  JAXRSClientFactory.create("https://www.google.com", testResource.class);
        WebClient.client(tr).getHeaders().putSingle("Hello", "hello");
        return tr;
    }

    @Bean(name="otherClient")
    public testResource otherClient(){
        JAXRSClientFactoryBean factoryBean = new JAXRSClientFactoryBean();
        factoryBean.setServiceClass(testResource.class);
        factoryBean.setAddress("https://www.google.com");
        factoryBean.setThreadSafe(true);
        factoryBean.getOutInterceptors().add(logClientOutRestInterceptor);
        return factoryBean.create(testResource.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public JacksonJsonProvider jsonProvider(ObjectMapper objectMapper) {
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(objectMapper);
        return provider;
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public EmbeddedServletContainerFactory embeddedServletContainerFactory() {
        // Made to match the context path when deploying to standalone tomcat- can easily be kept in sync w/ properties
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory("", 8080);
        return factory;
    }
}
