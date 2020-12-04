package org.leandro.inventory;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.stream.Stream;


@OpenAPIDefinition(
        info = @Info(
                title = "Inventory",
                version = "${api.version}",
                description = "${openapi.description}",
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0"),
                contact = @Contact(url = "https://www.linkedin.com/in/leandroca/", name = "Leandro", email = "leandro.ueg@gmail.com")
        ),
        tags = {
                @Tag(name = "Conference"),
                @Tag(name = "Route")
        }
)
@Singleton
public class Application {

    public static void main(String[] args) {

        System.out.println(stringInverter("ABC"));

    }

    public static String stringInverter(String test) {

        //
        char[] chars = test.toCharArray();
        String result = new String();
        for( int i = chars.length; i > 0; i--){
            result = result.concat(String.valueOf(chars[i-1]));
        }
        return result;
    }
}
