package br.ueg.tc.ueg_provider.infos;

import br.ueg.tc.pipa_integrator.interfaces.providers.KeyValue;
import br.ueg.tc.pipa_integrator.interfaces.providers.info.IUserData;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDataTeacherUEG implements IUserData {

    @SerializedName("id_pessoa")
    private String personId;

    @SerializedName("nome")
    private String firstName;

    @SerializedName("id_vinculo")
    private String boundId;

    @SerializedName("ref_departamento")
    private String depId;

    private String email;

    @SerializedName("personas")
    private List<String> personas;

    @SerializedName("keyValueList")
    private List<KeyValue> keyValueList;


}
