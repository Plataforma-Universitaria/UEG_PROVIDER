package br.ueg.tc.ueg_provider.infos;

import br.ueg.tc.pipa_integrator.interfaces.providers.info.IDisciplineSchedule;
import br.ueg.tc.pipa_integrator.interfaces.providers.info.ISchedule;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
@Getter
@Setter
public class DisciplineScheduleUEG implements IDisciplineSchedule {

    @SerializedName("disciplina")
    private String disciplineName;

    @SerializedName("sit_desc")
    private String status;

    @ToString.Exclude
    private List<ISchedule> scheduleList;

    private String teacherName;

    private Map<String, String> dayStartEndHour = new HashMap<>();
}
