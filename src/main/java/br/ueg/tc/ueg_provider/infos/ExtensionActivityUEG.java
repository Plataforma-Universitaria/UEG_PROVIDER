package br.ueg.tc.ueg_provider.infos;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ExtensionActivityUEG {

    @SerializedName("titulo")
    private String title;

    @SerializedName("nome")
    private String name;

    @SerializedName("acao_dt_inicial")
    private String dateInicial;

    @SerializedName("acao_dt_final")
    private String dateFinal;

    @SerializedName("carga_horaria")
    private String hours;

    @SerializedName("tpp_descricao")
    private String tppDescription;

    @SerializedName("ch_qtde_cce")
    private String chQtdeCCE;

    @SerializedName("ch_exigida")
    private String hourLimit;

    @SerializedName("ch_cumprida")
    private String hourReached;

}
