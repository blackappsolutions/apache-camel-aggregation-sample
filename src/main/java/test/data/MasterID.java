package test.data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MasterID implements Serializable {

    @Column(name = "stepNumber", nullable = false)
    public Integer stepNumber;

    @Column(name = "stepName", nullable = false)
    public String stepName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MasterID)) return false;
        MasterID masterID = (MasterID) o;
        return Objects.equals(stepNumber, masterID.stepNumber) &&
            Objects.equals(stepName, masterID.stepName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stepNumber, stepName);
    }
}
