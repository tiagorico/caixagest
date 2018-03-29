package com.github.rico.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Generic DAO implementation.
 *
 * @param <T>  The entity class
 * @param <PK> The entity primary key
 *             <p>
 *             Created by Luis Tiago Rico
 */
@SuppressWarnings("unchecked")
public abstract class GenericDAOBean<T, PK extends Serializable> {
    private final Logger LOG = LoggerFactory.getLogger(GenericDAOBean.class);
    @PersistenceContext(unitName = "caixagestPu")
    protected EntityManager manager;
    private Class<T> persistentClass;
    private Class<PK> primaryKeyClass;
    private SingularAttribute<T, PK> primaryKey;

    @PostConstruct
    public void init() {
        this.persistentClass =
                (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.primaryKeyClass =
                (Class<PK>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        this.primaryKey = manager.getMetamodel().entity(persistentClass).getDeclaredId(primaryKeyClass);
    }

    public Optional<T> findById(PK id) {
        return Optional.ofNullable(manager.find(persistentClass, id));
    }

    public Optional<T> findById(PK id, final Consumer<Root<T>> fetch) {
        final CriteriaBuilder builder = manager.getCriteriaBuilder();
        final CriteriaQuery<T> query = builder.createQuery(persistentClass);

        final Root<T> root = query.from(persistentClass);
        fetch.accept(root);

        query.select(root).where(root.get(primaryKey).in(id));

        Optional<T> result = empty();
        try {
            result = of(manager.createQuery(query).getSingleResult());
        } catch (NoResultException e) {
            LOG.debug("No result found", e);
        }

        return result;
    }

    public List<T> findAll() {
        return manager.createQuery("FROM " + persistentClass.getName()).getResultList();
    }

    public T insert(T o) {
        manager.persist(o);
        return o;
    }

    public void delete(T o) {
        manager.remove(o);
    }

}
