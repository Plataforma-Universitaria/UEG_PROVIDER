package br.ueg.tc.ueg_provider.infos;

import br.ueg.tc.pipa_integrator.interfaces.providers.info.IDisciplineAbsence;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class DisciplineAbsenceUEG implements IDisciplineAbsence {

    @SerializedName("disc_cursada")
    private String disciplineName;

    @SerializedName("gra_periodo")
    private String semesterActive;

    @SerializedName("mat_nfaltas")
    private Long totalAbsence;

    @SerializedName("mat_nfaltas_abonadas")
    private Long totalExcusedAbsences;

    @SerializedName("mat_pfreq_aula_ministrada")
    private String percentPresence;

    @SerializedName("sit_desc")
    private String status;

}
