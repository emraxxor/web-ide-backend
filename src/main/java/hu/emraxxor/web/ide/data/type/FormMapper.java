package hu.emraxxor.web.ide.data.type;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author Attila Barna
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface FormMapper {

	String source() default "";
	
	String sourceType() default "";
	
	String target() default "";

	String targetType() default "";
	
	String expression() default "";
}
