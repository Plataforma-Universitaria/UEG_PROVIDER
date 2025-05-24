package br.ueg.tc.ueg_provider.ai;

import java.util.List;

public abstract class AIApi implements IAIApi{

    protected String getDisciplineNamesMessage(String disciplineIntentList, List<String> disciplinesNames) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(startDisciplineNameQuestion);
        for (String disciplineName : disciplinesNames) {
            stringBuilder.append(disciplineName);
            stringBuilder.append(", ");
        }
        stringBuilder.replace(stringBuilder.lastIndexOf(","), stringBuilder.length(), ".");
        stringBuilder.append(endDisciplineNameQuestion.replace("?", disciplineIntentList.toUpperCase().trim()));
        return stringBuilder.toString().trim();
    }

}
