package in.skdv.skdvinbackend.model.entity;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class Jumpday {

    @Id
    private ObjectId _id;

    private LocalDate date;
    private boolean jumping;
    private List<Slot> slots;
    private List<String> tandemmaster;
    private List<String> videoflyer;
    @NotNull
    public String clientId;


    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isJumping() {
        return jumping;
    }

    public void setJumping(boolean jumping) {
        this.jumping = jumping;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public void setSlots(List<Slot> slots) {
        this.slots = slots;
    }

    public List<String> getTandemmaster() {
        return tandemmaster;
    }

    public void setTandemmaster(List<String> tandemmaster) {
        this.tandemmaster = tandemmaster;
    }

    public List<String> getVideoflyer() {
        return videoflyer;
    }

    public void setVideoflyer(List<String> videoflyer) {
        this.videoflyer = videoflyer;
    }

    @Override
    public String toString() {
        return "Jumpday{" +
                "date=" + date +
                ", jumping=" + jumping +
                ", slots=" + slots +
                ", tandemmaster=" + tandemmaster +
                ", videoflyer=" + videoflyer +
                '}';
    }

}
