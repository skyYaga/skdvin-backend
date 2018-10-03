package in.skdv.skdvinbackend.mail.template;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmailTemplateTest {

    private static final String BASE_URL = "https://example.com";

    @Autowired
    private TemplateEngine emailTemplateEngine;

    @Test
    public void testUserRegistrationMail() {
        Context ctx = new Context();
        ctx.setVariable("username", "horst");
        ctx.setVariable("tokenurl", BASE_URL);
        String htmlMail = emailTemplateEngine.process("html/user-registration", ctx);
        assertEquals("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    \n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
                "</head>\n" +
                "<body>\n" +
                "<p>Hello horst!</p>\n" +
                "<p>Please click on the Link below to confirm your registration.</p>\n" +
                "<a href=\"https://example.com\">https://example.com</a>\n" +
                "<p>Regards</p>\n" +
                "</body>\n" +
                "</html>", htmlMail);
    }

}