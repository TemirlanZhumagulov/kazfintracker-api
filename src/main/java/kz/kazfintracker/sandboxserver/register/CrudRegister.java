package kz.kazfintracker.sandboxserver.register;

public interface CrudRegister<T, K> {

  T load(K key);

  void create(T entity);

  void update(T entity);

  void delete(K key);

}
