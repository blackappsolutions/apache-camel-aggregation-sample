package test.data;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.util.Objects;

@Entity
public class Detail {

    @EmbeddedId
    public DetailID detailID;

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof Detail)) return false;
        Detail detail = (Detail) o;
        return Objects.equals(detailID, detail.detailID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(detailID);
    }
}
