package org.leandro.catalogue;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.inject.Singleton;


@OpenAPIDefinition(
        info = @Info(
                title = "Catalogue",
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
        Micronaut.run(Application.class);
    }

}
