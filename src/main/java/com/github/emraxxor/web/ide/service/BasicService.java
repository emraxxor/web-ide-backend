package com.github.emraxxor.web.ide.service;

import org.springframework.data.repository.CrudRepository;

/**
 * 
 * @author attila
 *
 * @param <T>
 * @param <R>
 */
public interface BasicService<T,R extends CrudRepository<T, ?>> {

	R repository();
	
}
