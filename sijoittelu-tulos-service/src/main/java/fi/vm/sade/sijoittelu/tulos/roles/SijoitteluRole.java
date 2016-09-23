package fi.vm.sade.sijoittelu.tulos.roles;

public class SijoitteluRole {
    public static final String READ_UPDATE_CRUD = "hasAnyRole('ROLE_APP_SIJOITTELU_READ','ROLE_APP_SIJOITTELU_READ_UPDATE','ROLE_APP_SIJOITTELU_CRUD')";
    public static final String UPDATE_CRUD = "hasAnyRole('ROLE_APP_SIJOITTELU_READ_UPDATE','ROLE_APP_SIJOITTELU_CRUD')";
    public static final String CRUD = "hasAnyRole('ROLE_APP_SIJOITTELU_CRUD')";

    public static final String CRUD_ROLE = "ROLE_APP_SIJOITTELU_CRUD";
    public static final String READ_ROLE = "ROLE_APP_SIJOITTELU_READ";
    public static final String UPDATE_ROLE = "ROLE_APP_SIJOITTELU_READ_UPDATE";

    public static final String PERUUNTUNEIDEN_HYVAKSYNTA = "ROLE_APP_SIJOITTELU_PERUUNTUNEIDEN_HYVAKSYNTA";
}
