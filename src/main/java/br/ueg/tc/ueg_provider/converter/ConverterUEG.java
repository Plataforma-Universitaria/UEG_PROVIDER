package br.ueg.tc.ueg_provider.converter;

import br.ueg.tc.pipa_integrator.converter.IConverterInstitution;
import br.ueg.tc.pipa_integrator.interfaces.providers.info.*;
import br.ueg.tc.ueg_provider.dto.KeyUrl;
import br.ueg.tc.ueg_provider.dto.Token;
import br.ueg.tc.ueg_provider.infos.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

public class ConverterUEG implements IConverterInstitution {

    private static final Gson gson = new Gson();


    public IDisciplineSchedule getDisciplineFromJson(JsonElement jsonObject) {
        return gson.fromJson(jsonObject, DisciplineScheduleUEG.class);
    }

    /**
     * Recebe um JsonArray com as informações das disciplinas e as converte na classe Discipline
     * caso o JsonArray esteja vazio retorna null, cria o map de dia e hora de inicio das disciplinas
     * @param jsonArray Json com as disciplinas e horarios de aulas
     * @return lista de diciplinas ou null
     */
    public List<IDisciplineSchedule> getDisciplinesWithScheduleFromJson(JsonArray jsonArray) {

        if(jsonArray != null && !jsonArray.isEmpty()) {
            List<IDisciplineSchedule> disciplineUEGList = new ArrayList<>();
            for (JsonElement jsonElement : jsonArray) {

                JsonObject jsonObject = jsonElement.getAsJsonObject();

                List<ISchedule> scheduleUEGList = new ArrayList<>();
                for (JsonElement element : jsonObject.getAsJsonArray("horario")){
                    ScheduleUEG scheduleUEG = gson.fromJson(element, ScheduleUEG.class);
                    scheduleUEGList.add(scheduleUEG);
                }

                DisciplineScheduleUEG disciplineUEG = gson.fromJson(jsonElement, DisciplineScheduleUEG.class);
                disciplineUEG.setScheduleList(scheduleUEGList);
                Map<String, String> dayStartEndHour = new HashMap<>();

                for (ISchedule iSchedule : disciplineUEG.getScheduleList()){
                    if(Objects.isNull(disciplineUEG.getTeacherName())){
                        disciplineUEG.setTeacherName(iSchedule.getTeacherName());
                    }

                    if (!dayStartEndHour.containsKey(iSchedule.getDay())) {
                        dayStartEndHour.put(iSchedule.getDay(),
                                iSchedule.getStartTime()+";"+iSchedule.getEndTime());
                    }else {
                        dayStartEndHour.put(iSchedule.getDay(),
                                dayStartEndHour.get(iSchedule.getDay())
                                        .concat("+")
                                        .concat(iSchedule.getStartTime()+";"+iSchedule.getEndTime()));
                    }
                }

                disciplineUEG.setDayStartEndHour(dayStartEndHour);

                disciplineUEGList.add(disciplineUEG);
            }
            return disciplineUEGList;
        }
        return null;
    }

    /**
     * Recebe uma lista de disciplinas e monta um map separando as aulas por dia e horario de inicio
     * @param disciplineUEGList lista das disciplinas com os horarios de aulas
     * @return mapa de dias e horarios com as disciplinas passadas
     */
    public static HashMap<String, List<DisciplineScheduleUEG>> doScheduleWeek(List<DisciplineScheduleUEG> disciplineUEGList) {

        HashMap<String, List<DisciplineScheduleUEG>> scheduleClassesWeek = new HashMap<>();

        for (DisciplineScheduleUEG disciplineUEG : disciplineUEGList) {
            disciplineUEG.getDayStartEndHour().forEach((day, hour) -> {
                if (!scheduleClassesWeek.containsKey(day)) {
                    List<DisciplineScheduleUEG> disciplineUEGS = new ArrayList<>();
                    disciplineUEGS.add(disciplineUEG);
                    scheduleClassesWeek.put(day, disciplineUEGS);
                } else {
                    scheduleClassesWeek.get(day).add(disciplineUEG);
                }
            });
        }

        return scheduleClassesWeek;
    }

    /**
     * Recebe o jsonArray com todas as disciplinas e notas e as converte para uma lista de Grade que possui os dados
     * da disciplina e a lista com notas da primeira e segunda VA
     *
     * @param jsonArray Json com todos dos dados de disciplinas realizadas pelo estudante
     * @return List de Grade preenchido ou vazio caso não tenha nada no jsonArray
     */
    public List<IDisciplineGrade> getGradesWithDetailedGradeFromJson(JsonArray jsonArray){
        List<IDisciplineGrade> iGradeList = new ArrayList<>();

        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            List<IDetailedDisciplineGrade> detailedGradeList = new ArrayList<>();

            for (JsonElement element : jsonObject.getAsJsonArray("nota_list")){
                DetailedDisciplineGradeUEG detailedGradeUEG = gson.fromJson(element, DetailedDisciplineGradeUEG.class);
                detailedGradeList.add(detailedGradeUEG);
            }
            DisciplineGradeUEG gradeUEG = gson.fromJson(jsonElement, DisciplineGradeUEG.class);
            gradeUEG.setDetailedGrades(detailedGradeList);
            iGradeList.add(gradeUEG);
        }

