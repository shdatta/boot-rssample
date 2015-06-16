package rssample;


import org.apache.cxf.helpers.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import rssample.clientInterfaces.testResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;

@Component
@Path("/hello")
public class Hello {

    @Autowired
    @Qualifier("myClient")
    testResource myClient;

    @Autowired
    @Qualifier("otherClient")
    private testResource otherClient;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() throws IOException {
        return IOUtils.toString((InputStream)otherClient.getCharge().getEntity());
    }

    public Hello() {
        System.out.println("Test");
    }
}
