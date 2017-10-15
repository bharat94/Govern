package govern.ny.hack.edu.govern.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

/**
 * Created by bharatv on 15/10/17.
 */

@IgnoreExtraProperties
public class DataModel {

    private List<GovModel> mGovModelList;

    public DataModel() {

    }

    public List<GovModel> getGovModelList() {
        return mGovModelList;
    }

    public void setGovModelList(List<GovModel> mGovModelList) {
        this.mGovModelList = mGovModelList;
    }
}
