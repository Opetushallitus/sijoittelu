package fi.vm.sade.sijoittelu.laskenta.resource;

import fi.vm.sade.sijoittelu.App;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Hidden
@RestController
@RequestMapping(value = "/")
public class StaticResource {

    @GetMapping(value = {"/swagger", "/swagger/**"})
    public void swagger(HttpServletResponse response) throws IOException {
        response.sendRedirect(App.CONTEXT_PATH.concat("/swagger-ui/index.html"));
    }

}
