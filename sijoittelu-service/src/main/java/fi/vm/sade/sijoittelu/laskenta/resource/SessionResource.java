package fi.vm.sade.sijoittelu.laskenta.resource;

import javax.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/resources/session")
@Tag(name = "session", description = "Sessionhallinta")
public class SessionResource {

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/maxinactiveinterval", produces = org.springframework.http.MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Palauttaa session erääntymisen aikarajan sekunteina", description = "Tarvitsee HTTP kutsun, jossa on session id", responses = { @ApiResponse(responseCode = "OK", content = @Content(schema = @Schema(implementation = String.class)))})
    public String maxInactiveInterval(HttpServletRequest req) {
        return Integer.toString(req.getSession().getMaxInactiveInterval());
    }
}
