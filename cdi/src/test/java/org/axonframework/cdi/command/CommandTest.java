package org.axonframework.cdi.command;

import org.axonframework.cdi.stereotype.Aggregate;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.*;

import javax.faces.bean.ApplicationScoped;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

public class CommandTest {

    @Rule
    public WeldInitiator weld = WeldInitiator
            .from(CommandTest.Account.class)
            .inject(this)
            .build();

    private AggregateTestFixture<Account> account;

    @Before
    public void setup() {
        account = new AggregateTestFixture<>(Account.class);
    }

    @Test
    public void testAccountCreate() {
        account.givenNoPriorActivity()
               .when(new CreateAccountCommand("4711", 100.00))
               .expectEvents(new AccountCreatedEvent("4711", 100.00));
    }

    public static class CreateAccountCommand {

        @TargetAggregateIdentifier
        private final String accountId;
        private final Double overdraftLimit;

        public CreateAccountCommand(String accountId, Double overdraftLimit) {
            this.accountId = accountId;
            this.overdraftLimit = overdraftLimit;
        }

        public String getAccountId() {
            return accountId;
        }

        public Double getOverdraftLimit() {
            return overdraftLimit;
        }

        @Override
        public String toString() {
            return "Create Account Command with accountId=" + accountId
                    + ", overdraftLimit=" + overdraftLimit;
        }
    }

    public static class AccountCreatedEvent {

        private final String accountId;
        private final Double overdraftLimit;

        public AccountCreatedEvent(String accountId, Double overdraftLimit) {
            this.accountId = accountId;
            this.overdraftLimit = overdraftLimit;
        }

        public String getAccountId() {
            return accountId;
        }

        public Double getOverdraftLimit() {
            return overdraftLimit;
        }

        @Override
        public String toString() {
            return "Account Created Event with accountId=" + accountId
                    + ", overdraftLimit=" + overdraftLimit;
        }
    }

    @ApplicationScoped
    @Aggregate
    public static class Account {

        @AggregateIdentifier
        private String accountId;

        @SuppressWarnings("unused")
        private Double overdraftLimit;

        public Account() {
            // Empty constructor needed for CDI proxying.
        }

        @CommandHandler
        public Account(final CreateAccountCommand command) {
            apply(new AccountCreatedEvent(command.getAccountId(),
                                          command.getOverdraftLimit()));
        }

        @EventSourcingHandler
        public void on(AccountCreatedEvent event) {
            this.accountId = event.getAccountId();
            this.overdraftLimit = event.getOverdraftLimit();
        }
    }
}
