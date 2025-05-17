package br.ueg.tc.ueg_provider.converter;

import br.ueg.tc.pipa_integrator.converter.IConverterInstitution;
import br.ueg.tc.pipa_integrator.institutions.info.*;
import br.ueg.tc.ueg_provider.infos.DisciplineScheduleUEG;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

public class ConverterUEG implements IConverterInstitution {

    @Override
    public IDisciplineSchedule getDisciplineFromJson(JsonElement jsonElement) {
        return null;
    }

    @Override
    public List<IDisciplineSchedule> getDisciplinesWithScheduleFromJson(JsonArray jsonArray) {
        return List.of();
    }

    @Override
    public List<IDisciplineGrade> getGradesWithDetailedGradeFromJson(JsonArray jsonArray) {
        return List.of();
    }

    @Override
    public List<IDisciplineGrade> getGradesBySemesterFromJson(JsonArray jsonElements, String semester) {
        return List.of();
    }

    @Override
    public IAcademicData getAcademicDataFromJson(JsonElement jsonElement) {
        return null;
    }

    @Override
    public List<IDisciplineAbsence> getDisciplinesWithAbsencesFromJson(JsonArray jsonElements) {
        return List.of();
    }

    @Override
    public IUserData getUserDataFromJson(JsonElement jsonElement) {
        return null;
    }
}
