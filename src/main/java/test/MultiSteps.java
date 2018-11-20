package test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "MULTISTEPS")
@NamedQuery(
        name = "stepByStep",
        query = "FROM MultiSteps m"
)
public class MultiSteps {

    @Id
    @Column(name = "step")
    public Integer step;

    @Column(name = "description")
    public String description;

    @Column(name = "group_id")
    public Integer groupId;
}
