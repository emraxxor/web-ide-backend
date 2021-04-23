package hu.emraxxor.web.ide.data.type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;

import org.springframework.util.StringUtils;

import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * @author Attila Barna
 *
 * @param <T>
 */
@Log4j2
public class FormElement<T> implements FormData {

	@IgnoreField
	protected transient T data;
	
	public FormElement() { }

	@Synchronized
	private static <F,T> void mapper(F from,T to, Field[] fields) {
		FormMapper fm;
		
		try {
			for(Field f : fields ) {
				if ( f.getAnnotation(IgnoreField.class)  != null ) continue;
				
				if ( (fm=f.getAnnotation(FormMapper.class)) != null ) {
					Method targetMethod;
					Method fromMethod;
					Class<?> fromType = fm.sourceType().equals(Null.class) ? f.getType() : fm.sourceType() ;
					Class<?> targetType = fm.targetType().equals(Null.class) ? f.getType() : fm.targetType() ;
					
					String sourceName = fm.source().equals("") ? f.getName() : fm.source();
					String targetName = fm.target().equals("") ? f.getName() : fm.target();
					
					fromMethod = from.getClass().getMethod( "get"  + StringUtils.capitalize(sourceName) );
					
					try {
						targetMethod = to.getClass().getMethod( "set"  + StringUtils.capitalize(targetName) , targetType );
					} catch(Exception e) {
						targetMethod = to.getClass().getMethod( "set"  + StringUtils.capitalize(targetName) , String.class );
					}
					
					Object value = fromMethod.invoke( from );
					
					if ( !fm.converter().equals(Null.class) ) {
						var converter = fm.converter().getDeclaredConstructor().newInstance();
						if ( converter instanceof FormElementConverter<?> ) {
							if ( value instanceof String ) {
								value =  targetType.cast( ((FormElementConverter<?>) converter).convert(value.toString()) );
							} else {
								value = ((FormElementConverter<?>) converter).convert( value );
							}
						}
					}

					if (!fm.expression().equals("")) {
						value = Class.forName(fm.expression()).getDeclaredConstructor().newInstance();
					}
					targetMethod.invoke( to ,  value );

				}
			}
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
		}
	}


