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
public @interface EntityProperty {
	
	/**
	 * Name of the property that is going to be converted
	 */
	String property();
	
	
}
