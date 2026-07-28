package in.neelporiya.linkedin.repository;

import in.neelporiya.linkedin.model.Company;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** // DESIGN PATTERN: Repository — company persistence can become SQL/Elastic later. */
public class CompanyRepository {

    private final Map<String, Company> companiesById = new ConcurrentHashMap<>();

    public void save(Company company) {
        companiesById.put(company.getId(), company);
    }

    public Company findById(String id) {
        return companiesById.get(id);
    }

    public Collection<Company> findAll() {
        return companiesById.values();
    }
}