	@Synchronized
	private static <F,T> void mapper(F from,T to) {
		mapper(from, to, to.getClass().getDeclaredFields());
	}
	
	
	public FormElement(T data) {
		this.data = data;
		Class<?> o = data.getClass(); 
		Field[] fields = this.getClass().getDeclaredFields();
		try {
			for(Field f : fields ) {
				// skip field
				if ( f.getAnnotation(IgnoreField.class)  != null ) continue;
				
				// convert timestamp to string
				if ( f.getAnnotation(TimestampToString.class) != null ) {
					Method s = this.getClass().getMethod("set" + StringUtils.capitalize(f.getName()) , f.getType());
					
					s.invoke(this, 
							DefaultDateFormatter.format(  
								(Timestamp) o.getMethod("get" + StringUtils.capitalize(f.getName())).invoke(data) ,
								f.getAnnotation(TimestampToString.class).type()
							)
					);
					
					continue;
				}
				
				// convert by property name
				if ( f.getAnnotation(EntityProperty.class) != null ) {
					Object rel = o.getMethod("get"+ StringUtils.capitalize(f.getName()) ).invoke(this.data);
					Method s = this.getClass().getMethod("set" + StringUtils.capitalize(f.getName()) , f.getType());
					Method property = rel.getClass().getMethod( "get" + StringUtils.capitalize( f.getAnnotation(EntityProperty.class).property()  ) );
					Object value = property.invoke(rel);
					s.invoke(this, value);
					continue;
				}
				
				// try to convert by name 
				Method s = this.getClass().getMethod("set" + StringUtils.capitalize(f.getName()) , f.getType());
				s.invoke(this,  o.getMethod("get" + StringUtils.capitalize(f.getName())).invoke(data) );
				
				// use form mapper
				mapper(data, this);
				
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			log.error(e);
		}	
	}
	
	public FormElement(T data, Field[] fields) {
		this.data = data;
		Class<?> o = data.getClass(); 
		try {
			for(Field f : fields ) {
				
				// skip field
				if ( f.getAnnotation(IgnoreField.class)  != null ) continue;
				
				// convert timestamp to string
				if ( f.getAnnotation(TimestampToString.class) != null ) {
					Method s = this.getClass().getMethod("set" + StringUtils.capitalize(f.getName()) , f.getType());
					
					s.invoke(this, 
							DefaultDateFormatter.format(  
								(Timestamp) o.getMethod("get" + StringUtils.capitalize(f.getName())).invoke(data) ,
								f.getAnnotation(TimestampToString.class).type()
							)
					);
					
					continue;
				}
				
				// convert by property name
				if ( f.getAnnotation(EntityProperty.class) != null ) {
					Object rel = o.getMethod("get"+ StringUtils.capitalize(f.getName()) ).invoke(this.data);
					Method s = this.getClass().getMethod("set" + StringUtils.capitalize(f.getName()) , f.getType());
					Method property = rel.getClass().getMethod( "get" + StringUtils.capitalize( f.getAnnotation(EntityProperty.class).property()  )  );
					Object value = property.invoke(rel);
					s.invoke(this, value);
					continue;
				}
				
				// try to convert by name 
				Method s = this.getClass().getMethod("set" + StringUtils.capitalize(f.getName()) , f.getType());
				s.invoke(this,  o.getMethod("get" + StringUtils.capitalize(f.getName())).invoke(data) );
				
				// use form mapper
				mapper(data, this);

			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			log.error(e);
		}	

	}

	
	public T toDataElement(Class<T> clazz,Field[] fields) {
		Constructor<?> cons;
		T object = null;
		try {
			cons = clazz.getConstructor();
			object = (T) cons.newInstance();
			
			for(Field f : fields ) {
				// skip field
				if ( f.getAnnotation(IgnoreField.class)  != null ) continue;
				
				// convert timestamp to string
				if ( f.getAnnotation(TimestampToString.class) != null ) {
					Method s = clazz.getMethod("set" + StringUtils.capitalize(f.getName()) , f.getAnnotation(TimestampToString.class).dateType());
					String u = (String) this.getClass().getMethod("get" + StringUtils.capitalize(f.getName())).invoke(this);
					
					if ( u != null ) 
						s.invoke(object, DefaultDateFormatter.timestamp(  u , f.getAnnotation(TimestampToString.class).type() ) );
					
					continue;
				}

				// convert by property name
				if ( f.getAnnotation(EntityProperty.class) != null ) 
					continue;
				
				// try to convert by name 
				Method s = clazz.getMethod("set" + StringUtils.capitalize(f.getName()) , f.getType());
				s.invoke(object,  this.getClass().getMethod("get" + StringUtils.capitalize(f.getName())).invoke(this) );
				
				// try to convert with using formmapper
				mapper(this, object, fields);
			}
			
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
			log.error(e);
		}	
		return object;
	}

	
	public T toDataElement(Class<T> clazz) {
		return this.toDataElement(clazz,  this.getClass().getDeclaredFields());
	}
	
	public T original() {
		return this.data;
	}
	
	public static <Y,Z>  Z convertTo(Y from, Class<Z> to) {
		Z object = null;
		Field[] fields = to.getDeclaredFields();

		try {
			object = to.getConstructor().newInstance();
			
			for(Field f : fields ) {
				// skip field
				if ( f.getAnnotation(IgnoreField.class)  != null ) continue;
				
				// convert timestamp to string
				if ( f.getAnnotation(TimestampToString.class) != null ) {
					Method s = to.getMethod("set" + StringUtils.capitalize(f.getName()) , f.getType());
					
					s.invoke(object, 
							DefaultDateFormatter.format(  
								(Timestamp) from.getClass().getMethod("get" + StringUtils.capitalize(f.getName())).invoke(from) ,
								f.getAnnotation(TimestampToString.class).type()
							)
					);
					
					continue;
				}
				
				if ( f.getAnnotation(EntityProperty.class) != null ) 
					continue;

				// try to convert by name 	
				Method s = to.getMethod("set" + StringUtils.capitalize(f.getName()) , f.getType());
				s.invoke(object, from.getClass().getMethod("get" + StringUtils.capitalize(f.getName())).invoke(from) );
				
				// try to convert with using formmapper
				mapper(from, object, fields);

			}
			
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
			log.error(e);
		}	
		return object;

	}
	
	public static <Y,Z>  void update(Y from, Z to) {
		Field[] fields = to.getClass().getDeclaredFields();
		try {
			for(Field f : fields ) {
				if (updateField(from, to, f)) continue;

				// try to convert with using formmapper
				mapper(from, to, fields);

			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			log.error(e);
		}	
	}

	private static <Y, Z> boolean updateField(Y from, Z to, Field f) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if ( f.getAnnotation(IgnoreField.class)  != null ) return true;

		if ( f.getAnnotation(TimestampToString.class) != null ) {
			Method s = to.getClass().getMethod("set" + StringUtils.capitalize(f.getName()) , f.getType());

			s.invoke(to,
					DefaultDateFormatter.format(
						(Timestamp) from.getClass().getMethod("get" + StringUtils.capitalize(f.getName())).invoke(from) ,
						f.getAnnotation(TimestampToString.class).type()
					)
			);

			return true;
		}

		if ( f.getAnnotation(EntityProperty.class) != null )
			return true;

		Method s = to.getClass().getMethod("set" + StringUtils.capitalize(f.getName()) , f.getType());
		s.invoke(to, from.getClass().getMethod("get" + StringUtils.capitalize(f.getName())).invoke(from) );
		return false;
	}

	public static <Y,Z>  void updateFrom(Y from, Z to) {
		Field[] fields = from.getClass().getDeclaredFields();
		try {
			for(Field f : fields ) {
				updateField(from, to, f);
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			log.error(e);
		}
	}
}

