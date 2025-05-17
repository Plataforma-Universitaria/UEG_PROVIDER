package br.ueg.tc.ueg_provider.formatter;

import br.ueg.tc.pipa_integrator.institutions.info.IDisciplineSchedule;

import java.util.List;

public class FormatterScheduleByDisciplineName {

    public List<IDisciplineSchedule> scheduleByDisciplineName(String disciplineName, List<IDisciplineSchedule> disciplines) {
        if(disciplines != null && !disciplines.isEmpty()){
            return disciplines.stream()
                    .filter(discipline ->
                            discipline.getDisciplineName().trim().equalsIgnoreCase(disciplineName.trim())).toList();
        }
        return null;
    }
}
