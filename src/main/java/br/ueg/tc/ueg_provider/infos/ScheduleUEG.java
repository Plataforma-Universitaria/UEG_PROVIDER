package br.ueg.tc.ueg_provider.infos;

import br.ueg.tc.pipa_integrator.interfaces.providers.info.ISchedule;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class ScheduleUEG implements ISchedule {

    @SerializedName("dia_desc_red")
    private String day;

    @ToString.Exclude
    @SerializedName("dia_id")
    private String dayId;

    @SerializedName("hor_ini")
    private String startTime;

    @SerializedName("hor_fim")
    private String endTime;

    @SerializedName("professor")
    private String teacherName;

    @SerializedName("sala")
    private String classroom;

}
