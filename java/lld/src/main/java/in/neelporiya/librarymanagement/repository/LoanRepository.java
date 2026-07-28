package in.neelporiya.librarymanagement.repository;

import in.neelporiya.librarymanagement.model.Loan;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Repository for active and historical loans. */
public class LoanRepository {

    private final Map<String, Loan> activeLoans = new ConcurrentHashMap<>();
    private final Map<String, Loan> allLoans = new ConcurrentHashMap<>();

    public void saveActive(Loan loan) {
        activeLoans.put(loan.getId(), loan);
        allLoans.put(loan.getId(), loan);
    }

    public Optional<Loan> findById(String loanId) {
        return Optional.ofNullable(allLoans.get(loanId));
    }

    public Loan removeActive(String loanId) {
        return activeLoans.remove(loanId);
    }

    public List<Loan> findActiveByMember(String memberId) {
        return activeLoans.values().stream()
                .filter(loan -> loan.getMemberId().equals(memberId))
                .toList();
    }
}
