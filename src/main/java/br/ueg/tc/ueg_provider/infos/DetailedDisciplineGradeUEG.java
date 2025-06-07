package br.ueg.tc.ueg_provider.infos;

import br.ueg.tc.pipa_integrator.interfaces.providers.info.IDetailedDisciplineGrade;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DetailedDisciplineGradeUEG implements IDetailedDisciplineGrade {

    @SerializedName("titulo")
    private String bimester;

    @SerializedName("nota")
    private Float gradeValue;

    @SerializedName("nota_peso")
    private Float gradeWeight;
}
