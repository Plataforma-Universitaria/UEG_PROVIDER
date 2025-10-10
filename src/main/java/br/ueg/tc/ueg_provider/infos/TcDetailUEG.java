package br.ueg.tc.ueg_provider.infos;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TcDetailUEG{

    @SerializedName("tcu_titulo")
    private String tcuTitle;

    private String student;

    @SerializedName("tcu_id")
    private String tcuId;

    @SerializedName("tcp_id")
    private String tcpId;

    @SerializedName("data_acompanhamento")
    private String followupDate;

    @SerializedName("assuntos_discutidos")
    private String details;

    @SerializedName("horas")
    private String hours;

    @SerializedName("data_hora_cad")
    private String registerDate;

    @SerializedName("status")
    private String status;

    @SerializedName("parecer")
    private String evaluation;

}
