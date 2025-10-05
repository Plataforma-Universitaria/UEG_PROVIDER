package br.ueg.tc.ueg_provider.infos;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class TcDetailUEG{

    @SerializedName("tcu_titulo")
    private String tcuTitle;

    private String student;

    @SerializedName("tcu_id")
    private String tcuId;

    @SerializedName("data_acompanhamento")
    private String followupDate;

    @SerializedName("assuntos_discutidos")
    private String details;

    @SerializedName("horas")
    private String hours;

    @SerializedName("data_hora_cad")
    private String registerDate;

    @SerializedName("status_descricao")
    private String status;

}
