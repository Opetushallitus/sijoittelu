package fi.vm.sade.sijoittelu.resource;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.hibernate.collection.internal.PersistentSet;
import org.springframework.stereotype.Component;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * User: wuoti
 * Date: 26.4.2013
 * Time: 15.57
 */
@Component
@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

    private final ObjectMapper objectMapper;

    public ObjectMapperProvider() {
        objectMapper = new ObjectMapper();

        objectMapper.configure(SerializationConfig.Feature.DEFAULT_VIEW_INCLUSION, false);
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // FIXME: Tämä tulostaa jsonin nätimmässä muodossa. Varmaan pois tuotannosta..
        objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }
}
