package br.ueg.tc.ueg_provider.infos;

import br.ueg.tc.pipa_integrator.institutions.KeyValue;
import br.ueg.tc.pipa_integrator.institutions.info.IUserData;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDataUEG implements IUserData {

    @SerializedName("acu_id")
    private String personId;

    @SerializedName("nome")
    private String firstName;

    @SerializedName("email_discente")
    private String email;

    @SerializedName("personas")
    private List<String> personas;

    @SerializedName("keyValueList")
    private List<KeyValue> keyValueList;


}
