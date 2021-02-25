package hu.emraxxor.web.ide.data.type;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Timestamp;


/**
 * 
 * @author Attila Barna
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface TimestampToString {

	DefaultDateFormatter.DATE_FORMAT type() default DefaultDateFormatter.DATE_FORMAT.STRICT_DATE_FORMAT;
	
	Class<?> dateType() default Timestamp.class;
}
