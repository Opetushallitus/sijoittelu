package fi.vm.sade.sijoittelu.laskenta.resource;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.prepost.PreAuthorize;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/resources/session")
@Api(value = "session", description = "Sessionhallinta")
public class SessionResource {

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/maxinactiveinterval", produces = org.springframework.http.MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(
            value = "Palauttaa session erääntymisen aikarajan sekunteina",
            notes = "Tarvitsee HTTP kutsun, jossa on session id",
            response = String.class)
    public String maxInactiveInterval(HttpServletRequest req) {
        return Integer.toString(req.getSession().getMaxInactiveInterval());
    }
}