        return iGradeList;

    }

    @Override
    public List<IDisciplineGrade> getGradesBySemesterFromJson(JsonArray jsonArray, String semester) {
        if (isNullOrEmpty(jsonArray))
            return null;

        List<IDisciplineGrade> iGradeList = new ArrayList<>();

        for (JsonElement jsonElement : jsonArray) {
            if (jsonElement.toString().contains("\"gra_periodo\":\""+semester+"\"")) {
                DisciplineGradeUEG gradeUEG = gson.fromJson(jsonElement, DisciplineGradeUEG.class);
                iGradeList.add(gradeUEG);
            }
        }

        return iGradeList;
    }

    @Override
    public IAcademicData getAcademicDataFromJson(JsonElement jsonElement) {
        return gson.fromJson(jsonElement, AcademicDataUEG.class);
    }

    @Override
    public List<IDisciplineAbsence> getDisciplinesWithAbsencesFromJson(JsonArray jsonArray) {

        if (isNullOrEmpty(jsonArray))
            return  null;

        List<IDisciplineAbsence> disciplineAbsenceList = new ArrayList<>();

        for (JsonElement jsonElement : jsonArray) {
            DisciplineAbsenceUEG disciplineAbsenceUEG = gson.fromJson(jsonElement, DisciplineAbsenceUEG.class);
            disciplineAbsenceList.add(disciplineAbsenceUEG);
        }

        return disciplineAbsenceList;
    }


    public List<ComplementaryActivityUEG> getComplementaryActivitiesFromJson(JsonElement jsonElement){
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            return null;
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("lista");

        if (isNullOrEmpty(jsonArray))
            return null;

        List<ComplementaryActivityUEG> list = new ArrayList<>();

        for (JsonElement element : jsonArray) {
            ComplementaryActivityUEG act = gson.fromJson(element, ComplementaryActivityUEG.class);
            list.add(act);
        }

        return list;
    }

    public List<ExtensionActivityUEG> getExtensionActivityFromJson(JsonArray jsonArray) {

        if (isNullOrEmpty(jsonArray))
            return  null;

        List<ExtensionActivityUEG> extensionActivityList = new ArrayList<>();

        for (JsonElement jsonElement : jsonArray) {
            ExtensionActivityUEG extensionActivity = gson.fromJson(jsonElement, ExtensionActivityUEG.class);
            extensionActivityList.add(extensionActivity);
        }

        return extensionActivityList;
    }



    /**
     * Recebe um json e retorna os dados da pessoa logada
     * @param jsonElement json com os dados do estudante
     * @return IStudentData com os dados do estudante
     */
    @Override
    public IUserData getUserDataFromJson(JsonElement jsonElement) {
        return gson.fromJson(jsonElement, UserDataUEG.class);
    }

    @Override
    public List<IDiscipline> getDisciplinesFromJson(JsonArray jsonArray) {
        if (isNullOrEmpty(jsonArray))
            return  null;

        List<IDiscipline> disciplineSimpleDataList = new ArrayList<>();

        for (JsonElement jsonElement : jsonArray) {
            DisciplineSimpleDataUEG disciplineSimpleDataUEG = gson.fromJson(jsonElement, DisciplineSimpleDataUEG.class);
            disciplineSimpleDataList.add(disciplineSimpleDataUEG);
        }

        return disciplineSimpleDataList;
    }

    public KeyUrl getKeyUrlFromJson(JsonElement jsonElement){
        return gson.fromJson(jsonElement, KeyUrl.class);
    }

    public Token getTokenFromJson(JsonElement jsonElement){
        return gson.fromJson(jsonElement, Token.class);
    }
    private boolean isNullOrEmpty(JsonArray jsonArray) {
        return jsonArray == null || jsonArray.isEmpty();
    }

    public ComplementaryActivityUEG getComplementaryHoursActivitiesFromJson(JsonElement jsonElement) {
        if ((jsonElement.isJsonNull()) || !jsonElement.isJsonObject())
            return  null;
        ComplementaryActivityUEG complementaryActivity = gson.fromJson(jsonElement, ComplementaryActivityUEG.class);
        return complementaryActivity;
    }
}
