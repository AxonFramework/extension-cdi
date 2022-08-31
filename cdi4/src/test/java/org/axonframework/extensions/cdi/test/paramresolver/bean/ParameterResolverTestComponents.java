package org.axonframework.extensions.cdi.test.paramresolver.bean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import org.axonframework.commandhandling.CommandHandler;

public class ParameterResolverTestComponents {

    public static final String KEY = "key";

    public static class CommandBean {}
    public static class CommandBean2 {}

    @Dependent
    public static class Configuration {

        public static class MyBean {
            private String name;
            public MyBean(String name) { this.name = name; }
            public String getName() { return name; }
        }

        @ApplicationScoped
        @Produces
        @Named
        public MyBean myBean () {
            return new MyBean("myBean");
        }

        @Produces
        @ApplicationScoped
        @Named
        public MyBean myOtherBean () {
            return new MyBean("myOtherBean");
        }

    }

    @ApplicationScoped
    public static class Handler {

        @CommandHandler
        public String handleWithBean(CommandBean cmd, @Named("myBean") Configuration.MyBean myBean) {
            return "handleWithBean:" + myBean.getName();
        }

        @CommandHandler
        public String handleWithNamedBean(CommandBean2 cmd, @Named("myOtherBean") Configuration.MyBean myBean) {
            return "handleWithBean:" + myBean.getName();
        }

    }
}
