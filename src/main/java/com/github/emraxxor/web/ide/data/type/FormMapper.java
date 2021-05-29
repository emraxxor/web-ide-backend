package com.github.emraxxor.web.ide.data.type;

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
	
	Class<?> sourceType() default Null.class;
	
	String target() default "";

	Class<?> targetType() default Null.class;
	
	String expression() default "";
	
	Class<?> converter() default Null.class;
}
