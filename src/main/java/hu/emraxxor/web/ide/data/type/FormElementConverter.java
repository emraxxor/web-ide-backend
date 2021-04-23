package hu.emraxxor.web.ide.data.type;

/**
 * 
 * @author attila
 *
 * @param <T>
 */
public interface FormElementConverter<T> {

	T convert(String e);
	
	String convert(Object e);


}
