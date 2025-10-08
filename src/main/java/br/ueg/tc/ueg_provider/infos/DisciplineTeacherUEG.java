package br.ueg.tc.ueg_provider.infos;

import br.ueg.tc.pipa_integrator.interfaces.providers.info.IDiscipline;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class DisciplineTeacherUEG implements IDiscipline {

    @SerializedName("dis_descricao")
    private String disciplineName;

    @SerializedName("curso")
    private String courseName;

    @SerializedName("dep_nome")
    private String depName;

    @SerializedName("modalidade")
    private String model;

    @SerializedName("matriz")
    private String matrixName;

    @SerializedName("tcu_id")
    private String tcuId;

    @SerializedName("tc_aluno")
    private String tcStudentName;

    @SerializedName("tc_titulo")
    private String tcTitle;


    @Override
    public String getStatus() {
        StringBuilder tc = new StringBuilder();
        return tcuId.equals("0") ? "" : tc.append("Título: ").append(tcTitle).append("\nAluno: ").append(tcStudentName).toString();
    }

    public String getTcStudentName() {
        return tcStudentName.replace("{", "").replace("}", "").replace("\"", "");
    }
}
