package fi.vm.sade.sijoittelu.tulos.roles;

/**
 * Created with IntelliJ IDEA.
 * User: jukais
 * Date: 17.6.2013
 * Time: 17.23
 * To change this template use File | Settings | File Templates.
 */
public class SijoitteluRole {

    public static final String READ_UPDATE_CRUD = "hasAnyRole('ROLE_APP_SIJOITTELU_READ','ROLE_APP_SIJOITTELU_READ_UPDATE','ROLE_APP_SIJOITTELU_CRUD')";
    public static final String UPDATE_CRUD = "hasAnyRole('ROLE_APP_SIJOITTELU_READ_UPDATE','ROLE_APP_SIJOITTELU_CRUD')";
    public static final String CRUD = "hasAnyRole('ROLE_APP_SIJOITTELU_CRUD')";

    public static final String CRUD_ROLE = "ROLE_APP_SIJOITTELU_CRUD";
    public static final String UPDATE_ROLE = "ROLE_APP_SIJOITTELU_READ_UPDATE";

}
