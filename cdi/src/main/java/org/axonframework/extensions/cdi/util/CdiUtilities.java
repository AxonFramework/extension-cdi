package org.axonframework.extensions.cdi.util;

import javax.enterprise.inject.spi.*;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

public class CdiUtilities {

    /**
     * Returns an object reference of a given bean.
     *
     * @param beanManager bean manager.
     * @param bean bean.
     * @param beanType bean type.
     * @return Object reference.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getReference(final BeanManager beanManager,
            final Bean<T> bean,
            final Type beanType) {
        return (T) beanManager.getReference(bean, beanType,
                beanManager.createCreationalContext(bean));
    }

//    public static <T> T getReference(final BeanManager beanManager, final Class<T> clazz) {
//        final Set<Bean<?>> beans = beanManager.getBeans(clazz);
//        final Bean<?> bean = beanManager.resolve(beans);
//
//        final CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
//        return (T) beanManager.getReference(bean, clazz, creationalContext);
//    }

    /**
     * Checks whether a given bean has methods annotated with given annotation.
     *
     * @param at AnnotatedType to check.
     * @param annotationClazz annotation class.
     * @return true if at least one annotated method is present.
     */
    public static final boolean hasAnnotatedMethod(final AnnotatedType<?> at,
            final Class<? extends Annotation> annotationClazz) {
        return at.getMethods().stream().anyMatch(m -> m.isAnnotationPresent(annotationClazz));
    }

    /**
     * Checks whether a bean has a member annotated with all provided
     * annotations.
     *
     * @param bean bean to check.
     * @param classes annotation classes to check for.
     * @return true if a member of a bean is annotated with all specified
     * annotations.
     */
    @SafeVarargs
    public static boolean hasAnnotatedMember(final Bean<?> bean,
            final Class<? extends Annotation>... classes) {
        final Predicate<Field> hasAllAnnotations
                = field -> stream(classes).allMatch(field::isAnnotationPresent);
        return stream(bean.getBeanClass().getDeclaredFields()).anyMatch(
                hasAllAnnotations);
    }

    /**
     * Retrieve the bean manager.
     *
     * @return bean manager, if any, or <code>null</code>.
     */
    public static BeanManager getBeanManager() {
        return CDI.current().getBeanManager();
    }

    /*
     * Resolve the beans and return the (typecasted) instance as an {@link Optional}.
     */
    @SuppressWarnings("unchecked")
    private static <T> Optional<T> resolve(final BeanManager mgr, final Set<Bean<?>> beans, final Class<T> clazz) {
        if ((beans == null) || beans.isEmpty()) {
            return Optional.empty();
        }
        Bean<?> bean = mgr.resolve(beans);
        return Optional.ofNullable((T)mgr.getReference(bean, clazz, mgr.createCreationalContext(bean)));
    }

    /**
     *
     * @param mgr
     * @param bean
     * @param clazz
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getBean(BeanManager mgr, Bean<?> bean, Class<T> clazz) {
        return Optional.ofNullable((T)mgr.getReference(bean, clazz, mgr.createCreationalContext(bean)));
    }

    /**
     *
     * @param mgr
     * @param clazz
     * @return
     */
    public static <T> Optional<Bean<?>> resolveBean(final BeanManager mgr, final Class<T> clazz) {
        Set<Bean<?>> beans = mgr.getBeans(clazz);
        if ((beans == null) || beans.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mgr.resolve(beans));
    }

    /**
     * Return a bean, given its class, as an {@link Optional}, empty if no bean instance found.
     *
     * @param clazz the required class of the bean.
     * @param <T> the class of the bean, to fix the template parameter.
     * @return an {@link Optional} of the bean.
     */
    public static <T> Optional<T> getInstance(final BeanManager mgr, final Class<T> clazz) {
        return resolve(mgr, mgr.getBeans(clazz), clazz);
    }

    /**
     * Return a bean, given its class, as an {@link Optional}, empty if no bean instance found.
     *
     * @param clazz the required class of the bean.
     * @param <T> the class of the bean, to fix the template parameter.
     * @return an {@link Optional} of the bean.
     */
    public static <T> Optional<T> getInstance(final Class<T> clazz) {
        return getInstance(getBeanManager(), clazz);
    }

    /**
     * Return a named bean as an {@link Optional}, empty if no bean instance found.
     *
     * @param mgr the {@link} BeanManager to query.
     * @param name the name of the bean.
     * @param clazz the required class of the bean.
     * @param <T> the class of the bean, to fix the template parameter.
     * @return an {@link Optional} of the bean.
     */
    public static <T> Optional<T> getInstance(final BeanManager mgr, final String name, final Class<T> clazz) {
        return resolve(mgr, mgr.getBeans(name), clazz);
    }

    /**
     * Return a named bean as an {@link Optional}, empty if no bean instance found.
     *
     * @param name the name of the bean.
     * @param clazz the required class of the bean.
     * @param <T> the class of the bean, to fix the template parameter.
     * @return an {@link Optional} of the bean.
     */
    public static <T> Optional<T> getInstance(final String name, final Class<T> clazz) {
        return getInstance(getBeanManager(), name, clazz);
    }

    public static String extractBeanName(AnnotatedMember<?> annotatedMember) {
        Named named = annotatedMember.getAnnotation(Named.class);

        if (named != null && !"".equals(named.value())) {
            return named.value();
        }

        // TODO: Should not try to derive the name of a member that does not
        // have the @Named annotation on it.
        return annotatedMember.getJavaMember().getName();
    }

    /**
     * Returns a transitive stream of all methods of a class, for the purpose of
     * scanning for methods with a given annotation. As we know Object will not
     * have Axon annotations, either that or null is a reason to stop traveling
     * upwards in the hierarchy.
     *
     * Added this because Class<>.getMethods() only returns a transitive list of
     * public methods.
     *
     * @param clazz The starting point in the hierarchy.
     * @return An empty stream for null or java.lang.Object, otherwise a stream
     * of all methods (public/protected/package private/private) followed by
     * those of its super, etc.
     */
    // TODO: See if this is really necessary, manifestation of a bug elsewhere
    // or is a quirk of CDI. Does have a performance cost.
    private static Stream<Method> getDeclaredMethodsTransitive(Class<?> clazz) {
        return ((clazz == null) || clazz.equals(Object.class))
                ? Stream.empty()
                : Stream.concat(stream(clazz.getDeclaredMethods()),
                        getDeclaredMethodsTransitive(clazz.getSuperclass()));
    }
}
