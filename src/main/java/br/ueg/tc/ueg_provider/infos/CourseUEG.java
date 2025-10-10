package br.ueg.tc.ueg_provider.infos;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class CourseUEG {
    @SerializedName("perfil")
    private String profile;

    @SerializedName("id_curso")
    private String courseId;

    @SerializedName("curso")
    private String courseName;

    @SerializedName("id_modalidade")
    private String modality;

    @SerializedName("dep_nome")
    private String depName;

    @SerializedName("matcurso_unidade")
    private String matUnity;

}
