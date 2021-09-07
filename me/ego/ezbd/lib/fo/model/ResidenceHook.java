package me.ego.ezbd.lib.fo.model;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import java.util.Collection;
import org.bukkit.Location;

class ResidenceHook {
    ResidenceHook() {
    }

    public Collection<String> getResidences() {
        return Residence.getInstance().getResidenceManager().getResidences().keySet();
    }

    public String getResidence(Location loc) {
        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
        return res != null ? res.getName() : null;
    }

    public String getResidenceOwner(Location loc) {
        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
        return res != null ? res.getOwner() : null;
    }
}