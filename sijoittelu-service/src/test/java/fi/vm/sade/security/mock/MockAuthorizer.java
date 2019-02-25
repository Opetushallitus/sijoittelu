package fi.vm.sade.security.mock;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import fi.vm.sade.authentication.business.service.Authorizer;
import fi.vm.sade.authorization.NotAuthorizedException;

@Component
@Profile("it")
public class MockAuthorizer implements Authorizer{
    @Override
    public void checkUserIsNotSame(final String s) throws NotAuthorizedException {

    }

    @Override
    public void checkOrganisationAccess(final String s, final String... strings) throws NotAuthorizedException {

    }
}
