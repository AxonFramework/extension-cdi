package org.axonframework.extensions.cdi.test.paramresolver;

import jakarta.enterprise.context.ApplicationScoped;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.conflictresolution.ConflictResolver;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MetaData;
import org.axonframework.messaging.annotation.MessageIdentifier;
import org.axonframework.messaging.unitofwork.UnitOfWork;

public class ParameterResolverTestComponents {

    public static final String KEY = "key";

    public static class CommandMeta {}
    public static class CommandMessage {}
    public static class CommandUoW {}
    public static class CommandId {}
    public static class CommandConflictResolver {}

    @ApplicationScoped
    public static class Handler {

        @CommandHandler
        public String handleWithMeta(CommandMeta cmd, MetaData metaData) {
            return "handleWithMeta:" + metaData.get(KEY).toString();
        }

        @CommandHandler
        public String handleWithMessage(CommandMessage cmd, Message message) {
            return "handleWithMessage:" + message.getMetaData().get(KEY).toString();
        }

        @CommandHandler
        public String handleWithUoW(CommandUoW cmd, UnitOfWork unitOfWork) {
            return "handleWithUoW:" + unitOfWork.getMessage().getMetaData().get(KEY).toString();
        }

        @CommandHandler
        public String handleWithMessage(CommandId cmd, @MessageIdentifier String id) {
            return "handleWithId:" + id;
        }

        @CommandHandler
        public String handleWithConflictResolver(CommandConflictResolver cmd, ConflictResolver conflictResolver) {
            return "handleWithConflictResolver:" + conflictResolver.getClass();
        }

    }
}
