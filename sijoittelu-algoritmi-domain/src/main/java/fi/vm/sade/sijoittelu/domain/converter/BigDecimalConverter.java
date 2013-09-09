package fi.vm.sade.sijoittelu.domain.converter;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.morphia.converters.SimpleValueConverter;
import com.google.code.morphia.converters.TypeConverter;
import com.google.code.morphia.mapping.MappedField;
import com.google.code.morphia.mapping.MappingException;

/**
 * 
 * @author Jussi Jartamo
 * 
 *         https://code.google.com/p/morphia/issues/detail?id=411
 *         https://code.google.com/p/morphia/issues/detail?id=412
 */
public class BigDecimalConverter extends TypeConverter implements SimpleValueConverter {

    private static Logger LOG = LoggerFactory.getLogger(BigDecimalConverter.class);

    public BigDecimalConverter() {
        super(BigDecimal.class);
    }

    @Override
    public Object encode(Object value, MappedField optionalExtraInfo) {
        if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }

    @Override
    public Object decode(Class targetClass, Object fromDBObject, MappedField optionalExtraInfo) throws MappingException {
        if (fromDBObject == null || fromDBObject.toString().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(fromDBObject.toString());
        } catch (NumberFormatException e) {
            LOG.error("Arvoa {} ei voitu muuttaa BigDecimal muotoon!", fromDBObject);
            return BigDecimal.ZERO;
        }
    }

}
