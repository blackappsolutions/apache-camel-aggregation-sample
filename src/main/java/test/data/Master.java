package test.data;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@NamedQueries(
    @NamedQuery(name = "selectAll", query = "Select m from Master m")
)
public class Master {

    @EmbeddedId
    public MasterID masterID;

    public String description;

    @Column(name = "group_id")
    public Integer groupId;

    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, orphanRemoval = true)//, mappedBy = "detailID.master" )
    @JoinColumns({
        @JoinColumn(name = "stepNumber", referencedColumnName = "stepNumber"),
        @JoinColumn(name = "stepName", referencedColumnName = "stepName")
    })
    public List<Detail> details;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Master)) return false;
        Master master = (Master) o;
        return Objects.equals(masterID, master.masterID) &&
            Objects.equals(description, master.description) &&
            Objects.equals(groupId, master.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(masterID, description, groupId);
    }
}
