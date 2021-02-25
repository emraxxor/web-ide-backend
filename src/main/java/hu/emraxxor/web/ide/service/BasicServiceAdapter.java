package hu.emraxxor.web.ide.service;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

/**
 * 
 * @author attila
 *
 * @param <T>
 * @param <R>
 */
@Transactional
public abstract class BasicServiceAdapter<T, ID, R extends CrudRepository<T, ID>> implements BasicService<T, R> {

	@Autowired
	protected R repository;
	

	public T save(T e) {
		return repository.save(e);
	}
	
	public Iterable<T> saveAll(Iterable<T> e) {
		return repository.saveAll((e));
	}
	
	public Optional<T> find(ID id) {
		return repository.findById(id);
	}
	
	public Iterable<T> findAll() {
		return repository.findAll();
	}
	
	public long count() {
		return repository.count();
	}
	
	public void delete(T entity) {
		repository.delete(entity);
	}
	
	public void deleteAll(Iterable<T> entities) {
		repository.deleteAll(entities);
	}
	
	@Override
	public R repository() {
		return repository;
	}
	
	
	
}
