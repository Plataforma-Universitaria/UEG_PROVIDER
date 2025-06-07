package br.ueg.tc.ueg_provider.infos;

import br.ueg.tc.pipa_integrator.interfaces.providers.info.IDetailedDisciplineGrade;
import br.ueg.tc.pipa_integrator.interfaces.providers.info.IDisciplineGrade;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@Setter
public class DisciplineGradeUEG implements IDisciplineGrade {

    @SerializedName("disc_cursada")
    private String disciplineName;

    @SerializedName("mat_mediafinal")
    private Float finalMedia;

    @SerializedName("periodo_grade")
    private String semester;

    @ToString.Exclude
    private List<IDetailedDisciplineGrade> detailedGrades;

}
