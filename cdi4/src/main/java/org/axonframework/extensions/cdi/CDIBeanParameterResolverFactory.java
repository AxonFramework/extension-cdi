package org.axonframework.extensions.cdi;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Qualifier;
import org.axonframework.common.Priority;
import org.axonframework.extensions.cdi.config.CDIConfigurationException;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.annotation.ParameterResolver;
import org.axonframework.messaging.annotation.ParameterResolverFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Priority(Priority.LOW)
public class CDIBeanParameterResolverFactory implements ParameterResolverFactory {

    @Override
    public ParameterResolver createInstance(Executable executable, Parameter[] parameters, int parameterIndex) {

        Parameter parameter = parameters[parameterIndex];
        Class<?> parameterType = parameter.getType();

        List<Annotation> annotationList = Arrays.asList(parameter.getAnnotations());
        annotationList = annotationList.stream()
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(Qualifier.class))
                .collect(Collectors.toList());

        Annotation[] annotations = annotationList.toArray(new Annotation[annotationList.size()]);

        Instance instance = CDI.current().select(parameterType, annotations);

        if (instance.isUnsatisfied()) {
            return null;
        }

        System.out.println("\n ------");
        System.out.println("Executable: " + executable.getName());
        System.out.println("parameters: " + parameters);
        System.out.println("parameterIndex: " + parameterIndex);
        System.out.println("parameter: " + parameter);
        System.out.println("parameterType: " + parameterType);
        System.out.println("INSTANCE: "  + instance);
        System.out.println("Resolvable: "  + instance.isResolvable());
        System.out.println("Ambiguous: "  + instance.isAmbiguous());
        System.out.println("Unsatisfied: "  + instance.isUnsatisfied());


        if (instance.isResolvable()) {
            return new CDIBeanParameterResolver(instance);
        }

        throw CDIConfigurationException.ambiguousInstance(parameterType, instance);

    }


    public static class CDIBeanParameterResolver<T> implements ParameterResolver<T> {

        Instance<T> instance;

        public CDIBeanParameterResolver (Instance<T> instance) {
            this.instance = instance;
        }

        @Override
        public T resolveParameterValue(Message message) {
            return instance.get();
        }

        @Override
        public boolean matches(Message message) {
            return true;
        }

    }

}
