package br.ueg.tc.ueg_provider.infos;

import br.ueg.tc.pipa_integrator.interfaces.providers.info.IDiscipline;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class DisciplineSimpleDataUEG implements IDiscipline {

    @SerializedName("disc_cursada")
    private String disciplineName;

    @SerializedName("sit_desc")
    private String status;

}
