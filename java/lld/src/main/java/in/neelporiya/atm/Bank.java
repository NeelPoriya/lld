package in.neelporiya.atm;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory bank repository used by ATM tests and examples. */
public class Bank {

    private final Map<String, Account> accountsById = new ConcurrentHashMap<>();

    public Bank addAccount(Account account) {
        Objects.requireNonNull(account, "account");
        Account previous = accountsById.putIfAbsent(account.getId(), account);
        if (previous != null) {
            throw new IllegalArgumentException("Duplicate account id: " + account.getId());
        }
        return this;
    }

    public Account requireAccount(String accountId) {
        Account account = accountsById.get(accountId);
        if (account == null) {
            throw new AuthenticationException("Unknown account: " + accountId);
        }
        return account;
    }
}
