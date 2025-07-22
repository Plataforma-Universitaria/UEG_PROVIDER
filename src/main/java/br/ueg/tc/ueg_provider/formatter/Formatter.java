package br.ueg.tc.ueg_provider.formatter;

import br.ueg.tc.pipa_integrator.enums.WeekDay;
import br.ueg.tc.pipa_integrator.interfaces.providers.info.IDiscipline;
import br.ueg.tc.pipa_integrator.interfaces.providers.info.IDisciplineAbsence;
import br.ueg.tc.pipa_integrator.interfaces.providers.info.IDisciplineGrade;
import br.ueg.tc.pipa_integrator.interfaces.providers.info.IDisciplineSchedule;
import br.ueg.tc.ueg_provider.infos.ComplementaryActivityUEG;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Formatter {

    public List<IDisciplineGrade> disciplineGradeByDisciplineName(String disciplineName, List<IDisciplineGrade> disciplines) {
        if(disciplines != null && !disciplines.isEmpty()){
            return disciplines.stream()
                    .filter(discipline ->
                            discipline.getDisciplineName().trim().equalsIgnoreCase(disciplineName.trim())).toList();
        }
        return null;
    }

    public List<IDisciplineSchedule> scheduleByDisciplineName(String disciplineName, List<IDisciplineSchedule> disciplines) {
        if(disciplines != null && !disciplines.isEmpty()){
            return disciplines.stream()
                    .filter(discipline ->
                            discipline.getDisciplineName().trim().equalsIgnoreCase(disciplineName.trim())).toList();
        }
        return null;
    }
    public List<IDisciplineAbsence> absencesByDisciplineName(String disciplineName, List<IDisciplineAbsence> disciplines) {
        if(disciplines != null && !disciplines.isEmpty()){
            return disciplines.stream()
                    .filter(discipline ->
                            discipline.getDisciplineName().trim().equalsIgnoreCase(disciplineName.trim())).toList();
        }
        return null;
    }

    public List<IDisciplineSchedule> disciplinesWithScheduleByDay(WeekDay day, List<IDisciplineSchedule> disciplines){
        if(disciplines == null || !disciplines.isEmpty()){
            return sortDisciplinesByDayAndStartHourClass(day.getShortName(), doWeeklyClassSchedule(disciplines));
        }
        return null;
    }

    private HashMap<String, List<IDisciplineSchedule>> doWeeklyClassSchedule(List<IDisciplineSchedule> disciplineList) {

        if (Objects.isNull(disciplineList) || disciplineList.isEmpty()){
            return null;
        }
        HashMap<String, List<IDisciplineSchedule>> weeklyClassSchedule = new HashMap<>();

        for (IDisciplineSchedule discipline : disciplineList) {
            discipline.getDayStartEndHour().forEach((day, startEndHour) -> {
                if (!weeklyClassSchedule.containsKey(day)) {
                    List<IDisciplineSchedule> disciplines = new ArrayList<>();
                    disciplines.add(discipline);
                    weeklyClassSchedule.put(day, disciplines);
                } else {
                    weeklyClassSchedule.get(day).add(discipline);
                }
            });
        }

        return weeklyClassSchedule;
    }

    private static List<IDisciplineSchedule> sortDisciplinesByDayAndStartHourClass(String dayShortName, Map<String, List<IDisciplineSchedule>> disciplineList) {

        if (Objects.isNull(disciplineList) || disciplineList.isEmpty())
            return new ArrayList<IDisciplineSchedule>();

        List<IDisciplineSchedule> disciplines = disciplineList.get(dayShortName);

        if (Objects.isNull(disciplines) || disciplines.isEmpty())
            return new ArrayList<IDisciplineSchedule>();

        disciplines.sort((discipline1, discipline2) -> {

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            int result;
            try {
                result = sdf.parse(discipline1.getDayStartEndHour().get(dayShortName).split(";")[0])
                        .compareTo(sdf.parse
                                (discipline2.getDayStartEndHour().get(dayShortName).split(";")[0]));
            } catch (ParseException ex) {
                return 0;
            }

            return result;
        });
        return disciplines;
    }

    public List<IDisciplineGrade> disciplineGradeBySemester(String semester, List<IDisciplineGrade> disciplines) {
        if(disciplines != null && !disciplines.isEmpty()){
            return disciplines.stream()
                    .filter(discipline ->
                            discipline.getSemester().trim().equalsIgnoreCase(semester.trim())).toList();
        }
        return null;
    }

    public List<IDiscipline> disciplineByStatus(String aprovado, List<IDiscipline> disciplines) {
        if(disciplines != null && !disciplines.isEmpty()){
            return disciplines.stream()
                    .filter(discipline ->
                            discipline.getStatus().trim().equalsIgnoreCase(aprovado.trim())).toList();
        }
        return null;
    }

}
