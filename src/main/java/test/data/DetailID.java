package test.data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DetailID implements Serializable {
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "stepNumber", referencedColumnName = "stepNumber"),
        @JoinColumn(name = "stepName", referencedColumnName = "stepName")
    })
    public Master master;

    @Column(name = "detailName", nullable = false)
    public String detailName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetailID)) return false;
        DetailID detailID = (DetailID) o;
        return Objects.equals(master, detailID.master) &&
            Objects.equals(detailName, detailID.detailName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(master, detailName);
    }
}
