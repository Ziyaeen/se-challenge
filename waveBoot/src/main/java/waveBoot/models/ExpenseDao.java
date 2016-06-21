package waveBoot.models;

import org.springframework.data.repository.CrudRepository;
import javax.transaction.Transactional;

@Transactional
public interface ExpenseDao extends CrudRepository<Expense, Long> {

    /* Spring Data JPA automatically generates code for below method */
    public Expense findByEmployeeName(String employeeName);
}
