package aston.h2.mapper;

public interface Mapper<T, V> {

    V map(T t);

    T reverseMap(V v);


}
