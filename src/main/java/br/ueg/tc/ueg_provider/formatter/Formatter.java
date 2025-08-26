package br.ueg.tc.ueg_provider.formatter;

import br.ueg.tc.pipa_integrator.enums.WeekDay;
import br.ueg.tc.pipa_integrator.interfaces.providers.info.*;
import br.ueg.tc.ueg_provider.infos.ComplementaryActivityUEG;
import br.ueg.tc.ueg_provider.infos.ExtensionActivityUEG;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Formatter {

    public List<IDisciplineGrade> disciplineGradeByDisciplineName(String disciplineName, List<IDisciplineGrade> disciplines) {
        if (disciplines != null && !disciplines.isEmpty()) {
            return disciplines.stream()
                    .filter(discipline ->
                            discipline.getDisciplineName().trim().equalsIgnoreCase(disciplineName.trim())).toList();
        }
        return null;
    }

    public List<IDisciplineSchedule> scheduleByDisciplineName(String disciplineName, List<IDisciplineSchedule> disciplines) {
        if (disciplines != null && !disciplines.isEmpty()) {
            return disciplines.stream()
                    .filter(discipline ->
                            discipline.getDisciplineName().trim().equalsIgnoreCase(disciplineName.trim())).toList();
        }
        return null;
    }

    public List<IDisciplineAbsence> absencesByDisciplineName(String disciplineName, List<IDisciplineAbsence> disciplines) {
        if (disciplines != null && !disciplines.isEmpty()) {
            return disciplines.stream()
                    .filter(discipline ->
                            discipline.getDisciplineName().trim().equalsIgnoreCase(disciplineName.trim())).toList();
        }
        return null;
    }

    public List<IDisciplineSchedule> disciplinesWithScheduleByDay(WeekDay day, List<IDisciplineSchedule> disciplines) {
        if (disciplines == null || !disciplines.isEmpty()) {
            return sortDisciplinesByDayAndStartHourClass(day.getShortName(), doWeeklyClassSchedule(disciplines));
        }
        return null;
    }

    public HashMap<String, List<IDisciplineSchedule>> doWeeklyClassSchedule(List<IDisciplineSchedule> disciplineList) {

        if (Objects.isNull(disciplineList) || disciplineList.isEmpty()) {
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

    public static List<IDisciplineSchedule> sortDisciplinesByDayAndStartHourClass(String dayShortName, Map<String, List<IDisciplineSchedule>> disciplineList) {

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
        if (disciplines != null && !disciplines.isEmpty()) {
            return disciplines.stream()
                    .filter(discipline ->
                            discipline.getSemester().trim().equalsIgnoreCase(semester.trim())).toList();
        }
        return null;
    }

    public List<IDiscipline> disciplineByStatus(String aprovado, List<IDiscipline> disciplines) {
        if (disciplines != null && !disciplines.isEmpty()) {
            return disciplines.stream()
                    .filter(discipline ->
                            discipline.getStatus().trim().equalsIgnoreCase(aprovado.trim())).toList();
        }
        return null;
    }


    public String formatDiscipline(List<IDiscipline> disciplines) {
        StringBuilder stringBuilder = new StringBuilder();
        disciplines.forEach(discipline -> {
            stringBuilder
                    .append(discipline.getDisciplineName())
                    .append("\n");
        });
        return stringBuilder.toString();

    }

    public String formatComplementaryActivities(ComplementaryActivityUEG complementaryActivity) {
        float least = Float.parseFloat(complementaryActivity.getHourLimit()) -
                Float.parseFloat(complementaryActivity.getHourReached());

        return "Horas Exigidas: " + complementaryActivity.getHourLimit()
                + "\nHoras cumpridas: " + complementaryActivity.getHourReached()
                + (least != 0 ? "\nVocê precisa de: " + least : "Você concluiu sua horas complementares") + "\n Você também pode pedir por _detalhes das atividades complementares_";
    }

    public String formatComplementaryActivities(List<ComplementaryActivityUEG> complementaryActivities) {
        if (complementaryActivities == null || complementaryActivities.isEmpty()) {
            return "Nenhuma atividade complementar encontrada.";
        }

        Map<String, List<ComplementaryActivityUEG>> groupedByModality = complementaryActivities.stream()
                .collect(Collectors.groupingBy(ComplementaryActivityUEG::getModality));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Encontrei as seguintes atividades complementares:\n\n");

        groupedByModality.forEach((modality, activities) -> {
            activities.forEach(activity -> {
                stringBuilder.append("Descrição: ")
                        .append(Objects.toString(activity.getDescription(), "-")).append("\n")
                        .append("Status: ")
                        .append(Objects.toString(activity.getHomolApproved(), "-").equals("t") ? "Aprovado" : "Pendente").append(" \n ")
                        .append("Data de solicitação: ")
                        .append(activity.getSolicitedDate()).append("Horas Solicitadas: ").append("\n")
                        .append(Objects.toString(activity.getSolicitedHours(), "-")).append("\n")
                        .append("Horas Aprovadas: ")
                        .append(Objects.toString(activity.getApprovedHours(), "-"))
                        .append("\n\n");

            });

            stringBuilder.append("\n");
        });
        stringBuilder.append("Se quiser peça por _resumo das atividades complementares_ para um resumo geral");

        return stringBuilder.toString().trim();
    }

    public String formatSchedule(List<IDisciplineSchedule> disciplineSchedules) {
        StringBuilder result = new StringBuilder();
        DateTimeFormatter timeParser = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate dataPattern = LocalDate.now();

        List<WeekDay> ordemDosDias = WeekDay.getWeekDaysSort();
        for (WeekDay dia : ordemDosDias) {
            String shortName = dia.getShortName();
            String fullName = dia.getFullName();

            for (IDisciplineSchedule discipline : disciplineSchedules) {
                List<ISchedule> daySchedule = discipline.getScheduleList().stream()
                        .filter(s -> shortName.equalsIgnoreCase(s.getDay()))
                        .sorted(Comparator.comparing(s -> LocalTime.parse(s.getStartTime(), timeParser)))
                        .toList();

                if (daySchedule.isEmpty()) continue;

                LocalDateTime inicio = LocalDateTime.of(dataPattern, LocalTime.parse(daySchedule.get(0).getStartTime(), timeParser));
                LocalDateTime fim = LocalDateTime.of(dataPattern, LocalTime.parse(daySchedule.get(daySchedule.size() - 1).getEndTime(), timeParser));

                long breakTime = 0;
                for (int i = 1; i < daySchedule.size(); i++) {
                    LocalDateTime endsBefore = LocalDateTime.of(dataPattern, LocalTime.parse(daySchedule.get(i - 1).getEndTime(), timeParser));
                    LocalDateTime startsAt = LocalDateTime.of(dataPattern, LocalTime.parse(daySchedule.get(i).getStartTime(), timeParser));
                    long diff = Duration.between(endsBefore, startsAt).toMinutes();
                    if (diff > 0) breakTime += diff;
                }

                result.append(String.format(
                        "Na *%s*, você tem aula de *%s*, com %s, das %s às %s",
                        fullName,
                        discipline.getDisciplineName(),
                        discipline.getTeacherName() != null ? discipline.getTeacherName().trim() : "professor não informado",
                        inicio.format(outputFormat),
                        fim.format(outputFormat)
                ));

                if (breakTime > 0) {
                    result.append(String.format(" (*Intervalo* de %d minutos)", breakTime));
                }

                result.append(".\n\n");
            }
        }

        return result.toString();
    }


    public String formatAbsence(List<IDisciplineAbsence> iDisciplineAbsences) {
        StringBuilder absences = new StringBuilder();
        iDisciplineAbsences.forEach(absence -> {
            absences.append(absence.getDisciplineName())
                    .append(":\n")
                    .append("Total de Faltas: ")
                    .append(absence.getTotalAbsence())
                    .append("\nAbonadas: ")
                    .append(absence.getTotalExcusedAbsences())
                    .append("\nPercentual de presença: ")
                    .append(absence.getPercentPresence())
                    .append("%").append("\n");
        });
        return absences.toString();
    }


    public String formatExtensionActivities(List<ExtensionActivityUEG> extensionActivities) {
        StringBuilder stringBuilder = new StringBuilder();
        if (extensionActivities.isEmpty())
            return "Não encontrei nenhuma atividade de extensão!";
        stringBuilder.append("Encontrei as seguintes informações:\n");
        extensionActivities.forEach(ext -> {
            stringBuilder
                    .append("Título: ").append(ext.getTitle()).append("\n")
                    .append("Responsável: ").append(ext.getName()).append("\n")
                    .append("Horas: ").append(ext.getHours()).append("\n\n");
        });
        return stringBuilder.toString();
    }

    public String formatDisciplineGrade(List<IDisciplineGrade> iDisciplineGrades) {
        StringBuilder stringBuilder = new StringBuilder();
        if (iDisciplineGrades.isEmpty())
            return "Não encontrei nenhuma nota associada a essa diciplina!";
        stringBuilder.append("Encontrei as seguintes informações:\n");
        iDisciplineGrades.forEach(disciplineGrade -> {
            stringBuilder
                    .append(disciplineGrade.getDisciplineName()).append("\n");
            disciplineGrade.getDetailedGrades()
                    .forEach(detailedGrade -> {
                        stringBuilder.append(detailedGrade.getBimester()).append(" - ")
                                .append(detailedGrade.getGradeValue()).append("\n");
                    });
            stringBuilder.append("Média final: ").append(disciplineGrade.getFinalMedia()).append("\n\n");
        });
        return stringBuilder.toString();
    }
}
