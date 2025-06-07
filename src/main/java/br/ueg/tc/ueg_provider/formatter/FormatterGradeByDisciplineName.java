package br.ueg.tc.ueg_provider.formatter;

import br.ueg.tc.pipa_integrator.interfaces.providers.info.IDisciplineGrade;

import java.util.List;

public class FormatterGradeByDisciplineName {

    public List<IDisciplineGrade> scheduleByDisciplineName(String disciplineName, List<IDisciplineGrade> disciplines) {
        if(disciplines != null && !disciplines.isEmpty()){
            return disciplines.stream()
                    .filter(discipline ->
                            discipline.getDisciplineName().trim().equalsIgnoreCase(disciplineName.trim())).toList();
        }
        return null;
    }
}
